const CONNECT_STATUS = "创建链接成功, 开始发送消息";
const RECOVER_STATUS = "链接断开, 重连...";
const CLOSE_STATUS = "链接已断开";
const REFLESH_TIME = 1000;

var ChatWS = (function(){
    var ws;
    var loopSend;
    var recover;

    function ChatWS(getSendMsgFunc, showDataFunc, statusFunc) {
        if (typeof getSendMsgFunc !== "function" || typeof showDataFunc !== "function") {
            console.error("Arguments must be function!");
            return;
        }
        this.getSendMsgFunc = getSendMsgFunc;
        this.showDataFunc = showDataFunc;
        this.statusFunc = typeof statusFunc === "function" ? statusFunc : function(status) {
            console.info("[STATUS] " + status);
        };
        this.connect();
    }

    ChatWS.prototype.connect = function() {
        var self = this;
        self.statusFunc(CONNECT_STATUS);
        self.ws = new WebSocket(CHAT_CONNECT);

        self.ws.onopen = function() {
            console.log('[连接已建立]');
            self.loopSend = setInterval(function() {
                var msg = self.getSendMsgFunc();
                if (msg) {
                    self.ws.send(msg);
                } else {
                    console.warn('[警告] 未发送消息');
                }
            }, REFLESH_TIME);
        };

        self.ws.onmessage = function(event) {
            console.log('[服务器响应]', event.data);
            self.showDataFunc(event.data);
        };

        self.ws.onclose = function() {
            self.statusFunc(RECOVER_STATUS);
            clearInterval(self.loopSend);
            self.recover = setTimeout(function() {
                self.connect();
            }, REFLESH_TIME);
        };

        self.ws.onerror = function(error) {
            console.log('[错误]', error);
            // Optionally trigger a reconnection attempt
            clearInterval(self.loopSend);
            self.recover = setTimeout(function() {
                self.connect();
            }, REFLESH_TIME);
        };
    };

    ChatWS.prototype.disconnect = function() {
        var self = this;
        self.statusFunc(CLOSE_STATUS);
        clearInterval(self.loopSend);
        clearTimeout(self.recover);
        self.ws.onclose = function(event) {
            console.log('[连接已断开]', event);
        };
        self.ws.onopen = null;
        self.ws.close();
    };

    return ChatWS;
})();
