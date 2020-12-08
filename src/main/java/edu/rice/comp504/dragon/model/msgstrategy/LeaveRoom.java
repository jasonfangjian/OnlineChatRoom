package edu.rice.comp504.dragon.model.msgstrategy;

import edu.rice.comp504.dragon.model.message.Message;

import static edu.rice.comp504.dragon.Utils.epoch2LocalTime;

public class LeaveRoom extends AMsgStrategy {
    private static AMsgStrategy only;

    /**
     * comments.
     * @return rt.
     */
    public static AMsgStrategy getOnly() {
        if (only == null) {
            only = new LeaveRoom();
        }
        return only;
    }

    public String getName() {
        return "LeaveRoom";
    }

    protected String getDisplayMsg(Message msg) {
        return msg.getUser().getUsername() + " left the room at " + epoch2LocalTime(msg.getTimeStamp());
    }
}
