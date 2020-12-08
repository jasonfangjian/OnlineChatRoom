package edu.rice.comp504.dragon.model;


import edu.rice.comp504.dragon.model.user.User;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentMatcher;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * ALL TESTCASE SHOULD BE RUN IN SEQUENTIAL ORDER
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DispatchAdapterTest {

    private static User initUser;
    private static User testUser;
    private static User blockTester;
    private static DispatchAdapter adapter;

    @BeforeClass
    public static void initAdapter() {
        adapter = DispatchAdapter.getInstance();
        final Session session = mock(Session.class);
        final RemoteEndpoint remote = mock(RemoteEndpoint.class);
        when(session.getRemote()).thenReturn(remote);
        final JSONObject request = new JSONObject();
        request.put("username", "tester0");
        request.put("age", "23");
        request.put("school", "Rice");
        request.put("interestList", Collections.singletonList("null"));
        initUser = adapter.login(session, request);
        //System.out.println(initUser.getUsername());
        adapter.createRoom(initUser,
                "Room0",
                new Point(0, 50),
                new String[]{"SOME_SCHOOL"},
                new String[]{"CODING"},
                false);
    }

    @Test
    public void t01_loginTest() {
        final Session session = mock(Session.class);
        final RemoteEndpoint remote = mock(RemoteEndpoint.class);
        final String[] interests = new String[]{"null"};

        when(session.getRemote()).thenReturn(remote);

        final JSONObject request = new JSONObject();
        request.put("username", "tester");
        request.put("age", "23");
        request.put("school", "Rice");
        request.put("interestList", Arrays.asList(interests));

        testUser = adapter.login(session, request);

        try {
            verify(remote, times(1))
                    .sendString(eq("{\"visibleRooms\":[\"Room0\"],\"type\":\"loginResponse\",\"username\":\"tester\"}"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void t02_loginErrorTest() {
        final Session session = mock(Session.class);
        final RemoteEndpoint remote = mock(RemoteEndpoint.class);
        final String[] interests = new String[]{"null"};

        when(session.getRemote()).thenReturn(remote);

        final JSONObject request = new JSONObject();
        request.put("username", "tester");
        request.put("age", "23");
        request.put("school", "Rice");
        request.put("interestList", Arrays.asList(interests));

        adapter.login(session, request);

        try {
            verify(remote, times(1))
                    .sendString(eq("{\"type\":\"loginFailed\",\"info\":\"invalidUsername\"}"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void t03_createRoomTest() {
        adapter.createRoom(initUser,
                "testRoom",
                new Point(0, 100),
                new String[]{"Rice"},
                new String[]{"null"},
                false);
        adapter.listUser(null).forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(1))
                        .sendString(eq("{\"schoolList\":\"Rice\"," +
                                "\"ageRange\":[0,100]," +
                                "\"msgType\":\"newPublicRoom\"," +
                                "\"interest\":\"null\"," +
                                "\"admin\":\"tester0\"," +
                                "\"roomName\":\"testRoom\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t04_createRoomErrorTest() throws IOException {
        adapter.createRoom(testUser,
                "Room0",
                new Point(0, 100),
                new String[]{"Rice"},
                new String[]{"null"},
                false);
        final RemoteEndpoint remote = testUser.getSession().getRemote();
        verify(remote, times(1))
                .sendString(eq("{\"type\":\"createRoomFailed\",\"info\":\"invalidRoomName\"}"));
    }

    @Test
    public void t05_joinRoomTest() {
        adapter.joinRoom(testUser, "testRoom");
        adapter.listUser("testRoom").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(1))
                        .sendString(argThat(getJoinMsgMatcher(testUser, "testRoom")));
                verify(remote, times(user.equals(testUser) ? 1 : 0))
                        .sendString(argThat(getWelComMsgMatcher(initUser, "testRoom")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t06_sendMsgTest() {
        adapter.sendUserMsg(testUser, "testMsg", "testRoom", 12L);
        adapter.listUser("testRoom").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(1))
                        .sendString(eq("{\"msg\":\"testMsg\"," +
                                "\"msgType\":\"UserMsg\"," +
                                "\"msgId\":\"12tester\"," +
                                "\"from\":\"tester\"," +
                                "\"time\":\"1969-12-31T18:00:00.012\"," +
                                "\"room\":\"testRoom\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t07_editMsgTest() {
        adapter.sendUserMsg(testUser, "editMsgBase", "testRoom", 13L);
        adapter.editMsg(testUser, "testRoom", "13tester", "editMsgResult", 14L);
        adapter.listUser("testRoom").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(1))
                        .sendString(eq("{\"msg\":\"editMsgBase\"," +
                                "\"msgType\":\"UserMsg\"," +
                                "\"msgId\":\"13tester\"," +
                                "\"from\":\"tester\",\"" +
                                "time\":\"1969-12-31T18:00:00.013\"," +
                                "\"room\":\"testRoom\"}"));
                verify(remote, times(1))
                        .sendString(eq("{\"msg\":\"tester edited the message at 1969-12-31T18:00:00.014\"," +
                                "\"UPDATE_MSG\":\"editMsgResult\"," +
                                "\"msgType\":\"EditMsg\"," +
                                "\"DELETE_MSG\":\"13tester\"," +
                                "\"msgId\":\"14tester\"," +
                                "\"from\":\"tester\"," +
                                "\"time\":\"1969-12-31T18:00:00.014\"," +
                                "\"room\":\"testRoom\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t08_editErrorTest() {
        adapter.sendUserMsg(testUser, "editMsgBase", "testRoom", 15L);
        adapter.editMsg(initUser, "testRoom", "15tester", "editMsgResult", 16L);
        adapter.listUser("testRoom").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(1))
                        .sendString(eq("{\"msg\":\"editMsgBase\"," +
                                "\"msgType\":\"UserMsg\"," +
                                "\"msgId\":\"15tester\"," +
                                "\"from\":\"tester\"," +
                                "\"time\":\"1969-12-31T18:00:00.015\"," +
                                "\"room\":\"testRoom\"}"));
                verify(remote, times(user.equals(initUser) ? 1 : 0))
                        .sendString(eq("{\"reason\":\"permission denied\"," +
                                "\"msgType\":\"editMsgError\"," +
                                "\"target\":\"15tester\"}"));
                verify(remote, never())
                        .sendString(eq("{\"msg\":\"tester0 edited the message at 1969-12-31T18:00:00.016\"," +
                                "\"UPDATE_MSG\":\"editMsgResult\"," +
                                "\"msgType\":\"EditMsg\"," +
                                "\"DELETE_MSG\":\"15tester\"," +
                                "\"msgId\":\"16tester0\"," +
                                "\"from\":\"tester0\"," +
                                "\"time\":\"1969-12-31T18:00:00.016\"," +
                                "\"room\":\"testRoom\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t09_recallMsgTest() {
        adapter.sendUserMsg(testUser, "recallMsgBase", "testRoom", 17L);
        adapter.recallOrDeleteMsg(testUser, "testRoom", "17tester", "recallMsg", 18L);
        adapter.listUser("testRoom").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(1))
                        .sendString(eq("{\"msg\":\"recallMsgBase\"," +
                                "\"msgType\":\"UserMsg\"," +
                                "\"msgId\":\"17tester\"," +
                                "\"from\":\"tester\"," +
                                "\"time\":\"1969-12-31T18:00:00.017\"," +
                                "\"room\":\"testRoom\"}"));
                verify(remote, times(1))
                        .sendString(eq("{\"msg\":\"tester recalled the message at 1969-12-31T18:00:00.018\"," +
                                "\"msgType\":\"RecallMsg\"," +
                                "\"DELETE_MSG\":\"17tester\"," +
                                "\"msgId\":\"18tester\"," +
                                "\"from\":\"tester\"," +
                                "\"time\":\"1969-12-31T18:00:00.018\"," +
                                "\"room\":\"testRoom\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t10_recallErrorTest() {
        adapter.sendUserMsg(testUser, "recallMsgBase", "testRoom", 19L);
        adapter.recallOrDeleteMsg(initUser, "testRoom", "19tester", "recallMsg", 20L);
        adapter.listUser("testRoom").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(1))
                        .sendString(eq("{\"msg\":\"recallMsgBase\"," +
                                "\"msgType\":\"UserMsg\"," +
                                "\"msgId\":\"19tester\"," +
                                "\"from\":\"tester\"," +
                                "\"time\":\"1969-12-31T18:00:00.019\"," +
                                "\"room\":\"testRoom\"}"));
                verify(remote, times(user.equals(initUser) ? 1 : 0))
                        .sendString(eq("{\"reason\":\"permission denied\"," +
                                "\"msgType\":\"recallMsgError\"," +
                                "\"target\":\"19tester\"}"));
                verify(remote, never())
                        .sendString(eq("{\"msg\":\"tester0 recalled the message at 1969-12-31T18:00:00.020\"," +
                                "\"msgType\":\"RecallMsg\"," +
                                "\"DELETE_MSG\":\"19tester\"," +
                                "\"msgId\":\"20tester0\"," +
                                "\"from\":\"tester0\"," +
                                "\"time\":\"1969-12-31T18:00:00.020\"," +
                                "\"room\":\"testRoom\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t11_deleteMsgTest() {
        adapter.sendUserMsg(testUser, "delMsgBase", "testRoom", 21L);
        adapter.recallOrDeleteMsg(initUser, "testRoom", "21tester", "deleteMsg", 22L);
        adapter.listUser("testRoom").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(1))
                        .sendString(eq("{\"msg\":\"delMsgBase\"," +
                                "\"msgType\":\"UserMsg\"," +
                                "\"msgId\":\"21tester\"," +
                                "\"from\":\"tester\"," +
                                "\"time\":\"1969-12-31T18:00:00.021\"," +
                                "\"room\":\"testRoom\"}"));
                verify(remote, times(1))
                        .sendString(eq("{\"msg\":\"tester0 deleted the message at 1969-12-31T18:00:00.022\"," +
                                "\"msgType\":\"DeleteMsg\"," +
                                "\"DELETE_MSG\":\"21tester\"," +
                                "\"msgId\":\"22tester0\"," +
                                "\"from\":\"tester0\"," +
                                "\"time\":\"1969-12-31T18:00:00.022\"," +
                                "\"room\":\"testRoom\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t12_delErrorTest() {
        adapter.sendUserMsg(testUser, "delMsgBase", "testRoom", 23L);
        adapter.recallOrDeleteMsg(testUser, "testRoom", "23tester", "deleteMsg", 24L);
        adapter.listUser("testRoom").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(1))
                        .sendString(eq("{\"msg\":\"delMsgBase\"," +
                                "\"msgType\":\"UserMsg\"," +
                                "\"msgId\":\"23tester\"," +
                                "\"from\":\"tester\"," +
                                "\"time\":\"1969-12-31T18:00:00.023\"," +
                                "\"room\":\"testRoom\"}"));
                verify(remote, times(user.equals(testUser) ? 1 : 0))
                        .sendString(eq("{\"reason\":\"permission denied\"," +
                                "\"msgType\":\"deleteMsgError\"," +
                                "\"target\":\"23tester\"}"));
                verify(remote, never())
                        .sendString(eq("{\"msg\":\"tester deleted the message at 1969-12-31T18:00:00.024\"," +
                                "\"msgType\":\"DeleteMsg\"," +
                                "\"DELETE_MSG\":\"23tester\"," +
                                "\"msgId\":\"24tester\"," +
                                "\"from\":\"tester\"," +
                                "\"time\":\"1969-12-31T18:00:00.024\"," +
                                "\"room\":\"testRoom\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t13_blockTest() throws IOException {
        final Session session = mock(Session.class);
        final RemoteEndpoint remote = mock(RemoteEndpoint.class);
        final String[] interests = new String[]{"null"};

        when(session.getRemote()).thenReturn(remote);

        final JSONObject request = new JSONObject();
        request.put("username", "testerB");
        request.put("age", "23");
        request.put("school", "Rice");
        request.put("interestList", Arrays.asList(interests));

        blockTester = adapter.login(session, request);
        adapter.joinRoom(blockTester, "testRoom");
        adapter.blockUser(testUser, "testerB");
        adapter.sendUserMsg(blockTester, "blockTest0", "testRoom", 31L);

        verify(testUser.getSession().getRemote(), never())
                .sendString(eq("{\"msg\":\"testerB said blockTest0 at 31\"," +
                        "\"msgType\":\"UserMsg\"," +
                        "\"msgId\":\"31testerB\"," +
                        "\"from\":\"testerB\"," +
                        "\"time\":\"31\"," +
                        "\"room\":\"testRoom\"}"));
    }

    @Test
    public void t14_unblockTest() throws IOException {
        adapter.unblockUser(testUser, "testerB");
        adapter.sendUserMsg(blockTester, "blockTest1", "testRoom", 32L);

        verify(testUser.getSession().getRemote(), times(1))
                .sendString(eq("{\"msg\":\"blockTest1\"," +
                        "\"msgType\":\"UserMsg\"," +
                        "\"msgId\":\"32testerB\"," +
                        "\"from\":\"testerB\"," +
                        "\"time\":\"1969-12-31T18:00:00.032\"," +
                        "\"room\":\"testRoom\"}"));
    }

    @Test
    public void t15_banTest() {
        adapter.banUser(initUser, "testRoom", "testerB");
        adapter.sendUserMsg(blockTester, "blockTest2", "testRoom", 33L);

        adapter.listUser("testRoom").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(1))
                        .sendString(argThat(getBanMsgMatcher(blockTester,initUser,"testRoom", false)));
                verify(remote, never())
                        .sendString(eq("{\"msg\":\"blockTest2\"," +
                                "\"msgType\":\"UserMsg\"," +
                                "\"msgId\":\"33testerB\"," +
                                "\"from\":\"testerB\"," +
                                "\"time\":\"33\"," +
                                "\"room\":\"testRoom\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t16_unbanTest() {
        adapter.unbanUser(initUser, "testRoom", "testerB");
        adapter.sendUserMsg(blockTester, "blockTest3", "testRoom", 34L);

        adapter.listUser("testRoom").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(1))
                        .sendString(argThat(getBanMsgMatcher(blockTester,initUser,"testRoom", true)));
                verify(remote, times(1))
                        .sendString(eq("{\"msg\":\"blockTest3\"," +
                                "\"msgType\":\"UserMsg\"," +
                                "\"msgId\":\"34testerB\"," +
                                "\"from\":\"testerB\"," +
                                "\"time\":\"1969-12-31T18:00:00.034\"," +
                                "\"room\":\"testRoom\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t17_reportTest() {
        adapter.reportUser(testUser, "testRoom", "testerB", 35L);
        adapter.listUser("testRoom").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(user.equals(initUser) ? 1 : 0))
                        .sendString(eq("{\"msg\":\"tester report user:testerB at 1969-12-31T18:00:00.035\"," +
                                "\"msgType\":\"ReportMsg\"," +
                                "\"msgId\":\"35tester\"," +
                                "\"from\":\"tester\"," +
                                "\"time\":\"1969-12-31T18:00:00.035\"," +
                                "\"room\":\"testRoom\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t18_kickOutTest() {
        adapter.kickOutUser(initUser, "testRoom", "testerB");
        adapter.listUser("testRoom").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(1))
                        .sendString(argThat(getLeavingMsgMatcher(blockTester,
                                "testRoom",
                                "admin kick out")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t19_createDMRoomTest() {
        adapter.createRoom(blockTester, testUser.getUsername());
        adapter.listUser(null).forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            final int time = (user.equals(blockTester) || user.equals(testUser)) ? 1 : 0;
            try {
                verify(remote, times(time))
                        .sendString(eq("{\"msgType\":\"newPrivateRoom\"," +
                                "\"admin\":\"testerB\"," +
                                "\"roomName\":\"DM(testerB,tester)\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t20_createDMRoomErrorTest() throws IOException {
        adapter.createRoom(blockTester, "111");
        final RemoteEndpoint remote = blockTester.getSession().getRemote();
        verify(remote, times(1))
                .sendString(eq("{\"type\":\"createRoomFailed\",\"info\":\"invalidGuestName\"}"));
    }

    @Test
    public void t21_leaveDMRoomTest() throws IOException {
        adapter.leaveRoom("DM(testerB,tester)", testUser);
        verify(blockTester.getSession().getRemote(), times(1))
                .sendString("{\"msgType\":\"removeRoom\",\"roomName\":\"DM(testerB,tester)\"}");
        verify(testUser.getSession().getRemote(), times(1))
                .sendString("{\"msgType\":\"removeRoom\",\"roomName\":\"DM(testerB,tester)\"}");
    }

    @Test
    public void t22_createPrivateRoomTest() {
        adapter.createRoom(initUser,
                "PriRoom",
                true);
        adapter.listUser(null).forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(user.equals(initUser) ? 1 : 0))
                        .sendString(eq("{\"msgType\":\"newPrivateRoom\"," +
                                "\"admin\":\"tester0\"," +
                                "\"roomName\":\"PriRoom\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    @Test
    public void t23_createPrivateRoomErrorTest() throws IOException {
        adapter.createRoom(blockTester,
                "PriRoom",
                true);
        final RemoteEndpoint remote = blockTester.getSession().getRemote();
        verify(remote, times(1))
                .sendString(eq("{\"type\":\"createRoomFailed\",\"info\":\"invalidRoomName\"}"));
    }


    @Test
    public void t24_inviteTest(){
        adapter.inviteUser(initUser, "PriRoom", new String[]{testUser.getUsername()});
        adapter.listUser(null).forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(user.equals(initUser) ? 1 : 0))
                        .sendString(eq("{\"type\":\"inviteUserSuccess\",\"info\":\"tester\"}"));
                verify(remote, times(user.equals(testUser) ? 1 : 0))
                        .sendString(argThat(getInviteMsgMatcher(initUser,"PriRoom")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t25_joinPrivateRoomTest() {
        adapter.joinRoom(testUser, "PriRoom");
        adapter.listUser("PriRoom").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(1))
                        .sendString(argThat(getJoinMsgMatcher(testUser, "PriRoom")));
                verify(remote, times(user.equals(testUser) ? 1 : 0))
                        .sendString(argThat(getWelComMsgMatcher(initUser, "PriRoom")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t26_joinPrivateRoomErrorTest() {
        adapter.joinRoom(blockTester, "PriRoom");
        try {
            verify(blockTester.getSession().getRemote(), times(1))
                    .sendString("{\"type\":\"joinRoomFailed\",\"info\":\"cannot join this room\"}");
        } catch (IOException e) {
            e.printStackTrace();
        }
        adapter.listUser("PriRoom").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, never())
                        .sendString(argThat(getJoinMsgMatcher(blockTester, "PriRoom")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t27_leavingRoomTest() {
        adapter.leaveRoom("testRoom", testUser);
        adapter.listUser("testRoom").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(1))
                        .sendString(argThat(getLeavingMsgMatcher(testUser, "testRoom", "user leaved room")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t28_adminLeavingRoomTest() {
        adapter.leaveRoom("testRoom", initUser);
        adapter.listUser(null).forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, times(1))
                        .sendString("{\"msgType\":\"removeRoom\",\"roomName\":\"testRoom\"}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void t29_joinRoomErrorTest() {
        adapter.joinRoom(testUser, "Room0");
        try {
            verify(testUser.getSession().getRemote(), times(1))
                    .sendString("{\"type\":\"joinRoomFailed\",\"info\":\"cannot join this room\"}");
        } catch (IOException e) {
            e.printStackTrace();
        }
        adapter.listUser("Room0").forEach(user -> {
            final RemoteEndpoint remote = user.getSession().getRemote();
            try {
                verify(remote, never())
                        .sendString(argThat(getJoinMsgMatcher(testUser, "Room0")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static ArgumentMatcher<String> getJoinMsgMatcher(final User user, final String roomName) {
        return str -> {
            final JSONObject jo = new JSONObject(str);
            try {
                return jo.getString("from").equals(user.getUsername())
                        && jo.getString("ADD_USER").equals(user.getUsername())
                        && jo.getString("room").equals(roomName)
                        && jo.getString("msgType").equals("JoinRoom");
            } catch (Exception e) {
                return false;
            }
        };
    }

    private static ArgumentMatcher<String> getLeavingMsgMatcher(final User user,
                                                                final String roomName,
                                                                final String reason) {
        return str -> {
            final JSONObject jo = new JSONObject(str);
            try {
                return jo.getString("REMOVE_USER").equals(user.getUsername())
                        && jo.getString("room").equals(roomName)
                        && jo.getString("msgType").equals("LeaveRoom")
                        && jo.getString("REMOVE_REASON").equals(reason);
            } catch (Exception e) {
                return false;
            }
        };
    }

    private static ArgumentMatcher<String> getWelComMsgMatcher(final User user, final String roomName) {
        return str -> {
            final JSONObject jo = new JSONObject(str);
            try {
                return jo.getString("from").equals(user.getUsername())
                        && jo.getString("room").equals(roomName)
                        && jo.getString("msgType").equals("welcome");
            } catch (Exception e) {
                return false;
            }
        };
    }

    private static ArgumentMatcher<String> getInviteMsgMatcher(final User user, final String roomName) {
        return str -> {
            final JSONObject jo = new JSONObject(str);
            try {
                return jo.getString("from").equals(user.getUsername())
                        && jo.getString("room").equals(roomName)
                        && jo.getString("msgType").equals("invitation");
            } catch (Exception e) {
                return false;
            }
        };
    }

    private static ArgumentMatcher<String> getBanMsgMatcher(final User user,
                                                            final User from,
                                                            final String roomName,
                                                            final boolean unban) {
        return str -> {
            final JSONObject jo = new JSONObject(str);
            try {
                return jo.getString("from").equals(from.getUsername())
                        && jo.getString("msg").equals(user.getUsername())
                        && jo.getString("room").equals(roomName)
                        && jo.getString("msgType").equals(unban ? "UnBan" : "Ban");
            } catch (Exception e) {
                return false;
            }
        };
    }
}
