package com.eventmanagement.controller;

import com.eventmanagement.model.User;
import com.eventmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

/**
 * Controller for user-related operations
 */
@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Show login page
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    /**
     * Process login
     */
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<User> user = userService.authenticateUser(username, password);

            if (user.isPresent()) {
                // Store user in session
                session.setAttribute("user", user.get());

                // Redirect based on user role
                if (user.get().isAdmin()) {
                    return "redirect:/admin/dashboard";
                } else {
                    return "redirect:/";
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid username or password");
                return "redirect:/login";
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error during login: " + e.getMessage());
            return "redirect:/login";
        }
    }

    /**
     * Process logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    /**
     * Show registration page
     */
    @GetMapping("/register")
    public String showRegistrationPage() {
        return "register";
    }

    /**
     * Process registration
     */
    @PostMapping("/register")
    public String processRegistration(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String fullName,
            RedirectAttributes redirectAttributes) {

        try {
            // Check if username already exists
            if (userService.getUserByUsername(username).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Username already exists");
                return "redirect:/register";
            }

            // Create new user
            User user = new User(username, password, email, fullName);
            userService.registerUser(user);

            redirectAttributes.addFlashAttribute("message", "Registration successful. Please login.");
            return "redirect:/login";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error during registration: " + e.getMessage());
            return "redirect:/register";
        }
    }

    /**
     * Show user profile
     */
    @GetMapping("/profile")
    public String showUserProfile(Model model, HttpSession session) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "user-profile";
    }

    /**
     * Update user profile
     */
    @PostMapping("/profile/update")
    public String updateUserProfile(
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam(required = false) String newPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // Check if user is logged in
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return "redirect:/login";
            }

            // Update user information
            user.setEmail(email);
            user.setFullName(fullName);

            // Update password if provided
            if (newPassword != null && !newPassword.isEmpty()) {
                user.setPassword(newPassword);
            }

            userService.updateUser(user);

            // Update session
            session.setAttribute("user", user);

            redirectAttributes.addFlashAttribute("message", "Profile updated successfully");
            return "redirect:/profile";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
            return "redirect:/profile";
        }
    }
}