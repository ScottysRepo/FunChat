import i18n from "i18next";
import { initReactI18next } from "react-i18next";

i18n.use(initReactI18next).init({
  lng: "en",
  fallbackLng: "en",
  interpolation: {
    escapeValue: false,
  },
  resources: {
    en: {
      translation: {
        welcome: "Welcome back",
        refreshChats: "Refresh chats",
        newChat: "New chat",
        profile: "Profile",
        logout: "Logout",
        noChats: "No chats yet. Create one or load your chats.",
        typeMessage: "Type a message…",
        selectChat: "Select a chat tab first",
        react: "React",
        current: "Current",
        startNewChat: "Start a new chat",
        dmHint:
          "For a direct message, leave the group name blank and enter one other user ID.",
        groupName: "Group name",
        memberIds: "Member IDs (comma-separated)",
        cancel: "Cancel",
        create: "Create",
      },
    },

    es: {
      translation: {
        welcome: "Bienvenido de nuevo",
        refreshChats: "Actualizar chats",
        newChat: "Nuevo chat",
        profile: "Perfil",
        logout: "Cerrar sesión",
        noChats: "No hay chats aún. Crea uno o carga tus chats.",
        typeMessage: "Escribe un mensaje…",
        selectChat: "Selecciona primero un chat",
        react: "Reaccionar",
        current: "Actual",
        startNewChat: "Iniciar un nuevo chat",
        dmHint:
          "Para un mensaje directo, deja el nombre vacío y añade un ID.",
        groupName: "Nombre del grupo",
        memberIds: "IDs de miembros (separados por comas)",
        cancel: "Cancelar",
        create: "Crear",
      },
    },
  },
});

export default i18n;
