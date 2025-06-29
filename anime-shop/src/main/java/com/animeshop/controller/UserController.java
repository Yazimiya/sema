package com.animeshop.controller;

import com.animeshop.entity.User;
import com.animeshop.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Controller
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${upload.dir}")
    private String uploadDir;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        User user = userService.findByEmail(currentUser.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") User updatedUser,
                                @RequestParam("avatar") MultipartFile avatar,
                                @AuthenticationPrincipal UserDetails currentUser) throws IOException {

        User user = userService.findByEmail(currentUser.getUsername()).orElseThrow();

        user.setEmail(updatedUser.getEmail());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        if (avatar != null && !avatar.isEmpty()) {
            if (user.getAvatarFilename() != null) {
                File old = Paths.get(uploadDir, "avatars", user.getAvatarFilename()).toFile();
                if (old.exists()) old.delete();
            }

            File avatarsDir = new File(uploadDir + "/avatars");
            if (!avatarsDir.exists()) avatarsDir.mkdirs();

            String filename = System.currentTimeMillis() + "_" + avatar.getOriginalFilename();
            avatar.transferTo(Paths.get(uploadDir, "avatars", filename));
            user.setAvatarFilename(filename);
        }

        userService.save(user);
        return "redirect:/profile?success";
    }
}
