package com.example.api.service;

import com.example.api.model.dto.RegisterUserDto;
import com.example.api.model.dto.LoginDto;
import com.example.api.model.entity.User;

import java.util.List;

public interface UserService {

    User register(RegisterUserDto dto) throws Exception;

    User login(LoginDto dto) throws Exception;

    List<User> findAll();

    User updateStatus(String id, boolean disabled) throws Exception;

}
