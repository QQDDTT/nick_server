<!doctype html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>files Test</title>
  </head>
  <body>
    <a href="/home">HOME</a>
    <div id="status"></div>
    <button onclick="send('FilesConsole_read')">READ</button>
    <button onclick="disconnect()">CLOSE</button>
    <button onclick="connect()">CONNECT</button>
    <div id="files"></div>
  </body>
  <script type="text/javascript">
    var ws = null;
    function connect() {
        ws = new WebSocket("ws://" + location.host + "/FilesConsole_path");
        ws.onopen = function () {
            document.getElementById("status").innerText = "创建链接成功, 开始发送消息";
        };
        ws.onmessage = function (event) {
            document.getElementById("files").innerText = event.data;
        };
        ws.onclose = function () {
            document.getElementById("status").innerText = "链接断开, 重连";
            setTimeout(function () {
                if(ws != null){
                    connect();
                }
            }, 1000);
        };
    }
    function send(msg){
        if (ws != null) {
            ws.send(msg);
        }else{
            connect();
            ws.send(msg);
        }
    }
    function disconnect() {
        if (ws != null) {
            ws.close();
            ws = null;
        }
    }
    </script>
</html>