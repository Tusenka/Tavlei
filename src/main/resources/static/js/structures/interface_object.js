var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.InterfaceObject = function (area, pattern, zindex, text) {
        this.area = area;
        this.pattern = pattern;
        this.zindex = zindex;
        this.properties = {"text": text, "hovered": false, "visible": true};

        return this;
    };

    xSSSR.InterfaceObject.prototype = {

        show: function () {
            this.properties.visible = true;
        },
        hide: function () {
            this.properties.visible = false;
        },

        text: function () {
            return this.properties.text;
        },
        isHovered: function () {
            return this.properties.hovered;
        },
        isVisible: function () {
            return this.properties.visible;
        }

    };
}());