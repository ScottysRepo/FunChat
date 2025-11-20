import { useState } from "react";

const BASE_URL = import.meta.env.VITE_API_BASE_URL;
function api(path) {
  const base = BASE_URL?.replace(/\/+$/, "") || "http://localhost:8080";
  const p = String(path || "").replace(/^\/+/, "");
  return `${base}/${p}`;
}

export default function AuthForm({ onLoginSuccess }) {
  const [mode, setMode] = useState("login"); // "login" | "register"
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [msg, setMsg] = useState("");

  async function handleRegister() {
    setMsg("");
    try {
      const res = await fetch(api("/api/auth/register"), {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ username, password }).toString(),
      });

      const ct = res.headers.get("content-type") || "";
      if (!res.ok) {
        const body = ct.includes("application/json")
          ? JSON.stringify(await res.json())
          : await res.text();
        throw new Error(`${res.status} ${res.statusText}${body ? `: ${body}` : ""}`);
      }

      const data = ct.includes("application/json") ? await res.json() : null;
      if (data?.id) {
        setMsg(`Registered! Your user id is ${data.id}. You can now log in.`);
      } else {
        setMsg("Registered! You can now log in.");
      }

      setMode("login");
    } catch (err) {
      setMsg(err.message || "Registration failed");
    }
  }

  async function handleLogin() {
    setMsg("");
    try {
      const res = await fetch(api("/api/auth/login"), {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ username, password }).toString(),
      });

      const data = await res.json();
      console.log("Login response:", data); // 🔍 debug

      if (!res.ok) {
        throw new Error(data?.message || "Login failed");
      }

      const token = data.token;
      const userId = data.id;
      const uname = data.username;

      if (!token) throw new Error("No token returned from server");
      if (!userId) throw new Error("No user id returned from server");

      onLoginSuccess({
        token,
        userId,
        username: uname,
      });
    } catch (err) {
      console.error(err);
      setMsg(err.message || "Login failed");
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (mode === "register") await handleRegister();
    else await handleLogin();
  }

  return (
    <div className="h-screen w-full flex items-center justify-center">
      <div className="max-w-sm w-full mx-auto p-6 bg-white rounded-2xl shadow-xl">
        <h1 className="text-xl font-bold mb-4">
          {mode === "login" ? "Welcome back" : "Create your account"}
        </h1>
        <p className="text-sm text-gray-600 mb-3">
          {mode === "login"
            ? "Sign in to start chatting with your friends in FunChat."
            : "Pick a username and password to get started."}
        </p>

        <form onSubmit={handleSubmit} className="space-y-3">
          <input
            className="border p-2 w-full"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <input
            className="border p-2 w-full"
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <button className="border p-2 w-full" type="submit">
            {mode === "login" ? "Log in" : "Create account"}
          </button>
        </form>

        <button
          type="button"
          className="text-sm underline mt-3"
          onClick={() =>
            setMode(mode === "login" ? "register" : "login")
          }
        >
          {mode === "login"
            ? "Need an account? Register"
            : "Have an account? Log in"}
        </button>

        {msg && <p className="mt-3 text-sm">{msg}</p>}
      </div>
    </div>
  );
}
