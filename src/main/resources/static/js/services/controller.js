var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.MouseController = function (master) {
        this.SERVICEREQUEST = [
            {
                name: "canvas",
                callback: this.initCanvas.bind(this)
            },
            {
                name: "designer",
                callback: this.setInterface.bind(this)
            }
        ];

        this.master = master;
        this.id = master.getNextId();

        this.canvas = undefined;
        this.canvasOffset = {x: 0, y: 0};

        this.interfaceService = undefined;


        this.mouseDown = false;
        this.leftBtn = false;
        this.rightBtn = false;

        this.lastMouse = {x: 0, y: 0};

        this.distance = 0;

        return this;
    };

    xSSSR.MouseController.prototype = {

        setInterface: function (service) {
            this.interfaceService = service;
        },

        initCanvas: function (canvas) {
            this.canvas = canvas;
            var corner = canvas.getBoundingClientRect();
            this.canvasOffset = {x: corner.left, y: corner.top};
            this.enable();
        },

        enable: function () {
            document.onmouseup = this.handleMouseUp.bind(this);
            if (this.canvas) {
                this.canvas.onmousedown = this.handleMouseDown.bind(this);
                this.canvas.onmousemove = this.handleMouseMove.bind(this);
            }
        },

        disable: function () {
            document.onmouseup = null;
            if (this.canvas) {
                this.canvas.onmousedown = null;
                this.canvas.onmousemove = null;
            }
        },

        handleMouseDown: function (e) {
            switch (e.which) {
                case 1:
                    this.leftBtn = true;
                    break;
                case 3:
                    this.rightBtn = true;
                    break;
                default:
                    break;
            }

            this.mouseDown = true;
            this.trackMouseData(e);

            if (e.preventDefault) {
                e.preventDefault()
            } else {
                return false;
            }
        },

        handleMouseUp: function (e) {
            this.mouseDown = false;
            this.leftBtn = false;
            this.rightBtn = false;
            this.sendMouseData();

            if (e.preventDefault) {
                e.preventDefault()
            } else {
                return false;
            }
        },

        handleMouseMove: function (e) {
            var diff = Math.abs(this.lastMouse.x - e.clientX) + Math.abs(this.lastMouse.y - e.clientY);
            this.distance += diff;

            this.trackMouseData(e);
        },

        trackMouseData: function (event) {
            this.lastMouse = {x: event.clientX - this.canvasOffset.x, y: event.clientY - this.canvasOffset.y};
            this.sendMouseData();
        },

        sendMouseData: function () {
            this.interfaceService.trackMouse(this.lastMouse.x, this.lastMouse.y, this.leftBtn, this.rightBtn, this.mouseDown);
        }
    };

}());