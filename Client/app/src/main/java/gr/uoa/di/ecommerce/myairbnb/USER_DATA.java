package gr.uoa.di.ecommerce.myairbnb;

import android.graphics.Bitmap;


public class USER_DATA {

    private static String IP = "https://192.168.43.143:4000/";

    private static String username;
    private static String password;
    private static String firstName;
    private static String lastName;
    private static String email;
    private static String telephone;
    private static boolean host;
    private static Bitmap userImage_bitmap;
    private static String jwt;

    public static void setIP(String IP){
        USER_DATA.IP = IP;
    }

    static void setUsername(String username) {
        USER_DATA.username = username;
    }
    static void setPassword(String password) {
        USER_DATA.password = password;
    }
    static void setFirstName(String firstName) {
        USER_DATA.firstName = firstName;
    }
    static void setLastName(String lastName) {
        USER_DATA.lastName = lastName;
    }
    static void setEmail(String email) {
        USER_DATA.email = email;
    }
    static void setTelephone(String telephone) {
        USER_DATA.telephone = telephone;
    }
    static void setHost(boolean host) {
        USER_DATA.host = host;
    }
    static void setUserImage_bitmap(Bitmap userImage_bitmap) {
        USER_DATA.userImage_bitmap = userImage_bitmap;
    }
    static void setJWT(String jwt){
        USER_DATA.jwt = jwt;
    }

    static String getUsername() {
        return username;
    }
    static String getPassword() {
        return password;
    }
    static String getFirstName() {
        return firstName;
    }
    static String getLastName() {
        return lastName;
    }
    static String getEmail() {
        return email;
    }
    static String getTelephone() {
        return telephone;
    }
    static boolean isHost() {
        return host;
    }
    static Bitmap getUserImage_bitmap() {
        return userImage_bitmap;
    }

    static String getJWT() {
        return jwt;
    }
    public static String getIP() {
        return IP;
    }
}
