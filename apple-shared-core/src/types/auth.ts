/**
 * Auth types — mirror apple-module-user/dto/LoginRequest.java + LoginResponse.java
 */

export interface LoginRequest {
  /** phone or email */
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: number;
  username: string;
  realName?: string;
  roleCode?: string;
  orgName?: string;
  avatar?: string;
  roles?: string[];
  permissions?: string[];
}

export interface CurrentUser {
  userId: number;
  username: string;
  realName?: string;
  phone?: string;
  email?: string;
  orgName?: string;
  avatar?: string;
  roleCode?: string;
  roles?: string[];
  permissions?: string[];
}
