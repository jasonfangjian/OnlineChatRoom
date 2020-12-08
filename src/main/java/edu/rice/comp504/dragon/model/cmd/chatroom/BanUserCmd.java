package edu.rice.comp504.dragon.model.cmd.chatroom;

import edu.rice.comp504.dragon.model.DispatchAdapter;
import edu.rice.comp504.dragon.model.chatroom.AChatRoom;
import edu.rice.comp504.dragon.model.chatroom.ChatRoom;
import edu.rice.comp504.dragon.model.cmd.user.IUserCmd;
import edu.rice.comp504.dragon.model.cmd.user.KickOutCmd;
import edu.rice.comp504.dragon.model.cmd.user.SendMsgCmd;
import edu.rice.comp504.dragon.model.message.Message;
import edu.rice.comp504.dragon.model.msgstrategy.JoinRoom;
import edu.rice.comp504.dragon.model.user.User;
import edu.rice.comp504.dragon.model.msgstrategy.*;

public class BanUserCmd implements IChatRoomCmd{
    private final String roomName;
    private final String targetId;
    private final User user;
    private final EditType type;

    /**
     * comments.
     * @param user u.
     * @param roomName r.
     * @param targetId t.
     * @param type t.
     */
    public BanUserCmd(final User user,
                      final String roomName,
                      final String targetId,
                      final String type) {
        this.roomName = roomName;
        this.targetId = targetId;
        this.user = user;
        this.type = EditType.valueOf(type);
    }

    @Override
    public String getName() {
        return "ban";
    }

    @Override
    public void execute(AChatRoom room) {
        if (!room.getRoomName().equals(roomName)) {
            return;
        }
        assert user.equals(room.getAdmin());
        assert room instanceof ChatRoom;
        final ChatRoom chatRoom = (ChatRoom) room;
        if (targetId.equals(room.getAdmin().getUsername())) {
            DispatchAdapter.getInstance().leaveRoom(room.getRoomName(),room.getAdmin());
            return;
        }
        if (type == EditType.mute) {
            chatRoom.banUser(targetId);
            final Message newMsg = new Message(targetId,room.getAdmin(),Ban.getOnly(), room.getRoomName());
            newMsg.appendAttachment(Message.AttachmentType.BAN_MSG, targetId);
            IUserCmd cmd = new SendMsgCmd(room.appendMsg(newMsg));
            DispatchAdapter.getInstance().firePropertyChange(room.getRoomName(), null, cmd);
        } else if (type == EditType.unmute) {
            chatRoom.unbanUser(targetId);
            final Message newMsg = new Message(targetId, room.getAdmin(),UnBan.getOnly(), room.getRoomName());
            newMsg.appendAttachment(Message.AttachmentType.UNBAN_MSG, targetId);
            IUserCmd cmd = new SendMsgCmd(room.appendMsg(newMsg));
            DispatchAdapter.getInstance().firePropertyChange(room.getRoomName(), null, cmd);
        } else {
            final IUserCmd cmd = new KickOutCmd(room, targetId);
            DispatchAdapter.getInstance().firePropertyChange(roomName, null, cmd);
        }
    }

    private enum EditType {
        mute, kick, unmute;
    }
}
