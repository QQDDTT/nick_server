const HTTP = "http://";
const HTTPS = "https//";
const WS = "ws://";
const WSS = "wss://";

const HOST = location.host;

const HOME_URL  = HTTP + HOST + "/home";
const FILES_URL  = HTTP + HOST + "/files";
const ECHO_URL  = HTTP + HOST + "/echo";
const GUEST_DEMO_URL  = HTTP + HOST + "/guest/demo";


function action(url){
    location.href = url;
}

function utf8ArrayToString(aBytes) {
    var sView = "";
    for (var nPart, nLen = aBytes.length, nIdx = 0; nIdx < nLen; nIdx++) {
        nPart = aBytes[nIdx];
        sView += String.fromCharCode(
            nPart > 251 && nPart < 254 && nIdx + 5 < nLen ? /* six bytes */
                /* (nPart - 252 << 30) may be not so safe in ECMAScript! So...: */
                (nPart - 252) * 1073741824 + (aBytes[++nIdx] - 128 << 24) + (aBytes[++nIdx] - 128 << 18) + (aBytes[++nIdx] - 128 << 12) + (aBytes[++nIdx] - 128 << 6) + aBytes[++nIdx] - 128
            : nPart > 247 && nPart < 252 && nIdx + 4 < nLen ? /* five bytes */
                (nPart - 248 << 24) + (aBytes[++nIdx] - 128 << 18) + (aBytes[++nIdx] - 128 << 12) + (aBytes[++nIdx] - 128 << 6) + aBytes[++nIdx] - 128
            : nPart > 239 && nPart < 248 && nIdx + 3 < nLen ? /* four bytes */
                (nPart - 240 << 18) + (aBytes[++nIdx] - 128 << 12) + (aBytes[++nIdx] - 128 << 6) + aBytes[++nIdx] - 128
            : nPart > 223 && nPart < 240 && nIdx + 2 < nLen ? /* three bytes */
                (nPart - 224 << 12) + (aBytes[++nIdx] - 128 << 6) + aBytes[++nIdx] - 128
            : nPart > 191 && nPart < 224 && nIdx + 1 < nLen ? /* two bytes */
                (nPart - 192 << 6) + aBytes[++nIdx] - 128
            : /* nPart < 127 ? */ /* one byte */
                nPart
        );
    }
    return sView;
}

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