const HTTP = "http://";
const HTTPS = "https://";
const WS = "ws://";
const WSS = "wss://";

const HOST = location.host;

const USER_LOGIN_URL  = HTTP + HOST + "/user/login";
const USER_REGISTER_URL  = HTTP + HOST + "/user/register";
const USER_UPDATE_URL  = HTTP + HOST + "/user/update";
const USER_DELETE_URL  = HTTP + HOST + "/user/delete";
const HOME_URL  = HTTP + HOST + "/home";
const FILES_URL  = HTTP + HOST + "/files";
const ECHO_URL  = HTTP + HOST + "/echo";
const GUEST_DEMO_URL  = HTTP + HOST + "/guest/demo";

/**
 * 根据提供的 URL 执行页面跳转
 * @param {string} url - 要跳转的目标 URL
 */
function action(url){
    location.href = url;
}

/**
 * 生成 JSON 字符串
 * @param {string} type - 消息类型
 * @param {string} key - 消息键
 * @param {string} message - 消息内容
 * @param {object|Map} value - 附加值
 * @returns {string} - JSON 字符串
 */
function getJson(type, key, message, value){
    var obj = {type: type, key: key, message: message};
    if (typeof value === "object" && !Array.isArray(value)) {
        obj.value = value;
    } else if (value instanceof Map) {
        obj.value = Object.fromEntries(value.entries());
    } else {
        obj.value = {};
    }
    return JSON.stringify(obj);
}
