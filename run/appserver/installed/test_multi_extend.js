var mapper = {};
var dev = {

    set: function(val){
        this.val = val;
    },

    get: function(){
        return this.val;
    }

};
var Container = extend(Java.type("java.lang.Object"), dev);
var Container2 = extend(Container, {
    dumbo:function(){
        return this.get();
    }
});

app.root.onget = function(request) {
    var c = new Container();
    var y = new Container2();
    var x = new Container2();

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
    dump(x.dumbo());
    if(x.dumbo() != 99){
        request.respond(2.05, "failed");
        return;
    }
    //if(y.get() != -1){
    //    request.respond(2.05, "failed");
    //    return;
    //}
    request.respond(2.05, "OK");
};