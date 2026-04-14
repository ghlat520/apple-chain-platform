/**
 * Common wrapper types — mirror apple-common/result/R.java and PageResult.java.
 * DO NOT add fields unless the backend adds them.
 */

/** R wrapper — apple-common/result/R.java */
export interface R<T> {
  code: number;
  message: string;
  data: T | null;
}

/** Paged list container embedded in R.data — apple-common/result/PageResult.java */
export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

/** Base entity audit fields — apple-common/entity/BaseEntity.java */
export interface BaseEntity {
  id?: number;
  createTime?: string;
  updateTime?: string;
  createBy?: number;
  updateBy?: number;
  deleted?: number;
}

export interface PageParams {
  page?: number;
  size?: number;
  keyword?: string;
}
