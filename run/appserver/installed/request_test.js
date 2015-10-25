var uri = 'coap://localhost/'; // set via POST
var method = 'GET'; // set via POST
var result = '0';
app.dump('Set RTT URI via POST');

function pollNode() {
    var client = new CoapRequest();
    client.timeout = 1000;
    client.open(method, uri, true);
    client.onload = function(){
        result = 'OK';
    };
    client.ontimeout = function(){
        result = 'TIMEOUT';
    };
    client.send('');
}

app.root.onget = function(request) {
    request.accept();

    dump('RTT waiting...');

    request.respond(ResponseCode.CONTENT, result);
}

app.root.onpost = function(request) {
    var value = request.requestText.split(' ');
    if (value.length != 2 && value.length != 1) {
        request.respond(128);
        return
    }
    uri = value[value.length-1];
    if (value.length == 2) {
        method = value[0];
    }
    app.dump('RTT URI: ' + uri);
    pollNode();
    app.dump('Done');
    request.respond(ResponseCode.CHANGED);
}
