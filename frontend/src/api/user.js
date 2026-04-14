import request from './request'

/**
 * 用户登录
 * 契约：contract/modules/user.md §1
 * @param {import('./types/user').LoginRequest} params
 * @returns {Promise<import('./types/user').LoginResponse>}
 */
export function login(params) {
  return request({
    url: '/user/login',
    method: 'post',
    data: params
  })
}

/**
 * 获取当前用户
 * 契约：contract/modules/user.md §2
 * @returns {Promise<import('./types/user').CurrentUserResponse>}
 */
export function getCurrentUser() {
  return request({
    url: '/user/auth/me',
    method: 'get'
  })
}

/**
 * 修改密码
 * 契约：contract/modules/user.md §3
 * @param {import('./types/user').ChangePasswordRequest} params
 * @returns {Promise<null>}
 */
export function changePassword(params) {
  return request({
    url: '/user/password',
    method: 'put',
    data: params
  })
}

/**
 * 用户登出
 * 契约：contract/modules/user.md §4
 * @returns {Promise<null>}
 */
export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

/**
 * 分配角色
 * 契约：contract/modules/user.md §5
 * @param {number} id 目标用户 ID
 * @param {import('./types/user').AssignRolesRequest} params
 * @returns {Promise<null>}
 */
export function assignRoles(id, params) {
  return request({
    url: `/user/${id}/roles`,
    method: 'put',
    data: params
  })
}
