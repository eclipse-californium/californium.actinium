
var Container  = extend(Java.type("java.util.concurrent.atomic.AtomicInteger"), {
    set1: function(val){
        this.set(val);
    },

    get1: function(){
        return this.get();
    }

});

app.root.onget = function(request) {
    var c = new Container();
    var y = new Container();
    var x = new Container();

    if(c.get1() != 0){
        request.respond(2.05, "failed 0");
        return;
    }
    c.set1(99);
    if(c.get1() != 99){
        request.respond(2.05, "failed 1");
        return;
    }
    c.set1(999);
    if(c.get1() != 999){
        request.respond(2.05, "failed 2");
        return;
    }

    if(x.get1() != 0){
        request.respond(2.05, "failed");
        return;
    }
    x.set1(4);
    if(x.get1() != 4){
        request.respond(2.05, "failed 3");
        return;
    }

    if(c.get1() != 999){
        request.respond(2.05, "failed 4");
        return;
    }
    if(y.get1() != 0){
        request.respond(2.05, "failed");
        return;
    }
    request.respond(2.05, "OK");
};