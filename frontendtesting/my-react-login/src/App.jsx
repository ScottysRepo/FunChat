import { useEffect, useState } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import AuthForm from "./AuthForm";
import Dashboard from "./Dashboard";
import ChatsPage from "./ChatsPage";

export default function App() {
  // Load saved auth (token + userId + username) from localStorage
  const [auth, setAuth] = useState(() => {
    const saved = localStorage.getItem("auth");
    return saved ? JSON.parse(saved) : null;
  });

  // Keep localStorage in sync
  useEffect(() => {
    if (auth) {
      localStorage.setItem("auth", JSON.stringify(auth));
    } else {
      localStorage.removeItem("auth");
    }
  }, [auth]);

  // Called when login succeeds
  const handleLoginSuccess = ({ token, userId, username }) => {
    setAuth({ token, userId, username });
  };

  const handleLogout = () => {
    setAuth(null);
  };

  // Not logged in 
  if (!auth) {
    return <AuthForm onLoginSuccess={handleLoginSuccess} />;
  }

  // Logged in 
  return (
    <Routes>
      <Route
        path="/"
        element={<Dashboard auth={auth} onLogout={handleLogout} />}
      />
      <Route
        path="/chats"
        element={<ChatsPage auth={auth} onLogout={handleLogout} />}
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
