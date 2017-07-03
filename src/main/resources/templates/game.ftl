<!DOCTYPE html>
<html>
<head>
    <title>Оберег - The Ward: Varyags vs. Vikings</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
    <script src="../static/js/icheck/icheck.js"></script>

    <script src="http://cdn.jsdelivr.net/sockjs/1/sockjs.min.js"></script>
    <script src="../static/js/stomp.js"></script>
    <script src="../static/js/structures/Classes.js"></script>
    <script src="../static/js/structures/Enums.js"></script>

    <!-- site design dependencies -->
    <link href="../static/css/style.css" rel="stylesheet" media="screen"/>
    <link href="../static/js/icheck/skins/minimal/blue.css" rel="stylesheet">
    <!-- data structures-->
    <script type="text/javascript" src="../static/js/structures/area.js"></script>
    <script type="text/javascript" src="../static/js/structures/board.js"></script>
    <script type="text/javascript" src="../static/js/structures/board_cell.js"></script>
    <script type="text/javascript" src="../static/js/structures/board_unit.js"></script>
    <script type="text/javascript" src="../static/js/structures/cell_factory.js"></script>
    <script type="text/javascript" src="../static/js/structures/interface_object.js"></script>
    <script type="text/javascript" src="../static/js/structures/interface_pattern.js"></script>
    <script type="text/javascript" src="../static/js/structures/unit_factory.js"></script>

    <!-- engine  -->
    <script type="text/javascript" src="../static/js/structures/common_functions.js"></script>

    <!-- singleton classes -->
    <script type="text/javascript" src="../static/js/services/artist.js"></script>
    <script type="text/javascript" src="../static/js/services/chronicler.js"></script>
    <script type="text/javascript" src="../static/js/services/configurer.js"></script>
    <script type="text/javascript" src="../static/js/services/controller.js"></script>
    <script type="text/javascript" src="../static/js/services/coordinator.js"></script>
    <script type="text/javascript" src="../static/js/services/designer.js"></script>
    <script type="text/javascript" src="../static/js/services/horologist.js"></script>
    <script type="text/javascript" src="../static/js/services/mediator.js"></script>
    <script type="text/javascript" src="../static/js/services/game_controller.js"></script>
    <script type="text/javascript" src="../static/js/services/socket.js"></script>
    <!-- special one -->
    <script type="text/javascript" src="../static/js/services/master.js"></script>

    <!-- site logic -->
    <script type="text/javascript" src="../static/js/structures/main.js"></script>

    <script type="text/javascript" src="../static/js/structures/entity.js"></script>
</head>

<body onload="startEngine();">
<!--<p id="log" class="gui" ></p>-->
<div>
    <canvas id="focusedView" class="canvas main_screen" width="1024px" height="512px">
        Your browser doesn't appear to support the HTML5 <code>&lt;canvas&gt;</code> element.
    </canvas>
</div>
<div class="gui">
    <canvas id="overallView" class="canvas" width="1024px" height="45px">
        Your browser doesn't appear to support the HTML5 <code>&lt;canvas&gt;</code> element.
    </canvas>
</div>
<div>
    <!--<button type="button" id="mainScreenButton" class="gui btn btn-primary" onClick="showMain();">Game</button>-->
    <button type="button" id="play-from-one-computer" class="gui btn field" onclick="restartGamePlease();">Play from one
        browser
    </button>
    <button type="button" id="PLAY-WITH-AI" class="gui btn field" onclick="playWithAI();">Play with AI</button>
    <button type="button" id="multiplayer" class="gui btn field" onclick="playWithHuman();">Play with Human</button>

    <lable class="field"><input type="radio" name="mySide" value="WHITE"/>Defenders</lable>
    <lable class="field"><input type="radio" name="mySide" value="BLACK"/>Attackers</lable>
    <label class="field"><input checked type="radio" name="mySide" value="ANY"/> Any </label>
</div>

<!--
<div class="alert alert-error over-gui" style="position: absolute; left:365px; top:15px; width:746px;">
    <button type="button" class="close" data-dismiss="alert">&times;</button>
    <strong>Warning!</strong> If starting from corner, it should be set to "Board cell" above!
</div>
-->
<script>
    $(document).ready(function () {
        $('input').iCheck({
            checkboxClass: 'icheckbox_minimal-blue',
            radioClass: 'iradio_minimal-blue',
            increaseArea: '20%' // optional
        });
    });
</script>

<footer clear="both">

</footer>

</body>
</html>