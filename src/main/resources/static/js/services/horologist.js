var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.TimerService = function (master, config) {
        this.SERVICEREQUEST = [];

        this.master = master;
        this.id = master.getNextId();

        this.config = config;
        this.autoStart = config.getOption("system", "clockAutoStart");
        this.systemClock = config.getOption("system", "refresh");

        this._mainLoop = function () {
            alert("empty loop")
        };

        this.lastFrame = new Date().getTime();
        this.timeSince = {
            load: 0,
            start: 0,
            lastFrame: 0
        };
        this.timers = {};

        if (this.autoStart) {
            this.start();
        }

        return this;
    };

    xSSSR.TimerService.prototype = {
        // OBSOLETE METHOD
        setComandSequence: function (callback) {
            this._iteration = callback
        },

        register: function (name, period, callback) {
            var timer = {
                "name": name,
                "period": period,
                "counter": 0,
                "disabled": false,
                "callback": callback
            };

            this.timers[name] = timer;
        },

        play: function (timerName) {
            this._switch(timerName, false)
        },

        pause: function (timerName) {
            this._switch(timerName, true)
        },

        _switch: function (timerName, isDisabled) {
            if (timerName in this.timers) {
                this.timers[timerName].disabled = isDisabled;
            } else {
                xSSSR.logException("TM: unknown timer name " + timerName);
            }
        },

        start: function () {
            this._increaseMainLoop();
            this._mainLoop();
        },

        restart: function () {
            this.timeSince.start = 0;
            this._increaseMainLoop();
            this._mainLoop();
        },

        stop: function () {
            this._reduceMainLoop();
            this._mainLoop();
        },

        _reduceMainLoop: function () {
            this._mainLoop = this._shortGameLoop;
        },

        _increaseMainLoop: function () {
            this._mainLoop = this._fullGameLoop;
        },

        _fullGameLoop: function () {
            this._repeat();
            this._iteration();
        },

        _shortGameLoop: function () {
            this._iteration();
        },

        _repeat: function () {
            xSSSR.requestAnimationFrame(this._mainLoop.bind(this), this.systemClock);
        },

        _iteration: function () {
            var now = new Date().getTime();
            var timePassed = now - this.lastFrame;
            if (timePassed < 0) {
                xSSSR.logError("TM: negative time passed. WTF?!");
            }

            this.timeSince.load += timePassed;
            this.timeSince.start += timePassed;
            this.timeSince.lastFrame += timePassed;

            this._grantTime(timePassed);
            this._spentTime();

            this.lastFrame = now;
            this.timeSince.lastFrame = 0;
        },

        _grantTime: function (amount) {
            var timer;
            for (var key in this.timers) {
                timer = this.timers[key];

                if (timer.disabled) {
                    continue;
                }

                timer.counter += amount;
            }
        },

        _spentTime: function () {
            var timer;
            for (var key in this.timers) {
                timer = this.timers[key];

                if (timer.counter < timer.period) {
                    continue;
                }

                timer.counter -= timer.period;
                timer.callback();
            }
        }
    };

}());