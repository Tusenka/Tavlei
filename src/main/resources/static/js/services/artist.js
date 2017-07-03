var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.DrawingService = function (master, config) {
        this.SERVICEREQUEST = [
            {
                name: "canvas",
                callback: this.initCanvas.bind(this)
            },
            {
                name: "overview",
                callback: this.initOverview.bind(this)
            }
        ];

        this.master = master;
        this.id = this.master.getNextId();

        this.config = config;
        this.constants = this.config.getOptionGroup("drawing");

        this.canvas = undefined;
        this.canvasCorner = undefined;
        this.overviewCanvas = undefined;

        this.resources = {};
        this.size = {w: 512, h: 512};
        this.BUSY = false;

        this.initGraphics();

        return this;
    };

    xSSSR.DrawingService.prototype = {

        initGraphics: function () {
            var sources = this.constants.externalResources;
            for (var key in sources) {
                this.loadImage(key, sources[key]);
            }
        },

        loadImage: function (name, src) {
            this.resources[name] = new Image();
            this.resources[name].src = src;
        },

        initCanvas: function (htmlCanvas) {
            this.canvas = htmlCanvas;
            this.context = htmlCanvas.getContext("2d");
            this.size = {w: htmlCanvas.width, h: htmlCanvas.height};
            this.canvasCorner = htmlCanvas.getBoundingClientRect();

            this.context.globalCompositeOperation = "source-over";

            //this.context.font = "30px Arial";
            //this.context.fillText("Hello World", 5, 30);	
        },

        initOverview: function (htmlCanvas) {
            this.overviewCanvas = htmlCanvas;
            this.overviewContext = htmlCanvas.getContext("2d");
            this.overviewSize = {w: htmlCanvas.width, h: htmlCanvas.height};
            this.overviewCanvasCorner = htmlCanvas.getBoundingClientRect();

            this.overviewContext.globalCompositeOperation = "source-over";

            this.drawDebugText("Overview context loaded!");
        },

        connectAreas: function (source, sink) {
            // TODO
        },

        clearArea: function (area) {
            this.context.clearRect(area.x1, area.y1, area.w, area.h);
        },

        drawArea: function (area, style) {
            this.drawPaddedArea(area, style, 0);
        },

        drawPaddedArea: function (area, style, shift) {
            if (!style)
                return;

            if (style in this.resources) {
                this._drawResourceAreaPadded(area, style, shift);
            } else {
                this._drawPaddedArea(area, style, shift);
            }
        },

        _drawPaddedArea: function (area, style, shift) {
            this.context.fillStyle = style;
            this.context.fillRect(area.x1 + shift, area.y1 + shift, area.w - 2 * shift, area.h - 2 * shift);
        },

        drawAreaBorder: function (area, style, size) {
            this.context.strokeStyle = style;
            this.context.lineWidth = size;
            this.context.strokeRect(area.x1, area.y1, area.w, area.h);
        },


        drawAreaOutline: function (area, style, size) {
            this.context.strokeStyle = style;
            this.context.lineWidth = size;
            size -= 1;
            this.context.strokeRect(area.x1 - size, area.y1 - size, area.w + 2 * size, area.h + 2 * size);
        },

        drawAreaText: function (area, style, text) {
            this.drawAreaTextFont(area, style, text, "12px serif");
        },

        drawAreaTextFont: function (area, style, text, font) {
            var maxStringLength = this.constants["charactersInOneLine"];
            var txt = "" + text; 		// Ensure text is a string, not a number;
            var offset = {
                x: area.w * ( ((maxStringLength - txt.length - 1) / 2) / maxStringLength ),
                y: 2 * area.h / 3
            };
            this.drawTextInsideContext(area.x1 + offset.x, area.y1 + offset.y, style, txt, font, this.context);
        },

        drawDebugText: function (text) {
            this.overviewContext.clearRect(0, 0, this.overviewSize.w, this.overviewSize.h);
            this.drawTextInsideContext(5, 30, "#0000FF", text, "30px Arial", this.overviewContext);
        },

        drawTextInsideContext: function (x, y, style, text, font, context) {
            context.fillStyle = style;
            context.font = font;
            context.fillText(text, x, y);
        },

        drawResourceArea: function (area, resource) {
            if (!(resource in this.resources))
                throw "Painter: resource " + resource + " not found";

            this._drawResourceAreaPadded(area, resource, 0);
        },

        _drawResourceAreaPadded: function (area, resource, shift) {
            var image = this.resources[resource];
            this.context.drawImage(image, area.x1 + shift, area.y1 + shift, area.w - 2 * shift, area.h - 2 * shift);
        },

        drawAreaOutArrow: function (areaFrom, areaTo, style) {
            var size = 2;
            this.context.strokeStyle = style;
            this.context.lineWidth = size;

            var pointFrom = {x: areaFrom.x1 + (areaFrom.w / 2), y: areaFrom.y1 + areaFrom.h};
            var pointTo = {x: areaTo.x1 + (areaTo.w / 2), y: areaTo.y1};
            this.drawArrowhead(pointFrom, pointTo, size + 5);
        },

        drawArrowhead: function (p1, p2, size) {
            var ctx = this.context;
            ctx.save();

            // Rotate the context to point along the path
            var dx = p2.x - p1.x, dy = p2.y - p1.y, len = Math.sqrt(dx * dx + dy * dy);
            ctx.translate(p2.x, p2.y);
            ctx.rotate(Math.atan2(dy, dx));

            // line
            ctx.lineCap = 'round';
            ctx.beginPath();
            ctx.moveTo(0, 0);
            ctx.lineTo(-len, 0);
            ctx.closePath();
            ctx.stroke();

            // arrowhead
            ctx.beginPath();
            ctx.moveTo(0, 0);
            ctx.lineTo(-size, -size);
            ctx.lineTo(-size, size);
            ctx.closePath();
            ctx.fill();

            ctx.restore();
        }
    };

}());