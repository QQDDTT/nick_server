const FILE_WS = new WebSocket(CHAT_CONNECT);
const CONNECT_STATUS = "创建链接成功, 开始发送消息";
const RECOVER_STATUS = "链接断开, 重连..."
const CLOSE_STATUS = "链接已断开"
const REFLESH_TIME = 1000;

function connect(getSendMsgFunc,showDataFunc,statusFunc) {
    if(typeof getSendMsgFunc != "function" || typeof showDataFunc != "function"){
        console.error("Arguments must be function !");
        return;
    }
    if(typeof statusFunc != "function"){
        statusFunc = function(status){
            console.info("[STATUS]" + status);
        };
    }
    FILE_WS.onopen = function () {
        statusFunc(CONNECT_STATUS);
        setInterval(function () {
            FILE_WS.send(getSendMsgFunc());
        }, REFLESH_TIME);
    };
    FILE_WS.onmessage = function (event) {
        showDataFunc(event.data);
    };
    FILE_WS.onclose = function () {
        statusFunc(RECOVER_STATUS);
        setTimeout(function () {
            connect(getSendMsgFunc,showDataFunc,statusFunc);
        }, REFLESH_TIME);
    };
}
function disconnect(statusFunc) {
    if(typeof statusFunc != "function"){
        statusFunc = function(status){
            console.info("[STATUS]" + status);
        };
    }
    FILE_WS.onclose = function (){
        setTimeout(function () {
            statusFunc(CLOSE_STATUS);
        }, REFLESH_TIME);
    }
    FILE_WS.close();
}

