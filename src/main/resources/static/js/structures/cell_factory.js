var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.CellFactory = function (config) {
        this.config = config.getOption("board", "floor");

        this.designs = {};
        return this;
    };

    xSSSR.CellFactory.prototype = {
        design: function (name, parameters) {
            try {
                this.designs[name] = new xSSSR.BoardCell(parameters.floor);
            } catch (e) {
                alert(e);
            }
        },

        spawn: function (name) {
            if (!(name in this.designs)) {
                throw "Cell Factory: Unknown cell design " + name;
            }

            return this.designs[name].clone();
        },

        isPassable: function (cell) {
            return cell.floor != this.config.DISABLED;
        },

        isOrdinary: function (cell) {
            return cell.floor == this.config.NORMAL;
        },

        isGoal: function (cell) {
            return cell.floor == this.config.EXIT;
        },

        isThrone: function (cell) {
            return cell.floor == this.config.THRONE;
        },

        isSpecial: function (cell) {
            return ( this.isGoal(cell) || this.isThrone(cell) );
        }
    };

}());