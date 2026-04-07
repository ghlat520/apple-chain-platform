package com.apple.chain.bigdata.service.impl;

import com.apple.chain.bigdata.entity.BdPermission;
import com.apple.chain.bigdata.entity.BdRole;
import com.apple.chain.bigdata.entity.BdRolePermission;
import com.apple.chain.bigdata.mapper.BdPermissionMapper;
import com.apple.chain.bigdata.mapper.BdRoleMapper;
import com.apple.chain.bigdata.mapper.BdRolePermissionMapper;
import com.apple.chain.bigdata.service.BdRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BdRoleServiceImpl
        extends ServiceImpl<BdRoleMapper, BdRole>
        implements BdRoleService {

    private final BdPermissionMapper permissionMapper;
    private final BdRolePermissionMapper rolePermissionMapper;

    @Override
    public List<BdPermission> listPermissions() {
        return permissionMapper.selectList(
                new LambdaQueryWrapper<BdPermission>()
                        .orderByAsc(BdPermission::getSortOrder));
    }

    @Override
    public BdPermission createPermission(BdPermission permission) {
        permissionMapper.insert(permission);
        return permission;
    }

    @Override
    public boolean deletePermission(Long id) {
        return permissionMapper.deleteById(id) > 0;
    }

    @Override
    public List<BdRolePermission> listRolePermissions(String roleCode) {
        return rolePermissionMapper.selectList(
                new LambdaQueryWrapper<BdRolePermission>()
                        .eq(BdRolePermission::getRoleCode, roleCode));
    }

    @Override
    @Transactional
    public int assignPermissions(String roleCode, List<String> permCodes) {
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<BdRolePermission>()
                        .eq(BdRolePermission::getRoleCode, roleCode));
        if (permCodes == null || permCodes.isEmpty()) {
            return 0;
        }
        List<BdRolePermission> rows = new ArrayList<>(permCodes.size());
        for (String pc : permCodes) {
            BdRolePermission rp = new BdRolePermission();
            rp.setRoleCode(roleCode);
            rp.setPermCode(pc);
            rolePermissionMapper.insert(rp);
            rows.add(rp);
        }
        return rows.size();
    }
}
