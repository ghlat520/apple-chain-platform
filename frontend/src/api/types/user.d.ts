// 该文件的类型来自 contract/modules/user.md
// 禁止手改 —— 契约变更请先改 Markdown 文档，再同步到此处
// 最近同步：2026-04-11

/** POST /api/user/login 请求体 */
export interface LoginRequest {
  username: string;
  password: string;
}

/** POST /api/user/login 响应体 */
export interface LoginResponse {
  token: string;
  userId: number;
  username: string;
  realName: string | null;
  roleCode: string | null;
  orgName: string | null;
  avatar: string | null;
  roles: string[];
  permissions: string[];
}

/** GET /api/user/auth/me 响应体 */
export interface CurrentUserResponse {
  userId: number;
  username: string;
  realName: string | null;
  phone: string | null;
  email: string | null;
  orgName: string | null;
  avatar: string | null;
  roleCode: string | null;
  roles: string[];
  permissions: string[];
}

/** PUT /api/user/password 请求体 */
export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

/** PUT /api/user/{id}/roles 请求体 */
export interface AssignRolesRequest {
  roleCodes: string[];
}
