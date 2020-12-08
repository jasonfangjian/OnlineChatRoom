package edu.rice.comp504.dragon.model.msgstrategy;

import edu.rice.comp504.dragon.model.message.Message;

import static edu.rice.comp504.dragon.Utils.epoch2LocalTime;

public class RecallMsg extends AMsgStrategy {
    private static AMsgStrategy only;

    /**
     * comments.
     * @return rt.
     */
    public static AMsgStrategy getOnly() {
        if (only == null) {
            only = new RecallMsg();
        }
        return only;
    }

    public String getName() {
        return "RecallMsg";
    }

    protected String getDisplayMsg(final Message msg) {
        return msg.getUser().getUsername() + " recalled the message at " + epoch2LocalTime(msg.getTimeStamp());
    }
}
