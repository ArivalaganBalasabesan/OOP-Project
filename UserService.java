package com.eventmanagement.service;

import com.eventmanagement.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing users
 * Implements business logic for user operations
 */
@Service
public class UserService {

    private final FileService fileService;
    private final String USER_FILE = "users.txt";

    @Autowired
    public UserService(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Register a new user
     */
    public User registerUser(User user) throws IOException {
        // Check if username already exists
        if (getUserByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        List<User> users = getAllUsers();
        users.add(user);
        saveUsers(users);

        return user;
    }

    /**
     * Authenticate a user
     */
    public Optional<User> authenticateUser(String username, String password) throws IOException {
        return getAllUsers().stream()
                .filter(user -> user.getUsername().equals(username) && user.getPassword().equals(password))
                .findFirst();
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() throws IOException {
        List<String> lines = fileService.readFile(USER_FILE);
        List<User> users = new ArrayList<>();

        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                users.add(User.fromFileString(line));
            }
        }

        return users;
    }

    /**
     * Get user by ID
     */
    public Optional<User> getUserById(String id) throws IOException {
        return getAllUsers().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    /**
     * Get user by username
     */
    public Optional<User> getUserByUsername(String username) throws IOException {
        return getAllUsers().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    /**
     * Update user information
     */
    public User updateUser(User updatedUser) throws IOException {
        List<User> users = getAllUsers();

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(updatedUser.getId())) {
                users.set(i, updatedUser);
                break;
            }
        }

        saveUsers(users);
        return updatedUser;
    }

    /**
     * Delete a user
     */
    public boolean deleteUser(String id) throws IOException {
        List<User> users = getAllUsers();
        boolean removed = users.removeIf(user -> user.getId().equals(id));

        if (removed) {
            saveUsers(users);
        }

        return removed;
    }

    /**
     * Save users to file
     */
    private void saveUsers(List<User> users) throws IOException {
        List<String> lines = users.stream()
                .map(User::toFileString)
                .collect(Collectors.toList());

        fileService.writeFile(USER_FILE, lines);
    }

    /**
     * Get all admin users
     */
    public List<User> getAllAdmins() throws IOException {
        return getAllUsers().stream()
                .filter(User::isAdmin)
                .collect(Collectors.toList());
    }
}