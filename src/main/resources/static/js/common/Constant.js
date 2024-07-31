const HTTP = "http://";
const HTTPS = "https//";
const WS = "ws://";
const WSS = "wss://";

const HOST = location.host;

const HOME_URL  = HTTP + HOST + "/home";
const FILES_URL  = HTTP + HOST + "/files";
const CHAT_URL  = HTTP + HOST + "/chat";
const GUEST_DEMO_URL  = HTTP + HOST + "/guest/demo";

const FILES_CONNECT = WS + HOST + "/files_connect";
const CHAT_CONNECT = WS + HOST + "/chat_connect";