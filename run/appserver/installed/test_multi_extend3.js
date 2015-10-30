var Container = extend(Java.type("java.lang.Object"), {
	get1: function(){
      return "A";
    }
});

var Container2 = extend(Container, {
	get2: function(){
      return this.super.get1() + " B";
    }
});

var Container3 = extend(Container2, {
	get3: function(){
      return this.super.get2() + " C";
    }
});


app.root.onget = function(request) {
    var a = new Container();
    var b = new Container2();
    var c = new Container3();
    if(a.get1() != "A"){
        request.respond(ResponseCode.BAD_REQUEST, a.get1());
        return;
    } 
    if(b.get2() != "A B"){
        request.respond(ResponseCode.BAD_REQUEST, b.get2());
        return;
    } 
    if(c.get3() != "A B C"){
        request.respond(ResponseCode.BAD_REQUEST, c.get3());
        return;
    } 
    request.respond(ResponseCode.CONTENT, "OK");
};