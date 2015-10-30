
var Container  = extend(Java.type("java.util.concurrent.atomic.AtomicInteger"), {
    set: function(val){
        this.super.set(val);
    },

    get: function(){
        return this.super.get();
    }

});

app.root.onget = function(request) {
    var c = new Container();
    var y = new Container();
    var x = new Container();

    if(c.get() != 0){
        request.respond(2.05, "failed 0");
        return;
    }
    c.set(99);
    if(c.get() != 99){
        request.respond(2.05, "failed 1");
        return;
    }
    c.set(999);
    if(c.get() != 999){
        request.respond(2.05, "failed 2");
        return;
    }

    if(x.get() != 0){
        request.respond(2.05, "failed");
        return;
    }
    x.set(4);
    if(x.get() != 4){
        request.respond(2.05, "failed 3");
        return;
    }

    if(c.get() != 999){
        request.respond(2.05, "failed 4");
        return;
    }
    if(y.get() != 0){
        request.respond(2.05, "failed");
        return;
    }
    request.respond(2.05, "OK");
};