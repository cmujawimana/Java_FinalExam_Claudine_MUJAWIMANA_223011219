package com.security.app.models;

public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String email;
    private String fullName;
    private String role;

    public User(int userId, String username, String passwordHash, String email, String fullName, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    // getters & setters (only getters used below for brevity)
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
}
