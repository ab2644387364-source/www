package com.example.api.controller;

import com.example.api.model.dto.RegisterUserDto;
import com.example.api.model.dto.LoginDto;
import com.example.api.model.entity.User;
import com.example.api.service.EmailService;
import com.example.api.service.UserService;
import com.example.api.utils.JwtTokenUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private EmailService emailService;

    @PostMapping("/register")
    public User register(@RequestBody RegisterUserDto dto) throws Exception {
        return userService.register(dto);
    }

    @PostMapping("/login")
    public java.util.Map<String, Object> login(@RequestBody LoginDto dto) throws Exception {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        User user = userService.login(dto);
        String token = JwtTokenUtil.createToken(user.getEmail(), new String[] { "ROLE_USER" },
                JwtTokenUtil.EXPIRATION_TIME);
        map.put("user", user);
        map.put("token", token);
        return map;
    }

    @GetMapping("/sendEmail")
    public void sendEmail(String email) throws Exception {
        emailService.sendVerificationCode(email);
    }

    @GetMapping("/profile")
    public User getProfile(
            @org.springframework.web.bind.annotation.RequestHeader("Authorization") String token)
            throws Exception {
        String email = JwtTokenUtil.getUsername(token.replace("Bearer ", ""));
        return userService.findByEmail(email);
    }

    @org.springframework.web.bind.annotation.PutMapping("/profile")
    public User updateProfile(
            @org.springframework.web.bind.annotation.RequestHeader("Authorization") String token,
            @RequestBody User updateUser) throws Exception {
        String email = JwtTokenUtil.getUsername(token.replace("Bearer ", ""));
        return userService.updateProfile(email, updateUser);
    }

    @org.springframework.web.bind.annotation.PutMapping("/password")
    public java.util.Map<String, Object> changePassword(
            @org.springframework.web.bind.annotation.RequestHeader("Authorization") String token,
            @RequestBody java.util.Map<String, String> body) throws Exception {
        String email = JwtTokenUtil.getUsername(token.replace("Bearer ", ""));
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        userService.changePassword(email, oldPassword, newPassword);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("message", "密码修改成功");
        return result;
    }

}
