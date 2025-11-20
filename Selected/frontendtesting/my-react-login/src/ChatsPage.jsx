import { useEffect, useMemo, useRef, useState } from "react";
import SockJS from "sockjs-client";
import Stomp from "stompjs";

// Helper to build API URLs
const BASE_URL = import.meta.env.VITE_API_BASE_URL;
function api(path) {
  const base = BASE_URL?.replace(/\/+$/, "") || "http://localhost:8080";
  const p = String(path || "").replace(/^\/+/, "");
  return `${base}/${p}`;
}

// Emotes
const EMOTES = [
  { code: 1, label: "👍" },
  { code: 2, label: "❤️" },
  { code: 3, label: "😂" },
];

export default function ChatsPage({ auth, onLogout }) {
  const { token, userId, username } = auth || {};
  const displayName = username || "USER";
  const effectiveUserId = userId; 

  const [status, setStatus] = useState("");

  // groups tabs
  const [groups, setGroups] = useState([]); // [{id, groupName, lastMessage}]
  const [activeTabId, setActiveTabId] = useState(null);

  // messages by group
  const [messagesByGroup, setMessagesByGroup] = useState({});
  const activeMessages = messagesByGroup[activeTabId] || [];

  // composer
  const [composer, setComposer] = useState("");

  // new chats
  const [showNewChat, setShowNewChat] = useState(false);
  const [newName, setNewName] = useState("");
  const [newMembers, setNewMembers] = useState("");

  // WebSockets
  const stompRef = useRef(null);
  const [wsConnected, setWsConnected] = useState(false);

  // Connect WebSocket on mount
  useEffect(() => {
    try {
      const socket = new SockJS(api("/ws-chat"));
      const client = Stomp.over(socket);

      // silence STOMP debug spam
      client.debug = () => {};

      client.connect(
        {},
        () => {
          console.log("WebSocket connected");
          stompRef.current = client;
          setWsConnected(true);
        },
        (error) => {
          console.error("WebSocket/STOMP error:", error);
          setWsConnected(false);
        }
      );

      return () => {
        setWsConnected(false);
        if (stompRef.current) {
          stompRef.current.disconnect(() =>
            console.log("WebSocket disconnected")
          );
          stompRef.current = null;
        }
      };
    } catch (err) {
      console.error("Error setting up WebSocket:", err);
      setWsConnected(false);
    }
  }, []);

  // current group topic
  useEffect(() => {
    if (!wsConnected || !activeTabId || !stompRef.current) return;

    const client = stompRef.current;
    const dest = `/topic/group.${activeTabId}`;
    console.log("Subscribing to", dest);

    const subscription = client.subscribe(dest, (frame) => {
      try {
        const msg = JSON.parse(frame.body);

        setMessagesByGroup((prev) => {
          const list = prev[activeTabId] || [];
          // if message already in the list, update it .
          // for like emotes
          const existingIndex = list.findIndex((m) => m.id === msg.id);
          let nextList;
          if (existingIndex >= 0) {
            nextList = [...list];
            nextList[existingIndex] = { ...list[existingIndex], ...msg };
          } else {
            nextList = [...list, msg];
          }
          return { ...prev, [activeTabId]: nextList };
        });

        // update preview text on tabs
        setGroups((gs) =>
          gs.map((g) =>
            g.id === activeTabId ? { ...g, lastMessage: msg.content || "" } : g
          )
        );
      } catch (err) {
        console.error("Error parsing WS message", err);
      }
    });

    return () => {
      subscription.unsubscribe();
    };
  }, [wsConnected, activeTabId]);

  // Load chats for logged in user
  async function loadMyGroups() {
    if (!effectiveUserId) {
      setStatus("Missing user id.");
      return;
    }
    setStatus("");
    try {
      console.log("Loading groups for user", effectiveUserId);
      const res = await fetch(api(`/api/group/user/${effectiveUserId}`), {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      if (!res.ok) throw new Error(await res.text());
      const data = await res.json();
      console.log("Groups response:", data);

      const normalized = (data || []).map((g) => ({
        id: g.id,
        groupName: g.groupName ?? g.name ?? `Group ${g.id}`,
        lastMessage: "",
      }));
      setGroups(normalized);
      if (normalized[0]?.id) {
        setActiveTabId(normalized[0].id);
        await loadGroupMessages(normalized[0].id);
      }
    } catch (err) {
      console.error("Failed to load chats:", err);
      setStatus(`Failed to load chats: ${err.message}`);
    }
  }

  // Load messages for a specific group
  async function loadGroupMessages(groupId) {
    try {
      console.log("Loading messages for group", groupId);
      const res = await fetch(api(`/api/messages/group/${groupId}`), {
        headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}) },
      });
      if (!res.ok) throw new Error(await res.text());
      const data = await res.json();
      console.log("Messages response:", data);

      setMessagesByGroup((m) => ({ ...m, [groupId]: data || [] }));
      const last = data?.[data.length - 1]?.content || "";
      setGroups((gs) =>
        gs.map((g) => (g.id === groupId ? { ...g, lastMessage: last } : g))
      );
    } catch (err) {
      console.error("Failed to load messages:", err);
      setStatus(`Failed to load messages: ${err.message}`);
    }
  }

  // Send message in active group
  async function sendMessage(e) {
    e.preventDefault();
    if (!activeTabId || !effectiveUserId || !composer.trim()) return;
    setStatus("");
    try {
      console.log("Sending message to group", activeTabId);
      const res = await fetch(api("/api/messages/send"), {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: new URLSearchParams({
          senderId: effectiveUserId,
          groupId: activeTabId,
          content: composer.trim(),
        }).toString(),
      });
      if (!res.ok) throw new Error(await res.text());
      const msg = await res.json();
      console.log("Send message response:", msg);

      setMessagesByGroup((prev) => {
        const list = prev[activeTabId] || [];
        if (msg && !list.some((m) => m.id === msg.id)) {
          return {
            ...prev,
            [activeTabId]: [...list, msg],
          };
        }
        if (!msg) {
          return {
            ...prev,
            [activeTabId]: [
              ...list,
              {
                id: Date.now(),
                senderId: effectiveUserId,
                content: composer.trim(),
                sentAt: new Date().toISOString(),
              },
            ],
          };
        }
        return prev;
      });

      setComposer("");
    } catch (err) {
      console.error("Send failed:", err);
      setStatus(err.message || "Send failed");
    }
  }

  // React to a message with emotes
  async function reactToMessage(messageId, emoteCode) {
    if (!messageId || !emoteCode) return;
    setStatus("");
    try {
      console.log("Reacting to message", messageId, "with emote", emoteCode);
      const res = await fetch(api("/api/messages/emote"), {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: new URLSearchParams({
          id: messageId,
          emote: emoteCode,
        }).toString(),
      });

      if (!res.ok) throw new Error(await res.text());
      const updated = await res.json();
      console.log("Emote response:", updated);

      // update this message in the local state
      setMessagesByGroup((prev) => {
        const list = prev[activeTabId] || [];
        const nextList = list.map((m) =>
          m.id === updated.id ? { ...m, ...updated } : m
        );
        return { ...prev, [activeTabId]: nextList };
      });
    } catch (err) {
      console.error("Emote failed:", err);
      setStatus(err.message || "Emote failed");
    }
  }

  // Create a new group or dm
  async function createGroup({ groupName, memberIds }) {
    setStatus("");
    try {
      console.log("Creating group:", groupName, memberIds);
      const res = await fetch(api("/api/group/create"), {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: new URLSearchParams([
          ["groupName", groupName],
          ...memberIds.map((m) => ["memberIds", String(m)]),
        ]).toString(),
      });

      if (!res.ok) throw new Error(await res.text());

      const g = await res.json();
      console.log("Create group response:", g);

      const newTab = { id: g.id, groupName: g.groupName, lastMessage: "" };
      setGroups((prev) => [...prev, newTab]);
      setActiveTabId(g.id);
      await loadGroupMessages(g.id);

      setStatus("Chat created!");
      return g; // success
    } catch (err) {
      console.error("Create group failed:", err);
      setStatus(err.message || "Create group failed");
      throw err;
    }
  }

  async function submitNewChat(e) {
    e.preventDefault();
    if (!effectiveUserId) {
      setStatus("Missing user id.");
      return;
    }

    const rawMemberStrings = newMembers
      .split(",")
      .map((s) => s.trim())
      .filter(Boolean);

    const otherMemberIds = rawMemberStrings
      .map((s) => Number(s))
      .filter((n) => !Number.isNaN(n));

    const ids = Array.from(new Set([effectiveUserId, ...otherMemberIds]));

    if (ids.length < 2) {
      setStatus("Please add at least one other member ID.");
      return;
    }

    try {
      await createGroup({
        groupName: newName || "New Chat",
        memberIds: ids,
      });

      setShowNewChat(false);
      setNewName("");
      setNewMembers("");
    } catch {
    }
  }

  // For auto-scrolling
  const lastKey = useMemo(
    () => `${activeTabId}:${activeMessages.length}`,
    [activeTabId, activeMessages.length]
  );

  useEffect(() => {
    const el = document.getElementById("messages-end");
    if (el) el.scrollIntoView({ behavior: "smooth" });
  }, [lastKey]);

  return (
    <div className="h-screen w-full flex flex-col">
      {/* Top bar */}
      <div className="flex items-center justify-between border-b px-4 py-2 bg-white rounded-b-2xl shadow-xl">
        <div className="flex items-center gap-3">
          <div>
            <p className="text-xs text-gray-400">Welcome back</p>
            <p className="text-lg font-semibold truncate max-w-[220px]">
              {displayName}
            </p>
            {effectiveUserId && (
              <p className="text-[11px] text-gray-500 mt-1">
                User ID:{" "}
                <span className="font-semibold">{effectiveUserId}</span>
              </p>
            )}
          </div>

          <div className="ml-4">
            <button
              className="border rounded px-3 py-1 text-sm"
              type="button"
              onClick={loadMyGroups}
            >
              Refresh chats
            </button>
          </div>

          <span className="text-xs ml-2">
            WS:{" "}
            {wsConnected ? (
              <span className="text-green-600">connected</span>
            ) : (
              <span className="text-red-600">disconnected</span>
            )}
          </span>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            className="border px-3 py-1 text-sm rounded-full"
            onClick={() => setShowNewChat(true)}
          >
            New chat
          </button>

          <button
            type="button"
            className="border rounded-full px-3 py-1 text-sm"
            onClick={() => alert("Profile page coming soon!")}
          >
            Profile
          </button>

          <button
            className="border rounded px-3 py-1 text-sm"
            type="button"
            onClick={onLogout}
          >
            Logout
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-2 border-b px-4 py-2 overflow-x-auto bg-gray-100">
        {groups.length === 0 ? (
          <div className="text-sm text-gray-500">
            No chats yet. Create one or load your chats.
          </div>
        ) : (
          groups.map((g) => {
            const isActive = activeTabId === g.id;
            return (
              <button
                key={g.id}
                onClick={() => {
                  setActiveTabId(g.id);
                  loadGroupMessages(g.id);
                }}
                className={[
                  "px-4 py-2 rounded-t-xl border text-left transition-colors",
                  isActive
                    ? "bg-white border-b-white shadow-sm"
                    : "bg-gray-50 hover:bg-gray-100 border-transparent",
                ].join(" ")}
                title={g.groupName}
              >
                <div className="text-sm font-semibold truncate max-w-[220px]">
                  {g.groupName}
                </div>
                <div className="text-[11px] text-gray-500 truncate max-w-[220px]">
                  {g.lastMessage ? `Last: ${g.lastMessage}` : "No messages yet"}
                </div>
              </button>
            );
          })
        )}
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-auto p-4 bg-white">
        {activeTabId ? (
          activeMessages.length ? (
            activeMessages.map((m) => (
              <div key={m.id} className="mb-4">
                <div className="text-xs text-gray-500">
                  <strong>From:</strong> {m.senderId} ·{" "}
                  {m.sentAt
                    ? new Date(m.sentAt).toLocaleString()
                    : "just now"}
                </div>

                <div className="border rounded-lg p-3 inline-block mt-1">
                  {m.content}
                </div>

                {/* Emote reactions */}
                <div className="mt-1 flex items-center gap-2 text-xs text-gray-500">
                  <span>React:</span>
                  {EMOTES.map((e) => (
                    <button
                      key={e.code}
                      type="button"
                      className="border rounded-full px-2 py-0.5 text-xs"
                      onClick={() => reactToMessage(m.id, e.code)}
                    >
                      {e.label}
                    </button>
                  ))}
                  {m.emote && (
                    <span className="ml-2">
                      Current:{" "}
                      {EMOTES.find((e) => e.code === m.emote)?.label ||
                        `#${m.emote}`}
                    </span>
                  )}
                </div>
              </div>
            ))
          ) : (
            <div className="text-sm text-gray-500">No messages yet.</div>
          )
        ) : (
          <div className="text-sm text-gray-500">
            Select a tab to view messages.
          </div>
        )}
        <div id="messages-end" />
      </div>

      {/* Composer */}
      <form
        onSubmit={sendMessage}
        className="border-t p-3 flex gap-2 bg-gray-50"
      >
        <input
          className="border rounded px-3 py-2 flex-1"
          placeholder={
            activeTabId ? "Type a message…" : "Select a chat tab first"
          }
          value={composer}
          onChange={(e) => setComposer(e.target.value)}
          disabled={!activeTabId}
        />
        <button
          className="border rounded px-4 py-2"
          type="submit"
          disabled={!activeTabId || !composer.trim()}
        >
          Send
        </button>
      </form>

      {status && (
        <div className="px-4 py-2 text-xs text-red-600">{status}</div>
      )}

      {/* New Chats */}
      {showNewChat && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-5">
            <h3 className="text-lg font-semibold mb-1">Start a new chat</h3>
            <p className="text-xs text-gray-500 mb-3">
              For a direct message, leave the group name blank and enter one
              other user ID. For a group chat, give it a name and add multiple
              IDs. Your ID is added automatically.
            </p>
            <form onSubmit={submitNewChat} className="space-y-3">
              <div>
                <label className="text-sm text-gray-600">
                  Group name (optional)
                </label>
                <input
                  className="border rounded px-3 py-2 w-full"
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  placeholder="e.g. Project Team"
                />
              </div>
              <div>
                <label className="text-sm text-gray-600">
                  Member IDs (comma-separated)
                </label>
                <input
                  className="border rounded px-3 py-2 w-full"
                  value={newMembers}
                  onChange={(e) => setNewMembers(e.target.value)}
                  placeholder="e.g. 2, 5, 7 (your ID is auto-included)"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  className="border rounded px-3 py-2"
                  onClick={() => setShowNewChat(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="border rounded px-3 py-2">
                  Create
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
