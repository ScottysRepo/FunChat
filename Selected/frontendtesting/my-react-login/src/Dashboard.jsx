import { useNavigate } from "react-router-dom";

export default function Dashboard({ auth, onLogout }) {
  const nav = useNavigate();
  const username = auth?.username || "Friend";
  const userId = auth?.userId;

  return (
    <div className="h-screen w-full flex flex-col">
      {/* Top bar */}
      <div className="flex items-center justify-between border-b px-4 py-3 bg-white rounded-b-2xl shadow-xl">
        <div>
          <p className="text-xs text-gray-400">Welcome back</p>
          <p className="text-lg font-semibold truncate max-w-[220px]">
            {username}
          </p>
          {userId && (
            <p className="text-[11px] text-gray-500 mt-1">
              User ID: <span className="font-semibold">{userId}</span>
            </p>
          )}
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            className="border rounded-full px-3 py-1 text-sm"
            onClick={() => alert("Profile page coming soon!")}
          >
            Profile
          </button>
          <button
            type="button"
            className="border rounded px-3 py-1 text-sm"
            onClick={onLogout}
          >
            Logout
          </button>
        </div>
      </div>

      {/* Main content */}
      <main className="flex-1 flex items-center justify-center px-4 pb-8">
        <div className="max-w-xl w-full mx-auto p-8 space-y-6 bg-white rounded-2xl shadow-xl">
          <h1 className="text-2xl font-bold">FunChat</h1>
          <p className="text-sm text-gray-600">
            Hey {username}, you’re all set. Jump back into your chats or start a
            new one.
          </p>

          <div className="flex gap-3">
            <button
              className="border px-4 py-2 rounded"
              type="button"
              onClick={() => nav("/chats")}
            >
              Open chats
            </button>
            <button
              className="border px-4 py-2 rounded"
              type="button"
              onClick={onLogout}
            >
              Logout
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}
