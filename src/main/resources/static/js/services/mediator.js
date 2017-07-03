var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.ProxyService = function (master, config) {
        this.SERVICEREQUEST = [];

        this.master = master;
        this.id = master.getNextId();

        this.config = config;
        this.options = this.config.getOptionGroup("server");
        this.serverAddress = this.options.gateway;

        this.content = {};

        return this;
    };

    xSSSR.ProxyService.prototype = {

        setCallbacks: function (struct) {
            this.callbacks = struct;
        },

        _tell: function (params) {
            var url = this._makeUrl(this.serverAddress, params);
            this._sendRequest(url, false);
        },

        _ask: function (params, callback) //expect answer
        {
            var url = this._makeUrl(this.serverAddress, params);
            this._sendRequest(url, callback);
        },

        _sendRequest: function (url, callback) {
            var xmlhttp = new XMLHttpRequest();
            xmlhttp.open("GET", url, true);
            xmlhttp.onreadystatechange = function () {
                if (xmlhttp.readyState == 4) {
                    if (xmlhttp.status == 200) {
                        if (callback) {
                            callback(JSON.parse(xmlhttp.responseText));
                        }
                    } else {
                        throw "Error " + xmlhttp.status + " while loading " + url + ". Something gone wrong: status " + xmlhttp.readyState + "; " + xmlhttp.responseText;
                    }
                }
            };
            xmlhttp.send();
        },

        _makeUrl: function (baseUrl, params) {
            var query = [];
            for (var key in params) {
                query.push(encodeURIComponent(key) + '=' + encodeURIComponent(params[key]));
            }
            var result = "" + baseUrl + '?' + query.join('&');
            return result;
        },

        // API to Coordinator
        process: function () {
            // Retrive opponents turn from server and call coordinator back
        },

        select: function () {
            // shortcut for Turn
        },

        deselect: function () {
            // shortcut for Turn
        },

        move: function () {
            // shortcut for Turn
        },

        find: function () {
            var params = {
                user: this.userId,

            };
        },

        create: function () {
        },

        connect: function () {
        },

        disconnect: function () {
        },

        join: function () {
        },

        turn: function () {
        },

        remind: function () {
        },

        say: function () {
        },

        listen: function () {
        }

    };

}());