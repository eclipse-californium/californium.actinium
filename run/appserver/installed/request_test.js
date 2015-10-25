var uri = 'coap://localhost/'; // set via POST
var method = 'GET'; // set via POST
var result = '0';
var async = true;
var abort = false;
app.dump('Set RTT URI via POST');

function pollNode() {
    var client = new CoapRequest();
    client.timeout = 1000;
    app.dump("Async: "+async);
    client.open(method, uri, async);
    client.onload = function(){
        result = 'OK';
    };
    client.ontimeout = function(){
        result = 'TIMEOUT';
    };
    client.send('');
    if(abort){
        client.abort();
    }
}

app.root.onget = function(request) {
    request.respond(ResponseCode.CONTENT, result);
}

app.root.onpost = function(request) {
    var value = request.requestText.split(' ');
    if (value.length != 3 && value.length != 4) {
        request.respond(128);
        return
    }
    uri = value[1];
    method = value[0];
    async = value[2]=='true';
    abort = value[3]=='true';
    app.dump('RTT URI: ' + uri);
    app.dump('abort: ' + abort);
    pollNode();
    app.dump('Done');
    request.respond(ResponseCode.CHANGED);
}
