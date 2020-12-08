package edu.rice.comp504.dragon.model.msgstrategy;

import edu.rice.comp504.dragon.model.message.Message;

public class Ban extends AMsgStrategy {
    private static AMsgStrategy only;

    /**
     * comments.
     * @return rt.
     */
    public static AMsgStrategy getOnly() {
        if (only == null) {
            only = new Ban();
        }
        return only;
    }

    public String getName() {
        return "Ban";
    }

    protected String getDisplayMsg(final Message msg) {
        return msg.getContext();
    }
}