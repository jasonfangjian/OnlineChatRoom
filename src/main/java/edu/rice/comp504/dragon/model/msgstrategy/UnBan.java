package edu.rice.comp504.dragon.model.msgstrategy;

import edu.rice.comp504.dragon.model.message.Message;

public class UnBan extends AMsgStrategy {
    private static AMsgStrategy only;

    /**
     * comments.
     * @return rt.
     */
    public static AMsgStrategy getOnly() {
        if (only == null) {
            only = new UnBan();
        }
        return only;
    }

    public String getName() {
        return "UnBan";
    }

    protected String getDisplayMsg(final Message msg) {
        return msg.getContext();
    }
}