package com.apple.chain.user.mapper;

import com.apple.chain.user.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * User mapper - extends MyBatis-Plus BaseMapper for standard CRUD.
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM uc_user WHERE username = #{username} AND deleted = 0 LIMIT 1")
    User findByUsername(String username);
}
