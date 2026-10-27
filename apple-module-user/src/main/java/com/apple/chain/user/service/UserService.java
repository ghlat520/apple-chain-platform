package com.apple.chain.user.service;

import com.apple.chain.user.dto.LoginRequest;
import com.apple.chain.user.dto.LoginResponse;
import com.apple.chain.user.dto.SmsLoginRequest;
import com.apple.chain.user.entity.User;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * User service interface.
 */
public interface UserService extends IService<User> {

    LoginResponse login(LoginRequest request);

    void sendSmsCode(String phone);

    LoginResponse smsLogin(SmsLoginRequest request);

    User getProfile(Long userId);

    void changePassword(Long userId, String oldPassword, String newPassword);

    IPage<User> listUsers(int page, int size, String keyword, String roleCode, Integer status);

    User createUser(User user);

    User updateUser(Long id, User user);

    void deleteUser(Long id);

    void exportUsers(String keyword, String roleCode, Integer status, HttpServletResponse response);
}
