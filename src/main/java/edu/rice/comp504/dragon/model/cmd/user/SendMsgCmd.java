package edu.rice.comp504.dragon.model.cmd.user;


import edu.rice.comp504.dragon.model.message.Message;
import edu.rice.comp504.dragon.model.user.User;

import java.io.IOException;

import static edu.rice.comp504.dragon.Utils.*;

public class SendMsgCmd implements IUserCmd {

    private Message msg;

    public SendMsgCmd(final Message msg) {
        this.msg = msg;
    }

    @Override
    public String getName() {
        return "sendMsg";
    }

    @Override
    public void execute(User receiver) {
        System.out.println("into SendMsgCmd. Receiver is: " + receiver.getUsername());
        System.out.println("msg is: " + msg.getContext());
        System.out.println("msg room is" + msg.getRoom());
        if (/*receiver.equals(msg.getUser()) ||*/ receiver.getBanList().contains(msg.getUser().getUsername())) {
            // todo: check flag
            return;
        }
        try {
            System.out.println("into try");
            if (receiver.getSession().isOpen()) {
                receiver.getSession().getRemote().sendString(toJson(msg.getKeyValueContext()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
