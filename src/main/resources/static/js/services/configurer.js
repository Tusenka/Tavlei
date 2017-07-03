var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.ConfigService = function (master) {
        this.SERVICEREQUEST = [];

        this.master = master;
        this.id = master.getNextId();

        this.options = {};

        return this;
    };

    xSSSR.ConfigService.prototype = {

        init: function (domainList) {
            var value;
            for (var key in domainList) {
                value = domainList[key];
                this.options[value] = {};
            }
        },

        load: function (dictionary) {
            this.options = dictionary;
        },

        setOptionGroup: function (domain, group) {
            if (!domain in this.options) {
                this.options[domain] = {};
            }

            var value;
            for (var key in group) {
                value = group[key];
                this._setOption(domain, key, value);
            }
        },

        setOption: function (domain, option, value) {
            if (!(domain in this.options)) {
                this.options[domain] = {};
            }

            this._setOption(domain, option, value);
        },

        _setOption: function (domain, option, value) {
            this.options[domain][option] = value;
        },

        getOption: function (domain, option) {
            var domainOptions = this.getOptionGroup(domain);

            if (!(option in domainOptions)) {
                throw "Config: no such option <" + option + "> in domain <" + domain + ">";
            }

            return domainOptions[option];
        },

        getOptionGroup: function (domain) {
            if (!(domain in this.options)) {
                throw "Config: no such domain <" + domain + ">";
            }

            return this.options[domain];
        }

    };

}());