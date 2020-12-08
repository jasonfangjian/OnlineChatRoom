package edu.rice.comp504.dragon.model.cmd.chatroom;

import edu.rice.comp504.dragon.Utils;
import edu.rice.comp504.dragon.model.DispatchAdapter;
import edu.rice.comp504.dragon.model.chatroom.AChatRoom;
import edu.rice.comp504.dragon.model.chatroom.ChatRoom;
import edu.rice.comp504.dragon.model.cmd.user.IUserCmd;
import edu.rice.comp504.dragon.model.cmd.user.SendMsgCmd;
import edu.rice.comp504.dragon.model.message.Message;
import edu.rice.comp504.dragon.Utils.*;

public class BroadcastCmd implements IChatRoomCmd{
    private final Message msg;

    public BroadcastCmd(final Message msg) {
        this.msg = msg;
    }

    @Override
    public String getName() {
        return "broadcast";
    }

    @Override
    public void execute(AChatRoom room) {
        if (!room.getRoomName().equals(msg.getRoom())) {
            return;
        }
        if (((ChatRoom)room).getBanList().contains(msg.getUser().getUsername())) {
            return;
        }
        //final IUserCmd cmd = new SendMsgCmd(msg);
        final IUserCmd cmd = new SendMsgCmd(room.appendMsg(msg));
        DispatchAdapter.getInstance().firePropertyChange(room.getRoomName(), null, cmd);
    }
}