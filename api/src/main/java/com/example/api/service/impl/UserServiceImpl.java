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
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private EmailService emailService;

    @Override
    public User register(RegisterUserDto dto) throws Exception {
        if (dto.getEmail() == null
                || !Pattern.matches("^\\w{3,}(\\.\\w+)*@[A-Za-z0-9]+(\\.[A-Za-z]{2,5}){1,2}$", dto.getEmail())) {
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
        user.setDisabled(false);
        user.setCreateAt(DataTimeUtil.getNowTimeString());
        user.setUpdateAt(DataTimeUtil.getNowTimeString());
        return userRepository.save(user);
    }

    @Override
    public User login(LoginDto dto) throws Exception {
        if (dto.getEmail() == null
                || !Pattern.matches("^\\w{3,}(\\.\\w+)*@[A-Za-z0-9]+(\\.[A-Za-z]{2,5}){1,2}$", dto.getEmail())) {
            throw new Exception("邮箱格式不正确");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new Exception("密码长度至少6位");
        }
        User user = userRepository.findUserByEmailAndPassword(dto.getEmail(), dto.getPassword());
        if (user == null) {
            throw new Exception("用户名或密码错误");
        }
        if (Boolean.TRUE.equals(user.getDisabled())) {
            throw new Exception("账号已被禁用");
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User updateStatus(String id, boolean disabled) throws Exception {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new Exception("用户不存在");
        }
        user.setDisabled(disabled);
        user.setUpdateAt(DataTimeUtil.getNowTimeString());
        return userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("用户不存在");
        }
        return user;
    }

    @Override
    public User updateProfile(String email, User updateUser) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("用户不存在");
        }
        // 更新用户信息（只更新个人信息字段，不更新邮箱和密码）
        if (updateUser.getName() != null) {
            user.setName(updateUser.getName());
        }
        if (updateUser.getPhone() != null) {
            user.setPhone(updateUser.getPhone());
        }
        if (updateUser.getAddress() != null) {
            user.setAddress(updateUser.getAddress());
        }
        if (updateUser.getCompany() != null) {
            user.setCompany(updateUser.getCompany());
        }
        user.setUpdateAt(DataTimeUtil.getNowTimeString());
        return userRepository.save(user);
    }

    @Override
    public void changePassword(String email, String oldPassword, String newPassword) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("用户不存在");
        }
        if (!user.getPassword().equals(oldPassword)) {
            throw new Exception("原密码错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new Exception("新密码长度至少6位");
        }
        user.setPassword(newPassword);
        user.setUpdateAt(DataTimeUtil.getNowTimeString());
        userRepository.save(user);
    }
}
