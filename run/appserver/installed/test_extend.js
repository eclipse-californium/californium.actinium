var mapper = {};
var dev = {

    set: function(val){
        dump(this);
        this.val = val;
    },

    get: function(){
        dump(this);
        return this.val;
    }

};
var Container = extend(Java.type("java.lang.Object"), dev);

app.root.onget = function(request) {
    var c = new Container();
    var y = new Container();
    var x = new Container();

    //if(c.get() != -1){
    //    request.respond(2.05, "failed");
    //    return;
    //}
    c.set(99);
    if(c.get() != 99){
        request.respond(2.05, "failed");
        return;
    }
    c.set(999);
    if(c.get() != 999){
        request.respond(2.05, "failed");
        return;
    }

    //if(x.get() != -1){
    //    request.respond(2.05, "failed");
    //    return;
    //}
    x.set(99);
    if(x.get() != 99){
        request.respond(2.05, "failed");
        return;
    }
    //if(y.get() != -1){
    //    request.respond(2.05, "failed");
    //    return;
    //}
    request.respond(2.05, "OK");
};