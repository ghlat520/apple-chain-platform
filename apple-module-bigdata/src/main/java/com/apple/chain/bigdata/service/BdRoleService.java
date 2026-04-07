package com.apple.chain.bigdata.service;

import com.apple.chain.bigdata.entity.BdPermission;
import com.apple.chain.bigdata.entity.BdRole;
import com.apple.chain.bigdata.entity.BdRolePermission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/** Role / permission matrix. */
public interface BdRoleService extends IService<BdRole> {

    List<BdPermission> listPermissions();

    BdPermission createPermission(BdPermission permission);

    boolean deletePermission(Long id);

    List<BdRolePermission> listRolePermissions(String roleCode);

    /** Replace all permissions bound to this role with the given set. */
    int assignPermissions(String roleCode, List<String> permCodes);
}
