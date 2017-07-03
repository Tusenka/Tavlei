var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.UnitFactory = function (config) {
        this.config = config.getOptionGroup("map");

        this.designs = {};
        return this;
    };

    xSSSR.UnitFactory.prototype = {
        design: function (name, parameters) {
            try {
                this.designs[name] = new xSSSR.BoardUnit(parameters.type, parameters.side);
            } catch (e) {
                alert(e);
            }
        },

        spawn: function (name) {
            if (!(name in this.designs)) {
                throw "Unit Factory: Unknown unit design " + name;
            }

            return this.designs[name].clone();
        },

        isNoble: function (unit) {
            return unit.type === this.config.unitRank.NOBLE;
        },

        isPrince: function (unit) {
            return (
                (this.isNoble(unit))
                && (unit.side === this.config.side.DEFENDER)
            );
        },

        isWarrior: function (unit) {
            return unit.type === this.config.unitRank.WARRIOR;
        },

        isViking: function (unit) {
            return (
                (this.isWarrior(unit))
                && (unit.side === this.config.side.ATTACKER)
            );
        },

        isWarden: function (unit) {
            return (
                (this.isWarrior(unit))
                && (unit.side === this.config.side.DEFENDER)
            );
        }
    };

}());