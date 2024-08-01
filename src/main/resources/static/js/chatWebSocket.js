const CONNECT_STATUS = "创建链接成功, 开始发送消息";
const RECOVER_STATUS = "链接断开, 重连..."
const CLOSE_STATUS = "链接已断开"
const REFLESH_TIME = 1000;

var ChatWS = (function(){
    var ws;
    var loopSend;
    var recover;
    function ChatWS(getSendMsgFunc,showDataFunc,statusFunc){
        if(typeof getSendMsgFunc != "function" || typeof showDataFunc != "function"){
            console.error("Arguments must be function !");
            return;
        }
        ChatWS.prototype.getSendMsgFunc = function(){
            return getSendMsgFunc();
        }
        ChatWS.prototype.showDataFunc = function(msg){
            showDataFunc(msg);
        }
        if(typeof statusFunc != "function"){
            ChatWS.prototype.statusFunc = function(status){
                console.info("[STATUS]" + status);
            };
        }else{
            ChatWS.prototype.statusFunc = function(status){
                statusFunc(status);
            }
        }
        this.ws = new WebSocket(CHAT_CONNECT);
    };
    ChatWS.prototype.connect = function(){
        var this_this = this;
        this_this.statusFunc(CONNECT_STATUS);
        this_this.ws.onopen = function () {
            this_this.loopSend = setInterval(function () {
                this_this.ws.send(this_this.getSendMsgFunc());
            }, REFLESH_TIME);
        };
        this_this.ws.onmessage = function (event) {
            this_this.showDataFunc(event.data);
        };
        this_this.ws.onclose = function () {
            this_this.statusFunc(RECOVER_STATUS);
            this_this.recover = setTimeout(function () {
                this_this.connect();
            }, REFLESH_TIME);
        };
    };
    ChatWS.prototype.disconnect = function() {
        var this_this = this;
        this.statusFunc(CLOSE_STATUS);
        clearInterval(this_this.loopSend);
        clearTimeout(this_this.recover);
        this_this.ws.onclose = null;
        this_this.ws.onopen = null;
        this_this.ws.close();
    };
    return ChatWS;
})();




