package edu.rice.comp504.dragon.model.cmd.user;

import edu.rice.comp504.dragon.model.user.User;

public interface IUserCmd {
    String getName();

    void execute(User user);
}
