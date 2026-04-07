package com.apple.chain.user.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.common.util.JwtUtil;
import com.apple.chain.user.dto.LoginRequest;
import com.apple.chain.user.dto.LoginResponse;
import com.apple.chain.user.entity.SysRole;
import com.apple.chain.user.entity.User;
import com.apple.chain.user.mapper.UserMapper;
import com.apple.chain.user.service.RbacService;
import com.apple.chain.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RbacService rbacService;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = baseMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new BizException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BizException("账号已被禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }

        // Resolve RBAC payload from sys_user_role / sys_role_permission.
        // Fallback to legacy User.roleCode (V4 single-field) when no sys_user_role row exists,
        // so seeded users without an explicit assignment can still log in during the
        // dual-write transition window.
        List<SysRole> roles = rbacService.getRolesForUser(user.getId());
        List<String> roleCodes = roles.isEmpty() && user.getRoleCode() != null
                ? Collections.singletonList(user.getRoleCode())
                : roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());
        List<String> permissions = rbacService.getPermissionCodesForUser(user.getId());

        String primaryRole = roleCodes.isEmpty() ? user.getRoleCode() : roleCodes.get(0);
        String token = jwtUtil.generateToken(
                user.getId(), user.getUsername(), primaryRole, roleCodes, permissions);

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roleCode(primaryRole)
                .orgName(user.getOrgName())
                .avatar(user.getAvatar())
                .roles(roleCodes)
                .permissions(permissions)
                .build();
    }

    @Override
    public User getProfile(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException("原密码错误");
        }
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6) {
            throw new BizException("新密码不能少于6位");
        }

        User update = new User();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(newPassword));
        updateById(update);
    }

    @Override
    public IPage<User> listUsers(int page, int size, String keyword, String roleCode, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.hasText(keyword), User::getUsername, keyword)
                .or(StringUtils.hasText(keyword), w -> w.like(StringUtils.hasText(keyword), User::getRealName, keyword))
                .eq(StringUtils.hasText(roleCode), User::getRoleCode, roleCode)
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getCreateTime);

        IPage<User> result = page(new Page<>(page, size), wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(User user) {
        User existing = baseMapper.findByUsername(user.getUsername());
        if (existing != null) {
            throw new BizException("用户名已存在");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            throw new BizException("密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        save(user);
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(Long id, User user) {
        User existing = getById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        user.setId(id);
        user.setUsername(null);
        user.setPassword(null);
        updateById(user);
        User updated = getById(id);
        updated.setPassword(null);
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        if (!removeById(id)) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
    }

    @Override
    public void exportUsers(String keyword, String roleCode, Integer status, HttpServletResponse response) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.hasText(keyword), User::getUsername, keyword)
                .eq(StringUtils.hasText(roleCode), User::getRoleCode, roleCode)
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getCreateTime);

        List<User> users = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("用户列表.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("用户ID,用户名,真实姓名,手机号,邮箱,角色,状态,机构名称,创建时间");
            for (User u : users) {
                writer.println(
                        u.getId() + "," +
                        escape(u.getUsername()) + "," +
                        escape(u.getRealName()) + "," +
                        escape(u.getPhone()) + "," +
                        escape(u.getEmail()) + "," +
                        escape(u.getRoleCode()) + "," +
                        (u.getStatus() == 1 ? "启用" : "禁用") + "," +
                        escape(u.getOrgName()) + "," +
                        u.getCreateTime()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
