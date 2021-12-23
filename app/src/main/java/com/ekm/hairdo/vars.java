package com.ekm.hairdo;

import java.util.ArrayList;

public class vars {

    public static final String USERSFAVS = "usersFavs";
    public static final String USERS_DATA = "usersData";
    public static final String ADDRESS = "address";
    public static String STYLES = "HAIR_STYLES3";
    public static String MESSAGES = "messages";
    public static String CHATLINK = "_chatlink_";
    public static String NONE = "none";
    public static String otherUID = "stylistUID";
    public static String stylistAddress = "stylistAddress";
    public static String stylistName = "stylistName";
    public static String stylistLat = "stylistLat";
    public static String stylistLng = "stylistLng";
    public static String chatDetails = "chatDetails";
    public static String messages = "MESSAGES_TREE";

    public static int getHisUIDPosition(String myUID, String hisUID) {
        String a = myUID;
        String b = hisUID;
        System.out.println( a+"____"+b);

        int c = a.compareTo(b);

        if (c<0) {
            return 1;
        } else {
            return 0;
        }
    }

}
