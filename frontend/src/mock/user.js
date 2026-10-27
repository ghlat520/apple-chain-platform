/**
 * 用户模块 Mock 数据
 * 数据来自 contract/modules/user.md 响应示例
 */

// §1 登录成功
export const loginSuccess = {
  token: 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMDI0In0.mock-token-signature',
  userId: 1024,
  username: 'zhangsan',
  realName: '张三',
  roleCode: 'FARMER',
  orgName: '洛川县苹果合作社',
  avatar: 'https://cdn.example.com/avatar/1024.png',
  roles: ['FARMER', 'COOP_MEMBER'],
  permissions: ['orchard:read', 'orchard:write', 'trace:read']
}

// §2 当前用户
export const currentUser = {
  userId: 1024,
  username: 'zhangsan',
  realName: '张三',
  phone: '13800138000',
  email: 'zhangsan@example.com',
  orgName: '洛川县苹果合作社',
  avatar: 'https://cdn.example.com/avatar/1024.png',
  roleCode: 'FARMER',
  roles: ['FARMER', 'COOP_MEMBER'],
  permissions: ['orchard:read', 'orchard:write']
}
