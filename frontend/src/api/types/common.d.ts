// 该文件的类型来自 contract/CONVENTIONS.md
// 禁止手改 —— 契约变更请先改 Markdown 文档，再同步到此处
// 最近同步：2026-04-11

/** 统一响应包装 —— 见 contract/CONVENTIONS.md §1 */
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T | null;
}

/** 分页请求 —— 见 contract/CONVENTIONS.md §2 */
export interface PageParam {
  page: number;
  size: number;
  keyword?: string;
}

/** 分页响应 —— 见 contract/CONVENTIONS.md §2 */
export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

/** 枚举字段统一形态 —— 见 contract/CONVENTIONS.md §4 */
export interface EnumValue<T = number> {
  code: T;
  desc: string;
}
