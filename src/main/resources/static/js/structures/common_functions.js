var xSSSR = xSSSR || {};

( function () {
    "use strict";

    xSSSR.requestAnimFrame = (function () {
        return window.requestAnimationFrame ||
            window.webkitRequestAnimationFrame ||
            window.mozRequestAnimationFrame ||
            window.oRequestAnimationFrame ||
            window.msRequestAnimationFrame ||
            function (callback, period) {
                window.setTimeout(callback, period);
            };
    })();

    xSSSR.requestAnimationFrame = (function () {
        return function (callback, period) {
            window.setTimeout(callback, period);
        };
    })();

    xSSSR.arrayContains = function (needle, arrhaystack) {
        return (arrhaystack.indexOf(needle) > -1);
    };

    xSSSR.loadJSON = function (text) {
        // Load text with Ajax synchronously: takes path to file and optional MIME type
        function loadTextFileAjaxSync(filePath, mimeType) {
            var xmlhttp = new XMLHttpRequest();
            xmlhttp.open("GET", "http://tavlei.net/game/" + filePath, false);
            if (mimeType != null) {
                if (xmlhttp.overrideMimeType) {
                    xmlhttp.overrideMimeType(mimeType);
                }
            }
            xmlhttp.send();
            if ((xmlhttp.status == 200) || ( (xmlhttp.status == 0) && (xmlhttp.responseText) )) {
                return xmlhttp.responseText;
            } else {
                throw "Error " + xmlhttp.status + " while loading " + filePath + ". Something gone wrong: status " + xmlhttp.readyState + "; " + xmlhttp.responseText;
            }
        }

        var jsonObj = loadTextFileAjaxSync(name + ".json", "application/json");

        return JSON.parse(jsonObj);
    };

    xSSSR.isFunction = function (objectToCheck) {
        var getType = {};
        return objectToCheck && getType.toString.call(objectToCheck) === '[object Function]';
    };

    xSSSR.logMessage = function (message) {
        //console.log(message);
    };

    xSSSR.logError = function (message) {
        xSSSR.logMessage(message);
        alert("Error: " + message);
    };

    xSSSR.logException = function (message) {
        throw message;				// It's ok
        xSSSR.logError(message);	// Trust me
    };

    xSSSR.assert = function (condition, message) {
        if (!condition) {
            xSSSR.logException("Assertion failed: " + message);
        }
    }
}());
