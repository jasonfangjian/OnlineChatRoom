'use strict';

import {requests} from './requests.js';

import {loginData, welcomeData} from "./mock.js";

let userInfo = {};
let roomInfo = {};

const emojiLib = [
    "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇", "🙂", "🙃", "😉", "😌",
    "😍", "🥰", "😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎",
];

Array.prototype.indexOf = function (val) {
    for (let i = 0; i < this.length; i++) {
        if (typeof this[i] === "object") {
            if (this[i].msgId === val) return i;
        } else {
            if (this[i] === val) return i;
        }
    }
    return -1;
};
Array.prototype.remove = function (val) {
    const index = this.indexOf(val);
    if (index > -1) {
        this.splice(index, 1);
    }
};
Array.prototype.replace = function (oldVal, newVal) {
    const index = this.indexOf(oldVal);
    if (index > -1) {
        this.splice(index, 1, newVal);
    }
}

// const webSocket = new WebSocket("wss://" + location.hostname + ":" + location.port + "/chatapp");
// const webSocket = new WebSocket("wss://" + location.hostname + ":" + location.port + "/chatapp");
let webSocket;

let pingId;

function ping() {
    webSocket.send("ping");
}

window.onload = function () {
    pageRedirect('login');

    setUpWebSocket();
    // pingId = setInterval(ping, 29000);

    /*
     * login page
     */
    $("#btn-login").click(login);

    /*
     * main page
     */
    // add event listener on logout button
    $("#btn-logout-main").click(logout);
    // add event listener on join button
    $("#btn-join").click(joinRoom);
    // add event listener on enter button
    $("#btn-enter").click(enterRoom);
    // add event listener on leave button
    $("#btn-leave").click(leaveRoom);

    // add event listener on room type radio buttons for room type selection
    $("#inp-public").click(function () {
        $("#div_public").show();
        $("#div_private").hide();
    });
    $("#inp-private").click(function () {
        $("#div_public").hide();
        $("#div_private").show();
    });

    // add event listener on create button
    $("#btn-create").click(createRoom);

    /*
     * room page
     */
    // add event listener on logout button
    $("#btn-logout-room").click(logout);

    // add event listener on back button
    $("#btn-back").click(function () {
        // roomInfo = {};
        pageRedirect('main');
    });

    $('#btn-send').click(sendMsg);
    $('#btn-recall').click(recallMsg);
    $('#btn-edit').click(editMsg);
    $('#btn-cancel').click(cancel);
    $('#btn-save-changes').click(saveChanges);
    $('#btn-delete').click(deleteMsg);
    $('#btn-invite').click(() => {
        pageRedirect('invite');
    });
    $('#btn-cancel-invite').click(() => {
        pageRedirect('room');
    });
    $('#btn-confirm-invite').click(invite);
    $('#btn-invite-success').click(() => {
        pageRedirect('room');
    });
    $('#btn-invite-fail').click(() => {
        pageRedirect('room');
    });
    $('#btn-ban').click(ban);
    $('#btn-block').click(block);
    $("#btn-join-room-fail").click(function () {
        pageRedirect('main');
    });
    $("#btn-login-fail").click(function () {
        pageRedirect('login');
    });
    $("#btn-unblock").click(unblock);
    $("#btn-unban").click(unban);
    // $("#btn-insert-emoji").click(insertEmoji);
    $("#select-emoji").change(insertEmoji);

    for (let i = 0; i < emojiLib.length; i++) {
        addOption($("#select-emoji"), emojiLib[i], emojiLib[i], "emoji-" + emojiLib[i]);
    }
}

// window.onbeforeunload = function () {
//     logout();
//     webSocket.close();
// }

window.onunload = function () {
    logout();
    clearInterval(pingId);
}

function setUpWebSocket() {
    webSocket = new WebSocket("wss://" + location.hostname + ":" + location.port + "/chatapp");
    webSocket.onopen = () => {
        pingId = setInterval(ping, 29000);
    }
    webSocket.onmessage = (msg) => responseHandler(msg);
    webSocket.onclose = () => {
        setTimeout(setUpWebSocket, 2000);
    }
    webSocket.onerror = () => {
        setTimeout(setUpWebSocket, 2000);
    }
}

function addOption(list, value, inner, id) {
    list.append(`<option id=${id} value=${value}>${inner} </option>`);
}

/**
 * Login page
 */

function logInFormCheck() {
    return !($("#inp-username").val().trim().length < 5);
}

/**
 * user login
 */
function login() {
    if (logInFormCheck()) {
        userInfo.username = $("#inp-username").val();
        userInfo.age = $("#inp-age").val();
        userInfo.school = $("#inp-school").val();
        userInfo.interest = $("#inp-interest").val().split(',') || [];
        userInfo.admin = [];

        webSocket.send(JSON.stringify(requests.getLoginRequest(
            userInfo.username,
            userInfo.age,
            userInfo.interest,
            userInfo.school
        )));
    } else {
        pageRedirect('login-fail');
    }
}

/**
 * logout main
 */
function logout() {
    userInfo = {};
    roomInfo = {};
    webSocket.send(JSON.stringify(requests.getLogoutRequest()));
    $("#allRooms").empty();
    $("#userRooms").empty();
    pageRedirect('login');
}

/**
 * Join a public room in the all room list
 */
function joinRoom() {
    let selected = $("#allRooms option:selected");
    if (selected.length === 1 && selected.val()) {
        webSocket.send(JSON.stringify(requests.getJoinRoomRequest(selected.val())));
    }
}

/**
 * Enter a room
 */
function enterRoom() {
    const selected = $("#userRooms option:selected");
    if (selected.length > 1) {
        return;
    }

    if (!selected.attr("id")) {
        return;
    }
    const roomName = selected.attr("id").match(/userRooms-(\S*)/)[1];
    const theRoomInfo = roomInfo[roomName];

    console.info("roomInfo is: ");
    console.info(roomInfo);

    $("#roomName").text(roomName);
    $("#res-age").empty();
    if (theRoomInfo.info.ageRange && !(theRoomInfo.info.ageRange[0] === 0 && theRoomInfo.info.ageRange[1] === 200)) {
        $("#res-age").text(theRoomInfo.info.ageRange[0] + " ~ " + theRoomInfo.info.ageRange[1]);
    }
    $("#res-interest").empty();
    if (theRoomInfo.info.interestList) {
        $("#res-interest").text(theRoomInfo.info.interestList);
    }
    $("#res-school").empty();
    if (theRoomInfo.info.schoolList) {
        $("#res-school").text(theRoomInfo.info.schoolList);
    }

    $("#userList").empty();
    theRoomInfo.users.map((item) => {
        if (item === theRoomInfo.info.admin) {
            addOption($("#userList"), item, item + " (admin)", "userList-" + item);
        } else {
            addOption($("#userList"), item, item, "userList-" + item);
        }
    });
    $("#messageList").empty();
    theRoomInfo.historyMsg.map((item) => {
        renderMsg(item);
    });
    $("#room-username").text(userInfo.username);

    if (theRoomInfo.info.admin === userInfo.username) {
        $("#btn-ban").show();
        $("#btn-unban").show();
        $("#btn-delete").show();
    } else {
        $("#btn-ban").hide();
        $("#btn-unban").hide();
        $("#btn-delete").hide();
    }
    if (theRoomInfo.info.isPrivate === true && theRoomInfo.info.admin === userInfo.username) {
        $("#btn-invite").show();
    } else {
        $("#btn-invite").hide();
    }

    pageRedirect('room');
}

/**
 * Render messages
 */
function renderMsg(data) {
    if (data.msgType === 'JoinRoom' || data.msgType === 'LeaveRoom' ||
        data.msgType === 'RecallMsg' || data.msgType === 'DeleteMsg') {
        $("#messageList").append(`<option class="msg-sys-info" id=${data.msgId} value=${data.from}>${"System Info: " + data.msg} </option>`);
    } else if (data.from === userInfo.username) {
        $("#messageList").append(`<option class="msg-from-myself" value=${data.from} id=${data.msgId}>${data.from + ": " + data.msg} </option>`);
        $("#messageList").append(`<option class="date-from-myself" value=${data.from} id=${data.msgId}>${data.time} </option>`);
    } else {
        $("#messageList").append(`<option class="msg-from-others" value=${data.from} id=${data.msgId}>${data.from + ": " + data.msg} </option>`);
        $("#messageList").append(`<option class="date-from-others" value=${data.from} id=${data.msgId}>${data.time} </option>`);
    }
}

/**
 * create a room
 */
function createRoom() {
    if ($("#inp-public").is(":checked")) {
        const roomName = $("#inp-roomName").val();
        // if (roomInfo.hasOwnProperty(roomName)) {
        //     console.info("Room " + roomName + " already exists!");
        //     return;
        // }
        createPublicRoom();
    } else {
        createPrivateRoom();
    }
    $("#inp-private-roomName").val("");
    $("#inp-roomName").val("");
    $("#inp-restriction_age_from").val("");
    $("#inp-restriction_age_to").val("");
    $("#inp-restriction_school").val("");
    $("#inp-restriction_interest").val("");
}

/**
 * create a public room
 */
function createPublicRoom() {
    const roomName = $("#inp-roomName").val();
    const ageRange = [$("#inp-restriction_age_from").val(), $("#inp-restriction_age_to").val()];
    const schoolList = $("#inp-restriction_school").val().split(",");
    const interestList = $("#inp-restriction_interest").val().split(",");

    let restriction = "false";
    if ($("#inp-restriction_age_from").val() || $("#inp-restriction_school").val() || $("#inp-restriction_interest").val()) {
        restriction = "true";
    }
    if (roomName) {
        webSocket.send(JSON.stringify(requests.getCreateRoomRequest(
            roomName, restriction, false, ageRange, schoolList, interestList
        )));
    }
}

/**
 * create a private room
 */
function createPrivateRoom() {
    const roomName = $("#inp-private-roomName").val();
    const ageRange = [$("#inp-restriction_age_from").val(), $("#inp-restriction_age_to").val()];
    const schoolList = $("#inp-restriction_school").val().split(",");
    const interestList = $("#inp-restriction_interest").val().split(",");

    const restriction = "false";
    if (roomName) {
        webSocket.send(JSON.stringify(requests.getCreateRoomRequest(
            roomName, restriction, true, ageRange, schoolList, interestList
        )));
    }
}

/**
 * leave a room
 */
function leaveRoom() {
    let selected = $("#userRooms option:selected");
    if (selected.length > 1) {
        alert("You can leave one chat room at a time.");
        return;
    }
    const roomName = selected.val();
    if (roomName) {
        webSocket.send(JSON.stringify(requests.getLeavingRoomRequest(roomName)));
    }
}

/**
 * insert emoji
 */
function insertEmoji() {
    const selected = $("#select-emoji option:selected");
    const input = $("#msg");
    input.val(input.val() + selected.text());
}

/**
 * send message
 */
function sendMsg() {
    const msg = $("#msg").val();
    if (msg) {
        webSocket.send(JSON.stringify(requests.getSendMsgRequest(msg, $("#roomName").text())));
        $("#msg").val('');
    }
}

/**
 * recall a message
 */
function recallMsg() {
    const selected = $("#messageList option:selected");
    if (selected.length > 1) {
        alert("You can only recall one message at a time.");
    } else {
        if (selected.val() === userInfo.username) {
            console.info(JSON.stringify(requests.getRecallUpdateRequest(selected.attr('id'), $("#roomName").text())));
            webSocket.send(JSON.stringify(requests.getRecallUpdateRequest(selected.attr('id'), $("#roomName").text())));
        } else {
            console.info("You can not recall messages sent by others!");
        }
    }
}

/**
 * edit a message
 */
function editMsg() {
    const selected = $("#messageList option:selected");
    if (selected.length > 1) {
        alert("You can only recall one message at a time.");
    } else {
        if (selected.val() === userInfo.username) {
            const input = $("#msg");
            input.val(selected.text().split(selected.val() + ': ')[1]);
            input.focus();
            $("#btn-send").hide();
            $("#btn-cancel").show();
            $("#btn-save-changes").show();
        } else {
            console.info("You can not edit messages sent by others!");
        }
    }
}

/**
 * save changes
 */
function saveChanges() {
    const selected = $("#messageList option:selected");
    webSocket.send(JSON.stringify(requests.getRecallUpdateRequest(selected.attr('id'), $("#roomName").text(), $("#msg").val())));
}

/**
 * cancel edit changes
 */
function cancel() {
    $("#msg").val('');
    $("#btn-send").show();
    $("#btn-cancel").hide();
    $("#btn-save-changes").hide();
}

/**
 * delete message
 */
function deleteMsg() {
    const selected = $("#messageList option:selected");
    if (selected.attr('id')) {
        webSocket.send(JSON.stringify(requests.getDeleteRequest(selected.attr('id'), $("#roomName").text())));
    }
}

/**
 * invite a user to join a private room
 */
function invite() {
    const inviteName = $("#inp-invite-username").val();
    if (inviteName) {
        webSocket.send(JSON.stringify(requests.getInviteRequest(inviteName, $("#roomName").text())));
    }
}

/**
 * Admin Ban a user
 */
function ban() {
    const selected = $("#userList option:selected");
    if (selected.val()) {
        webSocket.send(JSON.stringify(requests.getBanRequest("ban", selected.val(), $("#roomName").text())));
    }
}

/**
 * Admin unban a user
 */
function unban() {
    const selected = $("#userList option:selected");
    if (selected.val()) {
        webSocket.send(JSON.stringify(requests.getBanRequest("unban", selected.val(), $("#roomName").text())));
    }
}

/**
 * block a user
 */
function block() {
    const selected = $("#userList option:selected");
    if (selected.val()) {
        webSocket.send(JSON.stringify(requests.getBlockRequest("block", selected.val())));
    }
}

/**
 * unblock a user
 */
function unblock() {
    const selected = $("#userList option:selected");
    if (selected.val()) {
        webSocket.send(JSON.stringify(requests.getBlockRequest("unblock", selected.val())));
    }
}

/**
 * Handle the Responses
 */
function responseHandler(message) {
    const data = JSON.parse(message.data);
    // const data = message.data; // mock

    const msgType = data.type || data.msgType;

    console.info("data received " + msgType);
    console.info(data);

    switch (msgType) {
        case 'loginResponse':
            if (data.username === userInfo.username) {
                data.visibleRooms.map((item) => {
                    roomInfo[item] = {
                        "info": {"roomName": item},
                        "isPrivate": false,
                        "users": [],
                    };
                    addOption($("#allRooms"), item, item, "allRooms-" + item);
                });
                $("#main-username").text(userInfo.username);
                pageRedirect('main');
            }
            break;
        case 'welcome':
            const msg = JSON.parse(data.msg);
            console.info("msg is:");
            console.info(msg);

            let historyMsg = JSON.parse(msg.historyMsg);
            historyMsg = historyMsg.map((item) => JSON.parse(item));

            let info = JSON.parse(msg.info);
            console.info("info is:");
            console.info(info);

            let ageRange = [];
            if (info.ageRange) {
                ageRange = info.ageRange.split(' ~ ');
                ageRange[0] = parseInt(ageRange[0]);
                ageRange[1] = parseInt(ageRange[1]);
            }
            let schoolList = [];
            if (info.schoolList) {
                schoolList = info.schoolList.split(', ') || JSON.parse(info.schoolList);
            }
            console.info("interstList is:");
            console.info(info.interestList);
            let interestList = []
            if (info.interestList) {
                interestList = info.interestList.split(', ') || JSON.parse(info.interestList);
            }

            let users = JSON.parse(msg.users);

            roomInfo[data.room] = {
                ...roomInfo[data.room],
                ...msg,
                "historyMsg": historyMsg,
                "users": users,
                "info": {
                    "ageRange": ageRange,
                    "schoolList": schoolList,
                    "interestList": interestList,
                },
            };
            const roomName = data.room;
            const id = "allRooms-" + roomName;
            addOption($("#userRooms"), roomName, roomName, "userRooms-" + roomName);
            $("#" + id).remove();
            break;
        case 'JoinRoom':
        case 'joinRoom':
            if (data.ADD_USER === userInfo.username) {
                break;
            }
            roomInfo[data.room] = {
                ...roomInfo[data.room],
            };
            roomInfo[data.room].users.push(data.ADD_USER);
            roomInfo[data.room].historyMsg.push(data);
            if ($("#frame-room").is(":visible") && $("#roomName").text() === data.room) {
                addOption($("#userList"), data.ADD_USER, data.ADD_USER, "userList-" + data.ADD_USER);
                $("#messageList").append(`<option class="msg-sys-info" value=${data.from}>${"System Info: " + data.msg} </option>`);
            }
            break;
        case 'newPublicRoom':
            roomInfo[data.roomName] = {
                ...roomInfo[data.room],
                "info": {
                    "roomName": data.roomName,
                    "admin": data.admin,
                    "ageRange": data.ageRange,
                    "schoolList": data.schoolList,
                    "interestList": data.interest,
                    "isPrivate": false,
                },
                "historyMsg": [],
                "users": [data.admin]
            };
            let roomText = data.roomName + "(Public)";
            roomText += (data.ageRange[0] === 0 && data.ageRange[1] === 200) ? "" : " | ageRange: " + data.ageRange[0] + "~" + data.ageRange[1];
            roomText += data.schoolList ? " | school: " + data.schoolList : "";
            roomText += data.interest ? " | interest: " + data.interest : "";
            if (data.admin === userInfo.username) {
                addOption($("#userRooms"), data.roomName, roomText, "userRooms-" + data.roomName);
            } else {
                addOption($("#allRooms"), data.roomName, roomText, "allRooms-" + data.roomName);
            }
            break;
        case 'invitation':
        case 'newPrivateRoom':
            const privateRoomName = data.roomName || data.room;
            roomInfo[privateRoomName] = {
                ...roomInfo[privateRoomName],
                "info": {
                    "roomName": privateRoomName,
                    "admin": data.admin,
                    "isPrivate": true
                },
                "historyMsg": [],
                "users": [data.admin]
            };
            if (data.admin === userInfo.username) {
                addOption($("#userRooms"), privateRoomName, privateRoomName + "(Private)", "userRooms-" + privateRoomName);
            } else {
                addOption($("#allRooms"), privateRoomName, privateRoomName + "(Private)", "allRooms-" + privateRoomName);
            }
            break;
        case 'createRoomError':
        case 'createRoomFailed': {
            $("#join-room-fail-txt").hide();
            $("#create-room-fail-txt").show();
            pageRedirect('join-room-fail');
            break;
        }
        case 'LeaveRoom':
        case 'leaveRoom':
            roomInfo[data.room].users.remove(data.REMOVE_USER);
            roomInfo[data.room].historyMsg.push(data);
            // main
            if (data.REMOVE_USER === userInfo.username) {
                roomInfo[data.room].users.remove(userInfo.username);
                addOption($("#allRooms"), data.room, data.room, "allRooms-" + data.room);
                $("#" + "userRooms-" + data.room).remove();
            }
            // room
            if ($("#frame-room").is(":visible") && $("#roomName").text() === data.room) {
                $("#messageList").append(`<option class="msg-sys-info" value=${data.from}>${"System Info: " + data.msg} </option>`);
            }
            $("#userList-" + data.REMOVE_USER).remove();
            break;
        case 'removeRoom':
            const removeRoomName = data.roomName || data.msg.roomName;
            // main page
            console.info("roomInfo before remove: ");
            const debug = JSON.parse(JSON.stringify(roomInfo));
            console.info(debug);
            if (!roomInfo.hasOwnProperty(removeRoomName) || roomInfo[removeRoomName].users.indexOf(userInfo.username) === -1) {  // did not join the room
                $("#" + "allRooms-" + removeRoomName).remove();
            } else {  // joined in the room
                $("#" + "userRooms-" + removeRoomName).remove();
            }
            if ($("#frame-room").is(":visible") && $("#roomName").text() === removeRoomName) {
                $("#join-room-fail-txt").hide();
                $("#create-room-fail-txt").hide();
                $("#room-dissolved-txt").show();
                pageRedirect('join-room-fail');
            }
            delete roomInfo[removeRoomName];
            break;
        case 'UserMsg':
        case 'userMsg':
            roomInfo[data.room].historyMsg.push(data);
            if (data.room === $("#roomName").text()) {
                renderMsg(data);
            }
            break;
        case 'RecallMsg':
        case 'recallMsg':
            const recallMsgCxt = data.from + " recalled a message";
            roomInfo[data.room].historyMsg.replace(data.DELETE_MSG, {
                "msgType": data.msgType,
                "msgId": data.msgId,
                "room": data.room,
                "msg": recallMsgCxt,
                "from": data.from,
                "timeStamp": data.time,
            });
            if ($("#frame-room").is(":visible") && $("#roomName").text() === data.room) {
                const recallElement = $("#" + data.DELETE_MSG);
                recallElement.text(recallMsgCxt);
                recallElement.attr("class", "msg-sys-info");
            }
            break;
        case 'EditMsg':
            roomInfo[data.room].historyMsg.replace(data.DELETE_MSG, {
                "msgType": data.msgType,
                "msgId": data.msgId,
                "room": data.room,
                "msg": data.UPDATE_MSG,
                "from": data.from,
                "timeStamp": data.time,
            });
            if ($("#frame-room").is(":visible") && $("#roomName").text() === data.room) {
                $("#" + data.DELETE_MSG).text(data.from + " : " + data.UPDATE_MSG + " (Edited)");
            }
            if (data.from === userInfo.username) {
                cancel();
            }
            break;
        case 'DeleteMsg':
            const deleteMsgCxt = roomInfo[data.room].info.admin + " deleted a message";
            roomInfo[data.room].historyMsg.replace(data.DELETE_MSG, {
                "msgType": data.msgType,
                "msgId": data.msgId,
                "room": data.room,
                "msg": deleteMsgCxt,
                "from": data.from,
                "timeStamp": data.time,
            });
            if ($("#frame-room").is(":visible") && $("#roomName").text() === data.room) {
                const deleteElement = $("#" + data.DELETE_MSG);
                deleteElement.text(deleteMsgCxt);
                deleteElement.attr("class", "msg-sys-info");
            }
            break;
        case 'inviteUserSuccess':
            pageRedirect('invite-success');
            break;
        case 'inviteFailed':
            pageRedirect('invite-fail');
            break;
        case 'Ban':
            const banMsgCxt = data.msg + " was banned.";
            roomInfo[data.room].historyMsg.push({
                ...data,
                "msg": banMsgCxt,
            });
            if ($("#frame-room").is(":visible") && $("#roomName").text() === data.room) {
                $("#messageList").append(`<option class="msg-sys-info" value=${data.from}>${
                    "System Info: " + banMsgCxt} </option>`);
                $("#userList-" + data.msg).text(data.msg + " (Banned)");
            }
            break;
        case 'UnBan':
            const unbanMsgCxt = data.msg + " was unbanned.";
            roomInfo[data.room].historyMsg.push({
                ...data,
                "msg": unbanMsgCxt,
            });
            if ($("#frame-room").is(":visible") && $("#roomName").text() === data.room) {
                $("#messageList").append(`<option class="msg-sys-info" value=${data.from}>${
                    "System Info: " + unbanMsgCxt} </option>`);
                $("#userList-" + data.msg).text(data.msg);
            }
            break;
        case 'joinRoomFailed': {
            $("#join-room-fail-txt").show();
            $("#create-room-fail-txt").hide();
            pageRedirect('join-room-fail');
            break;
        }
        case 'loginFailed': {
            pageRedirect('login-fail');
            break;
        }
        default:
            alert("Unknown message received.\ntype: " + msgType);
            console.info(JSON.stringify(data));
            break;
    }
    console.info("roomInfo is: ");
    console.info(roomInfo);
}

// multi pages
function pageRedirect(src) {
    switch (src) {
        case 'login': {
            $("#frame-main").hide();
            $("#frame-login-fail").hide();
            $("#frame-room").hide();
            $("#frame-invite").hide();
            $("#frame-invite-success").hide();
            $("#frame-invite-fail").hide();
            $("#frame-join-room-fail").hide();
            $("#frame-login").show();
            break;
        }
        case 'main': {
            $("#frame-login").hide();
            $("#frame-login-fail").hide();
            $("#frame-room").hide();
            $("#frame-invite").hide();
            $("#frame-invite-success").hide();
            $("#frame-invite-fail").hide();
            $("#frame-join-room-fail").hide();
            $("#frame-main").show();
            // window.history.pushState({path: "main"}, null, "/main");
            break;
        }
        case 'room': {
            $("#frame-login").hide();
            $("#frame-login-fail").hide();
            $("#frame-main").hide();
            $("#frame-invite").hide();
            $("#frame-invite-success").hide();
            $("#frame-invite-fail").hide();
            $("#frame-join-room-fail").hide();
            $("#frame-room").show();
            $("#btn-cancel").hide();
            $("#btn-save-changes").hide();
            break;
        }
        case 'invite': {
            $("#frame-login").hide();
            $("#frame-login-fail").hide();
            $("#frame-main").hide();
            $("#frame-invite-success").hide();
            $("#frame-invite-fail").hide();
            $("#frame-join-room-fail").hide();
            $("#frame-room").show();
            $("#frame-invite").show();
            break;
        }
        case 'invite-success': {
            $("#frame-login").hide();
            $("#frame-login-fail").hide();
            $("#frame-main").hide();
            $("#frame-invite").hide();
            $("#frame-invite-fail").hide();
            $("#frame-join-room-fail").hide();
            $("#frame-invite-success").show();
            $("#frame-room").show();
            break;
        }
        case 'invite-fail': {
            $("#frame-login").hide();
            $("#frame-login-fail").hide();
            $("#frame-main").hide();
            $("#frame-invite").hide();
            $("#frame-invite-success").hide();
            $("#frame-join-room-fail").hide();
            $("#frame-invite-fail").show();
            $("#frame-room").show();
            break;
        }
        case 'join-room-fail': {
            $("#frame-login").hide();
            $("#frame-login-fail").hide();
            $("#frame-main").show();
            $("#frame-invite").hide();
            $("#frame-invite-success").hide();
            $("#frame-invite-fail").hide();
            $("#frame-room").hide();
            $("#frame-join-room-fail").show();
            break;
        }
        case 'login-fail': {
            $("#frame-login").hide();
            $("#frame-login-fail").show();
            $("#frame-main").hide();
            $("#frame-invite").hide();
            $("#frame-invite-success").hide();
            $("#frame-invite-fail").hide();
            $("#frame-room").hide();
            $("#frame-join-room-fail").hide();
            break;
        }
        default:
            alert('Unauthorized Action!');
    }
}

/**
 * Router
 */
window.onpopstate = function (event) {
    console.info(event.state);
    console.info(event.state.path);
}

// main page


// room page
