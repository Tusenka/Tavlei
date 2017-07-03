/**
 * Created by Irina on 26.12.2016.
 */
var xSSSR = xSSSR || {};
xSSSR.Position = function (col, row) {
    this.col = col;
    this.row = row;
};

xSSSR.PositionFromHash = function (cellId) {

    var raw_point = xSSSR.getBoard().cellIdToPoint(cellId);
    return new xSSSR.Position(raw_point.x - 1, raw_point.y - 1)
};
xSSSR.toPosition = function (p) {
    if ("row" in p && "col" in p) return p;
    if ("x" in p && "y" in p) return new xSSSR.Position(p.x, p.y);
    throw new Error("InvalidArgumentException Argument must have either x or col value");

};
xSSSR.sideAdapter = function (intSide) {
    if (intSide == settings.map.side.DEFENDER) return GameMechanic.Side.WHITE;
    else  return GameMechanic.Side.BLACK;
};
xSSSR.sideToString = function (xsdSide) {
    if (xsdSide == GameMechanic.Side.WHITE) return "Wardens";
    else  return "Vikings";
};
xSSSR.sideToJsSide = function (xsdSide) {
    if (xsdSide == GameMechanic.Side.WHITE) return settings.map.side.DEFENDER;
    else   return settings.map.side.BLACK;
};
xSSSR.Move = function (startPosition, endPosition) {
    this.start = startPosition;
    this.destination = endPosition;
};
xSSSR.convertObject = function (objFrom, prototypeTo, propertyMap, valueMap) {
    propertyMap = propertyMap || {};
    valueMap = valueMap || {};
    for (var propertyName in prototypeTo) {
        var propertyTo = (propertyName in propertyMap) ? propertyMap[propertyName] : propertyName,
            valueFrom = objFrom[propertyName];
        if (!(propertyName in prototypeTo)) {
            continue;
        }
        if (propertyTo in valueMap) {
            prototypeTo[propertyTo] = valueMap[prototypeTo](valueFrom);
        }
        else {
            prototypeTo[propertyTo] = valueFrom
        }
    }
    return prototypeTo;
};
xSSSR.convertToPrototype = function (objFrom, prototypeTo, propertyMap, valueMap) {
    propertyMap = propertyMap || {};
    valueMap = valueMap || {};
    for (var propertyName in prototypeTo) {
        var propertyTo = (propertyName in propertyMap) ? propertyMap[propertyName] : propertyName,
            valueFrom = objFrom[propertyName];
        if (!(propertyName in prototypeTo)) {
            continue;
        }
        if (propertyTo in valueMap) {
            prototypeTo[propertyTo] = valueMap[prototypeTo](valueFrom);
        }
        else {
            prototypeTo[propertyTo] = valueFrom
        }
    }
    return prototypeTo;
};



