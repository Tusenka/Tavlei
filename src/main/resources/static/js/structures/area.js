var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.Area = function (name, x, y, width, height) {
        return {
            name: name,
            x1: x, x2: x + width, w: width,
            y1: y, y2: y + height, h: height,
            ul: {x: x, y: y},
            br: {x: x + width, y: y + height},
            center: {x: x + width / 2, y: y + height / 2},
            z: 0
        };
    };


}());