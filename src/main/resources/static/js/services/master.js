var xSSSR = xSSSR || {};

(function () {

    xSSSR.GrandMaster = function () {
        if (arguments.callee._singletonInstance) {
            return arguments.callee._singletonInstance;
        }
        this.SEARCHLOCATION = "SERVICEREQUEST";
        this.AFTERALL = "AFTERALL";

        this.slaveArmy = {};	// Key -> singleton object
        this.waitingList = {};	// Key -> vector of callbacks waiting for Key to appear
        this.readyToWork = false;
        this.lastID = 0;

        arguments.callee._singletonInstance = this;
        return this;
    };
    xSSSR.getGrandMaster = function () {
        return new xSSSR.GrandMaster();
    };
    xSSSR.GrandMaster.getSlave = function (name) {
        return new xSSSR.getGrandMaster().getSlave(name);
    };
    xSSSR.GrandMaster.prototype = {

        _add: function (name, object) {
            if (name === this.AFTERALL) {
                xSSSR.logException("GM: Keyword " + this.AFTERALL + " is reserved for system use");
            }

            this.slaveArmy[name] = object;
        },

        addResource: function (name, res) {
            this._add(name, res);
            this._serve(name, res);
        },

        addSlave: function (name, slave) {
            // More slaves for my army!
            this._add(name, slave);

            // Provide the new slave with everything he wants
            if (this.SEARCHLOCATION in slave) {
                var requestList = slave[this.SEARCHLOCATION];

                var request;
                for (var key in requestList) {
                    request = requestList[key];
                    if ((request == undefined) || (request.name == undefined) || (request.callback == undefined)) {
                        continue;
                    }

                    this._provideWith(request.name, request.callback);
                }
            }

            // Give the new slave to everyone else
            this._serve(name, slave);
        },

        _provideWith: function (name, callback) {
            if (!(name in this.slaveArmy)) {
                this.waitingList[name] = this.waitingList[name] || [];
                this.waitingList[name].push(callback);
            } else {
                callback(this.slaveArmy[name]);
            }
        },

        _serve: function (name, object) {
            // Give old slaves what they waiting for
            var probablyCallback;
            var servingQue = this.waitingList[name] || [];
            for (var key in servingQue) {
                probablyCallback = servingQue[key];
                probablyCallback(object);
            }

            this.waitingList[name] = [];
        },

        getSlave: function (name) {
            if (!(name in this.slaveArmy)) {
                var msg = "GM: I do not know such Slave service <" + name + ">";
                xSSSR.logException(msg);
            }
            return this.slaveArmy[name];
        },

        wellDone: function () {
            this.readyToWork = false;

            var waiterCount = 0;
            var waitingFor = [];

            for (var key in this.waitingList) {
                if (key === this.AFTERALL)
                    continue;

                if (this.waitingList[key].length > 0) {
                    waiterCount += this.waitingList[key].length;
                    waitingFor.push(key);
                }
            }

            if (waiterCount > 0) {
                var msg = "GM: " + waiterCount + " dependencies weren't resolved! I.e: " + waitingFor.toString();
                xSSSR.logException(msg);
            }

            // Fire all possible AFTERALL callbacks. 
            // At this point in time either everything is fine and ready
            // ... either we should get Exception above
            var onFinish = this.waitingList[this.AFTERALL];
            var onFinishCallback;
            for (var i = 0; i < onFinish.length; ++i) {
                onFinishCallback = onFinish[i];
                onFinishCallback();
            }

            this.readyToWork = true;
        },

        getNextId: function () {
            this.lastID++;
            return this.lastID;
        }
    };

}());