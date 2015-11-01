
var Container  = extend(Java.type("java.util.concurrent.atomic.AtomicInteger"), {
    hashCode: function(){
        return this.super.hashCode()%10;
    }

});


var Container2  = extend(Java.type("java.util.concurrent.atomic.AtomicInteger"), {
    hashCode2: function(){
        return this.super.hashCode()%10;
    }

});

app.root.onget = function(request) {
    var c = new Container();
    var y = new Container2();

    if(c.super$hashCode()%10 != c.hashCode()){
        request.respond(2.05, "failed 0 "+c.super$hashCode()+"--"+c.hashCode());
        return;
    }

    if(y.hashCode()%10 != y.hashCode2()){
        request.respond(2.05, "failed 1");
        return;
    }
    request.respond(2.05, "OK");
};