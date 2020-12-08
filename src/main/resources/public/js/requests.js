const getLoginRequest = (username, age, interests, school) => ({
    type: "login",
    username,
    age,
    school,
    "interestList": interests
});

const getLogoutRequest = () => ({
    type: "logout"
});

const getJoinRoomRequest = (roomName) => ({
    type: "joinRoom",
    roomName
});

const getLeavingRoomRequest = (roomName) => ({
    type: "leaveRoom",
    roomName
});

const getSendMsgRequest = (context, roomName) => ({
    type: "sendUserMsg",
    timeStamp: new Date().getTime(),
    context,
    roomName: roomName,
});

const getRecallUpdateRequest = (msgId, roomName, newContext) => ({
    type: newContext ? "editMsg" : "recallMsg",
    timeStamp: new Date().getTime(),
    newContext,
    roomName,
    msgId
});

const getDeleteRequest = (msgId, roomName) => ({
    type: "deleteMsg",
    roomName,
    msgId,
    timeStamp: new Date().getTime(),
});

const getCreateDMRequest = (target, initMsg) => ({
    type: "createDM",
    context: initMsg,
    target
});

const getCreateRoomRequest = (roomName, restriction, isPrivate, ageRange, schools, interests) => ({
    type: "createRoom",
    roomName,
    restriction,
    isPrivate,
    ageRange,
    schoolList: schools,
    interestList: interests
});

const getInviteRequest = (target, roomName) => ({
    type: "invite",
    target: [target],
    roomName
})

const getBanRequest = (banType, tarUser, roomName) => ({
    type: banType,
    tarUser,
    roomName
});

const getBlockRequest = (blockType, tarUser) => ({
    type: blockType,
    tarUser
});

const getReportRequest = (roomName, userId, reportDetails) => ({
    type: "report",
    userId,
    roomName,
    reportDetails
})

const requests = {
    getLoginRequest,
    getLogoutRequest,
    getJoinRoomRequest,
    getLeavingRoomRequest,
    getSendMsgRequest,
    getRecallUpdateRequest,
    getDeleteRequest,
    getCreateDMRequest,
    getCreateRoomRequest,
    getInviteRequest,
    getBanRequest,
    getBlockRequest,
    getReportRequest
}

export {requests};