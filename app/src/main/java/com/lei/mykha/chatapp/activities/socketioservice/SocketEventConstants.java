package com.lei.mykha.chatapp.activities.socketioservice;

/**
 * Created by mykha on 01/06/2017.
 */

public class SocketEventConstants {
    public static String socketConnection = "socket.connection";

    public static String login = "login";
    public static String loginResponse = "login response";

    public static String register = "register";
    public static String registerResponse = "register response";

    public static String updateToken = "token update";
    public static String deleteToken = "token delete";

    public static String conversations = "query conversations";
    public static String conversationsResponse = "conversations response";

    public static String friendlist = "query friends";
    public static String friendlistResponse = "friendlist response";

    public static String searchPerson = "find person";
    public static String searchPersonResponse = "person response";

    public static String queryMessages = "query messages";
    public static String queryMessagesResponse = "conversation messages";

    public static String acceptFriend = "accept friend";
    public static String sendFriendRequest = "friend request";

    public static String sendMessage = "send message";
    public static String startConversation = "start conversation";
    public static String startConversationResponse = "start conversation response";

    public static String connectionFailure = "failedToConnect";


}
