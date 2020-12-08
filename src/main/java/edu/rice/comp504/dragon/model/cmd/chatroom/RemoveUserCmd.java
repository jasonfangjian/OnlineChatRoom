package edu.rice.comp504.dragon.model.cmd.chatroom;

import edu.rice.comp504.dragon.model.DispatchAdapter;
import edu.rice.comp504.dragon.model.chatroom.AChatRoom;
import edu.rice.comp504.dragon.model.chatroom.DirectRoom;
import edu.rice.comp504.dragon.model.cmd.user.IUserCmd;
import edu.rice.comp504.dragon.model.cmd.user.SendMsgCmd;
import edu.rice.comp504.dragon.model.message.Message;
import edu.rice.comp504.dragon.model.msgstrategy.LeaveRoom;
import edu.rice.comp504.dragon.model.user.User;

public class RemoveUserCmd implements IChatRoomCmd{

    private final User user;
    private final String targetRoom;
    private final String reason;

    /**
     * comments
     * @param user u.
     * @param targetRoom t.
     * @param reason r.
     */
    public RemoveUserCmd(final User user, final String targetRoom, final String reason) {
        this.user = user;
        this.targetRoom = targetRoom;
        this.reason = reason;
    }

    @Override
    public String getName() {
        return "logout";
    }

    @Override
    public void execute(AChatRoom room) {
        final DispatchAdapter adapter = DispatchAdapter.getInstance();
        if (targetRoom != null && !targetRoom.equals(room.getRoomName())) {
            return;
        }
        if (!adapter.listUser(room.getRoomName()).contains(user)) {
            return;
        }
        //System.out.println(room.getRoomName());
        final Message newMsg = new Message(null, user, LeaveRoom.getOnly(), room.getRoomName());
        newMsg.appendAttachment(Message.AttachmentType.REMOVE_USER, user.getUsername());
        newMsg.appendAttachment(Message.AttachmentType.REMOVE_REASON, reason);
        room.appendMsg(newMsg);
        final IUserCmd cmd = new SendMsgCmd(newMsg);
        adapter.firePropertyChange(room.getRoomName(), null, cmd);
        if (room instanceof DirectRoom || user.getUsername().equals(room.getAdmin().getUsername())) {
            adapter.removeRoom(room);
        } else {
            if (targetRoom != null) {
                adapter.removeUserFromRoom(targetRoom,user);
            } else {
                adapter.removeUserFromRoom(room.getRoomName(),user);
            }
        }
    }
}
