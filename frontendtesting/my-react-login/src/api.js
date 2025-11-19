const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

function toFormBody(params) {
  const usp = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => usp.set(k, String(v)));
  return usp.toString();
}

export async function register(username, password) {
  const res = await fetch(`${BASE_URL}/api/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: toFormBody({ username, password })
  });
  if (!res.ok) throw new Error(await res.text());
  return res.text();
}

export async function login(username, password) {
  const res = await fetch(`${BASE_URL}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: toFormBody({ username, password })
  });
  if (!res.ok) throw new Error(await res.text());
  const text = await res.text();
  const token = text.replace(/^JWT Token:\s*/i, "").trim();
  return token;
}

export async function sendMessage(senderId, groupId, content, token) {
  const res = await fetch(`${BASE_URL}/api/messages/send`, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: toFormBody({ senderId, groupId, content })
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}
