package edu.rice.comp504.dragon.model.msgstrategy;

import edu.rice.comp504.dragon.model.message.Message;

import static edu.rice.comp504.dragon.Utils.epoch2LocalTime;

public class ReportMsg extends AMsgStrategy {
    private static AMsgStrategy only;

    /**
     * comments.
     * @return rt.
     */
    public static AMsgStrategy getOnly() {
        if (only == null) {
            only = new ReportMsg();
        }
        return only;
    }

    public String getName() {
        return "ReportMsg";
    }

    @Override
    protected String getDisplayMsg(final Message msg) {
        return msg.getUser().getUsername() + " report user:" + msg.getContext() + " at " + epoch2LocalTime(msg.getTimeStamp());
    }
}
