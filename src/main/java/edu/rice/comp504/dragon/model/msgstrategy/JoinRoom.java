package edu.rice.comp504.dragon.model.msgstrategy;

import edu.rice.comp504.dragon.model.message.Message;

import static edu.rice.comp504.dragon.Utils.epoch2LocalTime;

public class JoinRoom extends AMsgStrategy {
    private static AMsgStrategy only;

    /**
     * comments.
     * @return rt.
     */
    public static AMsgStrategy getOnly() {
        if (only == null) {
            only = new JoinRoom();
        }
        return only;
    }

    public String getName() {
        return "JoinRoom";
    }

    protected String getDisplayMsg(final Message msg) {
        return msg.getUser().getUsername() + " joined the room at " + epoch2LocalTime(msg.getTimeStamp());
    }
}
