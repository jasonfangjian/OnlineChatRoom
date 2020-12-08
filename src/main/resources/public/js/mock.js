const loginData = {
    data: {
        "type": "loginResponse",
        "userName": "user1",
        "visibleRooms": [ "room1", "room2", "room3", "room4" ]
    }
}

const welcomeData = {
    data: {
        "msgType": "welcome",
        "msg": {
            "info" : {
                "admin": "MJ",
                "ageRange": "0 ~ 33",
                "interestList": ["interestA", "interestB", "..."],
                "schoolList": ["Rice", "UCLA", "UCB", "..."],
                "roomName": "room4"
            } ,
            "historyMsg": [
                "some messages here",
                "...",
            ],
            "users": ["MJ", "WHO", "ELSE", "..."]
        },
        "time": 12345,
        "room": "room4",
        "msgId": "idHere",
        "from": "${admin}"
    }
}

const joinRoomData = {
    "msgType": "JoinRoom",
    "msg": "user2 joined the room at time-1234",
    "time": 12345,
    "room": "room4",
    "msgId": "msgId",
    "ADD_USER": "user2",
    "from": "user2"
}

export { loginData, welcomeData, joinRoomData };