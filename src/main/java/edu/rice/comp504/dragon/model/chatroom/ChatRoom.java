package edu.rice.comp504.dragon.model.chatroom;

import edu.rice.comp504.dragon.model.DispatchAdapter;
import edu.rice.comp504.dragon.model.message.Message;
import edu.rice.comp504.dragon.model.msgstrategy.ReportMsg;
import edu.rice.comp504.dragon.model.user.User;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ChatRoom extends AChatRoom{

    /**
     * Restrictions.
     **/
    private Point ageRange;
    private List<String> schoolList;
    private List<String> interestList;

    private Set<String> banList;

    /**
     * Constructor of the edu.rice.comp504.dragon.model.chatroom without restrictions
     * */
    public ChatRoom(User admin, String roomName) {
        super(admin, roomName);

        this.ageRange = new Point(0, 200);
        initSchoolList();
        initInterestList();
        banList = new HashSet<>();
    }

    @Override
    public boolean canUserJoin(final User user) {
        return checkRestriction(user);
    }

    /**
     * comments.
     * @param admin a.
     * @param roomName r.
     * @param ageRange a.
     * @param schoolList s.
     * @param interestList i.
     */
    public ChatRoom(User admin, String roomName, Point ageRange, String[] schoolList, String[] interestList) {
        super(admin, roomName);
        this.roomName = roomName;
        if (ageRange == null) {
            this.ageRange = new Point(0, 200);
        } else {
            this.ageRange = ageRange;
        }
        setSchoolList(schoolList);
        setInterestList(interestList);
        banList = new HashSet<>();
    }



    @Override
    public List<User> getUsers() {
        return DispatchAdapter.getInstance().listUser(this.roomName);
    }

    @Override
    public boolean msgCheck(Message msg) {
        if (banList.contains(msg.getUser().getUsername())) {
            return false;
        }

        final boolean containBanWords = msg.isUserMsg() && banWords.stream().anyMatch(msg.getContext().toLowerCase()::contains);
        return !containBanWords;


//        if(msg.getContext() == null)
//            return true;
//        final boolean containBanWords = banWords.stream().anyMatch(msg.getContext().toLowerCase()::contains);
//        return !containBanWords;

    }

    @Override
    public Map<String, String> getRoomInfos() {
        final Map<String,String> infos = new HashMap<>();
        infos.put("ageRange", String.format("%d ~ %d", ageRange.x, ageRange.y));
        infos.put("schoolList",schoolList.stream().collect(Collectors.joining(",")));
        infos.put("interestList",interestList.stream().collect(Collectors.joining(",")));
        infos.put("admin", admin.getUsername());
        infos.put("roomName", roomName);
        return infos;

    }

    @Override
    public boolean isViewableByUser(User user) {
        return true;
    }

    void initSchoolList() {

        schoolList = new ArrayList<>();
    }

//    void initInterestList() {
//        interestList = new ArrayList<>();
//        // todo: init SchoolList
//        this.schoolList= new ArrayList<>();
//    }
    void initInterestList() {
        // todo: init interestList
        this.interestList = new ArrayList<>();
    }

    void setSchoolList(String[] schoolList) {
        this.schoolList = Arrays.asList(schoolList);
    }

    void setInterestList(String[] interestList) {
        this.interestList = Arrays.asList(interestList);
    }

    public void banUser(final String userName) {
        banList.add(userName);
    }

    public void unbanUser(final String userName) {
        banList.remove(userName);
    }

    /**
     * comments.
     * @param newUser new.
     * @return rt.
     */
    public boolean checkRestriction(User newUser) {
        int age = newUser.getAge();
//        if (age < ageRange.x || age > ageRange.y || (!schoolList.isEmpty() &&!schoolList.contains(newUser.getSchool()))) {

        if ((ageRange.x != 0 && ageRange.y != 200) && (age < ageRange.x || age > ageRange.y) || (!schoolList.contains(newUser.getSchool()) && schoolList.size() != 0)) {
            return false;
        }
        for (String interest : interestList) {
            if (!newUser.getInterest().contains(interest)) {
                return false;
            }
        }
        return true;
    }

    public Set<String> getBanList() {
        return banList;
    }

}
