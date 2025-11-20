if (typeof globalThis.global === "undefined") {
  globalThis.global = globalThis;
}

import SockJS from "sockjs-client";
export default SockJS;
