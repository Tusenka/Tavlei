var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.EventManager = function (master, config) {
        this.SERVICEREQUEST = [];

        this.master = master;
        this.id = this.master.getNextId();

        this._events = {};

        return this;
    };

    xSSSR.EventManager.prototype = {

        listen: function (name, callBack) {
            var events = this._events;
            var callBacks = events[name] = events[name] || [];
            callBacks.push(callBack);
        },

        fire: function (name, args) {
            var callBacks = this._events[name] || [];
            var argArray = args || [];

            if (callBacks.length == 0) {
                xSSSR.logMessage("EM: No listeners for fired event " + name);
            }
            for (var i = 0; i < callBacks.length; i++) {
                this._call(callBacks[i], argArray);
            }
        },

        _call: function (expectedCallback, args) {
            if (xSSSR.isFunction(expectedCallback)) {
                expectedCallback.apply(undefined, args);
            } else {
                xSSSR.logException("EM: Incorrect callback " + expectedCallback + "(" + args + ")");
            }
        }
    }
}());