package edu.rice.comp504.dragon.model.cmd.chatroom;

import edu.rice.comp504.dragon.model.chatroom.AChatRoom;

public interface IChatRoomCmd {
    String getName();

    void execute(AChatRoom room);
}
