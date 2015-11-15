var CoapRequest = Java.type("org.eclipse.californium.actinium.jscoap.CoapRequest");
var JavaScriptStaticAccess = Java.type("org.eclipse.californium.actinium.plugnplay.JavaScriptStaticAccess");
var dump = JavaScriptStaticAccess.dump;
var setInterval = app.setInterval;
var clearInterval = app.clearInterval;
var setTimeout = app.setTimeout;
var clearTimeout = app.clearTimeout;
var ResponseCode = Java.type("org.eclipse.californium.core.coap.CoAP.ResponseCode");
var JavaScriptResource = Java.type("org.eclipse.californium.actinium.jscoap.JavaScriptResource");
var _packages = ["java.lang", "java.util", "java.io", "java.net", "java.text", "org.eclipse.californium.core.coap", "org.eclipse.californium.actinium.jscoap", "org.eclipse.californium.actinium.jscoap.jserror"];
var global = this;
// Emulate global package import
this.__noSuchProperty__ = function(name) {
    for (var i in _packages) {
        try {
            var type = Java.type(_packages[i] + "." + name);
            global[name] = type;
            return type;
        } catch (e) {}
    }
    if (this === undefined) {
        throw new ReferenceError(name + " is not defined");
    } else {
        return undefined;
    }
};
// Override JS Date function
var Date = java.util.Date;
function _copy(copy, obj) {
    for (var attr in obj) {
        if (obj.hasOwnProperty(attr)) {
            copy[attr] = obj[attr];
        }
    }
    return copy;
}
function _copy_with_scope(copy, obj, scope) {
    for (var attr in obj) {
        if (obj.hasOwnProperty(attr)) {
            if ((typeof obj[attr]) != "function") {
                scope[attr] = obj[attr];
            } else {
                copy[attr] = function(fn) {
                    return function() {
                        return fn.apply(scope, arguments);
                    };
                }(obj[attr]);
                copy[attr]._length = obj[attr].length;
            }
        }
    }
    return copy;
}
// Extend function with a java like behavior
var extend = function() {
    var cls, fn, clname;
    if(arguments.length == 2){
        cls = arguments[0];
        fn = arguments[1];
    }
    if(arguments.length == 3){
        clname = arguments[0];
        cls = arguments[1];
        fn = arguments[2];
        fn['getClassName'] = function(){
            return clname;
        }
    }
    var local_fn = _copy({}, fn);
    var parent_jsobj = function(data, contexts, container) {

        return new JSAdapter() {
            __call__: function(name) {
                var val = _super(data.self, name, Array.apply(null, arguments).slice(1));
                return val;
            },
            __get__: function(name) {
                return data.self[name];
            }
        };
    };
    if (cls._cls != undefined) {
        var base = _copy({}, cls._fn);
        parent_jsobj = cls._jsobj;
        cls = cls._cls;
    }
    var local_cls = cls;
    var _jsobj = function(data, contexts, container) {
        var context = {
            super: parent_jsobj(data, contexts, container)
        };
        contexts.push(context);
        var obj = {};
        _copy_with_scope(obj, local_fn, context);
        _copy(container, obj);
        Object.setPrototypeOf(obj, context.super);
        return obj;
    };
    var extended = new JSAdapter() {
        __get__: function (name) {
            if (name == '_cls') {
                return local_cls;
            } else if (name == "_jsobj") {
                return _jsobj;
            } else if (name == "new") {
                //return supplier
                return function (){
                    return new extended();
                };
            } else {
                return undefined;
            }
        },

        __call__: function (name, arg1, arg2) {
            // ignore calls
        },

        __new__: function() {
            var data = {
                self: null
            };
            var container = {};
            var contexts = [];
            _jsobj(data, contexts, container);
            var t = _extend(cls, container);
            var self = new t();
            var self_obj = {"__java":self};
            Object.bindProperties(self_obj, self);
            Object.setPrototypeOf(container, self_obj);
            for (var i = contexts.length - 1; i >= 0; i--) {
                Object.setPrototypeOf(contexts[i], self_obj);
            };
            data.self = self;
            return self;
        }
    };
    return extended;
};