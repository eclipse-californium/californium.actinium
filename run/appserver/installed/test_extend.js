var mapper = {};
var dev = {
    val: -1,
    set: function(val){
        this.val = val;
    },

    get: function(){
        return this.val;
    }

};
var Container = extend(Java.type("java.lang.Object"), dev);

app.root.onget = function(request) {
    var c = new Container();
    var y = new Container();
    var x = new Container();

    if(c.get() != -1){
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

    if(x.get() != -1){
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
    if(y.get() != -1){
        request.respond(2.05, "failed");
        return;
    }
    request.respond(2.05, "OK");
};