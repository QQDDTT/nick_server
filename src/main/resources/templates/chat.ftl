<!doctype html>
<html lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>echo Test</title>
  </head>
  <body>
    <a href="/home">HOME</a>
    <button onclick="connect()">CONNECT</button>
    <button onclick="disconnect()">CLOSE</button>
    <input type="text" id="hello" value="PING"/>
    <div id="msg"></div>
    <div id="status"></div>
  </body>
  <script type="text/javascript">
    var ws = null;
    var meter,myVar;
    function connect() {
        ws = new WebSocket("ws://" + location.host + "/echo");
        ws.onopen = function () {
            document.getElementById("status").innerText = "创建链接成功, 开始发送消息";
            setInterval(function () {
                if (ws != null){
                    ws.send(document.getElementById("hello").value);
                }
            }, 1000);
        };
        ws.onmessage = function (event) {
            document.getElementById("msg").innerText = event.data;
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
    function disconnect() {
        if (ws != null) {
            ws.close();
            ws = null;
        }
    }
    connect();
</script>
</html>