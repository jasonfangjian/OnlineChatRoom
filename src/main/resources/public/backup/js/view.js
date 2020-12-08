'use strict';

let info = { }

window.onload = function () {
    $("#btn-login").click(function() {
        if (signInFormCheck()) {
            info.username = $("#inp-username").val();
            info.age = $("#inp-age").val();
            info.school = $("#inp-school").val();
            info.interest = $("#inp-interest").val();
            info.admin = [];
            localStorage.setItem("info", JSON.stringify(info));
            $(location).attr('href', 'main.html');
        } else {
            alert("Username has to be longer than 5 characters");
        }
    })
}

function signInFormCheck() {
    // TODO: check if username exist. Might have to send get/post request to controller
    return !($("#inp-username").val().trim().length < 5);
}
