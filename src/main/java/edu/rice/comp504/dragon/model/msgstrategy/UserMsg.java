package edu.rice.comp504.dragon.model.msgstrategy;

import edu.rice.comp504.dragon.model.message.Message;

public class UserMsg extends AMsgStrategy {
    private static AMsgStrategy only;

    /**
     * comments.
     * @return rt.
     */
    public static AMsgStrategy getOnly() {
        if (only == null) {
            only = new UserMsg();
        }
        return only;
    }

    public String getName() {
        return "UserMsg";
    }

    @Override
    protected String getDisplayMsg(final Message msg) {
        //return msg.getUser().getUsername() + " said " + msg.getContext() + " at " + msg.getTimeStamp();
        return msg.getContext();
    }
}
