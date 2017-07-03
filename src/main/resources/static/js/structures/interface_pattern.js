var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.InterfacePattern = function (area, styleNormal, styleHover, border, padding, text) {
        return {
            target: area,
            style: styleNormal,
            hovered: styleHover,
            border: border,
            padding: padding,
            text: text
        };
    };

}());