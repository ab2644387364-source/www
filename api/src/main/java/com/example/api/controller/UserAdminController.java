package com.example.api.controller;

import com.example.api.model.entity.User;
import com.example.api.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {

    @Resource
    private UserService userService;

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN' ,'ROLE_ADMIN')")
    public List<User> findAll() {
        return userService.findAll();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN' ,'ROLE_ADMIN')")
    public User updateStatus(@PathVariable String id, @RequestParam boolean disabled) throws Exception {
        return userService.updateStatus(id, disabled);
    }
}
