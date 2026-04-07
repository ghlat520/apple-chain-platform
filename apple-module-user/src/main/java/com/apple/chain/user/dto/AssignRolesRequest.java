package com.apple.chain.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Body for {@code POST /api/admin/users/{id}/roles}.
 * Accepts either {@code roleIds} or {@code roleCodes} (codes take precedence
 * when both are present, since they are more stable across environments).
 */
@Getter
@Setter
public class AssignRolesRequest {

    private List<Long> roleIds;

    @NotEmpty(message = "roleCodes 不能为空")
    private List<String> roleCodes;
}
