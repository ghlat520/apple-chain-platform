package com.apple.domain.service;

import com.apple.common.exception.BusinessException;
import com.apple.domain.entity.SysUser;
import com.apple.domain.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    public SysUser findByUsername(String username) {
        return sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    public SysUser getById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw BusinessException.of(404, "User not found: " + id);
        }
        return user;
    }

    public Page<SysUser> listPage(int page, int size, String keyword, String roleCode, String status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getPhone, keyword);
        }
        if (StringUtils.isNotBlank(roleCode)) {
            wrapper.eq(SysUser::getRoleCode, roleCode);
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);
        return sysUserMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public SysUser create(SysUser user) {
        SysUser existing = findByUsername(user.getUsername());
        if (existing != null) {
            throw BusinessException.of(400, "Username already exists: " + user.getUsername());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus("active");
        }
        sysUserMapper.insert(user);
        return user;
    }

    public SysUser update(Long id, SysUser update) {
        SysUser existing = getById(id);
        existing.setRealName(update.getRealName());
        existing.setPhone(update.getPhone());
        existing.setEmail(update.getEmail());
        existing.setRoleCode(update.getRoleCode());
        existing.setStatus(update.getStatus());
        sysUserMapper.updateById(existing);
        return existing;
    }

    public void delete(Long id) {
        getById(id);
        sysUserMapper.deleteById(id);
    }
}
