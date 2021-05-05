package com.example.api.service.impl;

import com.example.api.model.dto.RegisterUserDto;
import com.example.api.model.dto.LoginDto;
import com.example.api.model.entity.User;
import com.example.api.repository.UserRepository;
import com.example.api.service.EmailService;
import com.example.api.service.UserService;
import com.example.api.utils.DataTimeUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private EmailService emailService;

    @Override
    public User register(RegisterUserDto dto) throws Exception {
        if (dto.getEmail() == null || !Pattern.matches("^\\w{3,}(\\.\\w+)*@[A-Za-z0-9]+(\\.[A-Za-z]{2,5}){1,2}$", dto.getEmail())) {
            throw new Exception("邮箱格式不正确");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new Exception("密码长度至少6位");
        }
        boolean status = emailService.checkVerificationCode(dto.getEmail(), dto.getCode());
        if (!status) {
            throw new Exception("验证码错误");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new Exception("邮箱已注册");
        }
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setCreateAt(DataTimeUtil.getNowTimeString());
        return userRepository.save(user);
    }

    @Override
    public User login(LoginDto dto) throws Exception {
        if (dto.getEmail() == null || !Pattern.matches("^\\w{3,}(\\.\\w+)*@[A-Za-z0-9]+(\\.[A-Za-z]{2,5}){1,2}$", dto.getEmail())) {
            throw new Exception("邮箱格式不正确");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new Exception("密码长度至少6位");
        }
        User user = userRepository.findUserByEmailAndPassword(dto.getEmail(), dto.getPassword());
        if (user == null) {
            throw new Exception("用户名或密码错误");
        }
        return user;
    }
}
