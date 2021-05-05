package com.example.api.service;

import com.example.api.model.dto.RegisterUserDto;
import com.example.api.model.dto.LoginDto;
import com.example.api.model.entity.User;

public interface UserService {

    User register(RegisterUserDto dto) throws Exception;

    User login(LoginDto dto) throws Exception;

}
