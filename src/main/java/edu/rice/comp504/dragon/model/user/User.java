package edu.rice.comp504.dragon.model.user;

import edu.rice.comp504.dragon.model.chatroom.ChatRoom;
import edu.rice.comp504.dragon.model.cmd.user.IUserCmd;
import org.eclipse.jetty.websocket.api.Session;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

public class User implements PropertyChangeListener {
    private String username;
    private int age;
    private String school;
    private List<String> interest;

    private Set<String> banList;


    /**
     *  this.curRoom indicates which room the edu.rice.comp504.dragon.model.user enters
     *  isAdmin indicates if the edu.rice.comp504.dragon.model.user is the admin of curRoom
     **/
    private ChatRoom curRoom;

    private final Session session;

    /**
     * comments.
     * @param session s.
     * @param username u.
     * @param age a.
     * @param school s.
     * @param interest i.
     */
    public User(Session session ,String username, int age, String school, String[] interest) {
        this.username = username;
        this.age = age;
        this.school = school;
        this.setInterest(interest);
        this.curRoom = null;
        this.session = session;
        this.banList = new HashSet<>();
    }

    public void enterRoom(ChatRoom room) {
        setCurRoom(room);
    }
//
//    private void setInterest(String[] interest) {
//        this.interest = new ArrayList<>();
//        Collections.addAll(this.interest,interest);
//    }

    public void setCurRoom(ChatRoom room) {
        this.curRoom = room;
    }


    private void setInterest(String[] interest) {
        this.interest = Arrays.asList(interest);
    }

    public String getUsername() {
        return this.username;
    }

    public int getAge() {
        return this.age;
    }

    public String getSchool() {
        return this.school;
    }

    public List<String> getInterest() {
        return this.interest;
    }

    public void blockUser(final String username) {
        banList.add(username);
    }

    public void unblockUser(final String username) {
        banList.remove(username);
    }

    public Session getSession() {
        return this.session;
    }

    public Collection<String> getBanList() {
        return banList;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("into user propertyChange");
        assert evt.getNewValue() instanceof IUserCmd;
        final IUserCmd cmd = (IUserCmd) evt.getNewValue();
        cmd.execute(this);
    }
}
