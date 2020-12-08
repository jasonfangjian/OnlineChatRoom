'use strict';

let info;
// TODO: create websocket

window.onload = function () {
    console.log("MAIN")
    info = JSON.parse(localStorage.getItem("info"));
    // TODO: ask controller for the chat room list
    // TODO: list the rooms into #allRooms and #userRooms according to the info

    // add event listener on join button
    $("#btn-join").click(function() {
        let selected = $("#allRooms option:selected");
        // TODO: parse the selected.html() to list out the restrictions
        // TODO: check if the user meets the restrictions
        if (selected.length > 1) {
            alert("You can join one chat room at a time.");
            return;
        }
        addOption($("#userRooms"), selected.val(), selected.html());
        selected.remove();
    })

    // add event listener on enter button
    $("#btn-enter").click(function() {
        localStorage.setItem("info", JSON.stringify(info));
        $(location).attr('href', 'main.html');
    });

    // add event listener on leave button
    $("#btn-leave").click(function() {
        let selected = $("#userRooms option:selected");
        if (selected.length > 1) {
            alert("You can leave one chat room at a time.");
            return;
        }
        addOption($("#allRooms"), selected.val(), selected.html());
        selected.remove();
    });

    // add event listener on room type radio buttons for room type selection
    $("#roomType1").click(function() {
        $("#div_public").css("display", "");
        $("#div_private").css("display", "none");
    });
    $("#roomType2").click(function() {
        $("#div_public").css("display", "none");
        $("#div_private").css("display", "");
    });

    // add event listener on create button
    $("#btn-create").click(function() {
        if ($("#roomType1").is(":checked")) {
            // TODO: check if the specified room name exits (if so, alert() and fail create)
            // TODO: create public chat room in #userRooms and let controller know
        } else {
            // TODO: check if the specified user exits (if not, alert() and fail create)
            // TODO: create private chat room in #userRooms and let controller know
        }
    })

    // TODO: update main page every ? seconds (e.g. setInterval(updateMainPage, 5000) );
}

function addOption(list, value, inner) {
    list.append(`<option value=${value}>${inner} </option>`);
}

// TODO: create function to update if there is new message or new rooms
// function updateMainPage() { }

