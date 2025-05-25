package com.eventmanagement.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a user in the system.
 * This class demonstrates encapsulation through private fields and getter/setter methods.
 */
public class User implements Serializable {
    private String id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private boolean isAdmin;

    // Default constructor - needed for deserialization
    public User() {
        this.id = UUID.randomUUID().toString();
        this.isAdmin = false;
    }

    // Parameterized constructor
    public User(String username, String password, String email, String fullName) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.isAdmin = false;
    }

    // Admin constructor
    public User(String username, String password, String email, String fullName, boolean isAdmin) {
        this(username, password, email, fullName);
        this.isAdmin = isAdmin;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    // For file storage - convert to string format
    public String toFileString() {
        return id + "|" + username + "|" + password + "|" + email + "|" + fullName + "|" + isAdmin;
    }

    // For file storage - parse from string format
    public static User fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid user data: " + line);
        }

        User user = new User();
        user.setId(parts[0]);
        user.setUsername(parts[1]);
        user.setPassword(parts[2]);
        user.setEmail(parts[3]);
        user.setFullName(parts[4]);
        user.setAdmin(Boolean.parseBoolean(parts[5]));

        return user;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}