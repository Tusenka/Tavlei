var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.BoardCell = function (floor) {
        this.floor = floor;
        this.unit = false;
        return this;
    };

    xSSSR.BoardCell.prototype = {
        clone: function () {
            return new xSSSR.BoardCell(this.floor);
        }

    };

}());