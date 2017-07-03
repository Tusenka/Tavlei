var GameMechanic = GameMechanic || {};

GameMechanic.GameMechanicResponse = function () {
};
GameMechanic.GameMechanicResponse.prototype = {

    move: null,//ExpectedType=Move
    winner: null,//ExpectedType=Side
    isStalemate: null,//ExpectedType=Boolean
    message: null,//ExpectedType=String
    yourSide: null,//ExpectedType=Side
    type: null,//ExpectedType=ResponseType
};
GameMechanic.ObjectFactory = function () {
};
GameMechanic.ObjectFactory.prototype = {};
GameMechanic.GameMechanicRequest = function () {
};
GameMechanic.GameMechanicRequest.prototype = {

    move: null,//ExpectedType=Move
    mySide: null,//ExpectedType=Side
    type: null,//ExpectedType=RequestType
    mode: null,//ExpectedType=GameModeType
};
GameMechanic.Move = function () {
};
GameMechanic.Move.prototype = {

    start: null,//ExpectedType=Position
    destination: null,//ExpectedType=Position
    defeated: null,//ExpectedType=ArrayList
};
GameMechanic.Position = function () {
};
GameMechanic.Position.prototype = {

    row: null,//ExpectedType=int
    col: null,//ExpectedType=int
};