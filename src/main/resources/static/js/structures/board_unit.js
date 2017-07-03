var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.BoardUnit = function (type, side) {
        this.type = type;
        this.side = side;
        return this;
    };

    xSSSR.BoardUnit.prototype = {
        clone: function () {
            return new xSSSR.BoardUnit(this.type, this.side);
        },

        alliedWith: function (unit) {
            return this.side == unit.side;
        },

        enemyTo: function (unit) {
            return !this.alliedWith(unit);
        }
    };

}());