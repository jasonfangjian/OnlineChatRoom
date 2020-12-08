package edu.rice.comp504.dragon.model.cmd.chatroom;

import edu.rice.comp504.dragon.model.DispatchAdapter;
import edu.rice.comp504.dragon.model.chatroom.AChatRoom;
import edu.rice.comp504.dragon.model.chatroom.ChatRoom;
import edu.rice.comp504.dragon.model.cmd.user.IUserCmd;
import edu.rice.comp504.dragon.model.cmd.user.KickOutCmd;
import edu.rice.comp504.dragon.model.cmd.user.SendMsgCmd;
import edu.rice.comp504.dragon.model.message.Message;
import edu.rice.comp504.dragon.model.msgstrategy.ReportMsg;
import edu.rice.comp504.dragon.model.user.User;

public class ReportUserCmd implements IChatRoomCmd{
    private final String roomName;
    private final String targetId;
    private final User user;
    private final long time;

    /**
     * comments.
     * @param user u.
     * @param roomName r.
     * @param targetId t.
     * @param time t.
     */
    public ReportUserCmd(final User user, final String roomName, final String targetId, final long time) {
        this.roomName = roomName;
        this.targetId = targetId;
        this.user = user;
        this.time = time;
    }

    @Override
    public String getName() {
        return "report";
    }

    @Override
    public void execute(AChatRoom room) {
        if (!room.getRoomName().equals(roomName)) {
            return;
        }
        assert room instanceof ChatRoom;
        final User admin = room.getAdmin();

        Message report = new Message(targetId, user, ReportMsg.getOnly(), roomName, time);
        new SendMsgCmd(report).execute(admin);

    }
}
