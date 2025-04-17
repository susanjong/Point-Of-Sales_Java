package com.example.uts_pbo;

public class UserSession {
    private static User currentUser;
    
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public static boolean isAdmin() {
        return isLoggedIn() && currentUser.isAdmin();
    }
    
    public static void logout() {
        currentUser = null;
    }
    
    public static int getUserId() {
        return isLoggedIn() ? currentUser.getId() : -1;
    }
    
    public static String getUsername() {
        return isLoggedIn() ? currentUser.getUsername() : "";
    }
}