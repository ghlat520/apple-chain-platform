package com.apple.chain.trade.mapper;

import com.apple.chain.trade.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("SELECT * FROM td_chat_message WHERE deleted = 0 AND session_id = #{sessionId} " +
            "ORDER BY send_time ASC LIMIT #{limit}")
    List<ChatMessage> findBySession(@Param("sessionId") String sessionId, @Param("limit") int limit);

    @Update("UPDATE td_chat_message SET read_flag = 1 " +
            "WHERE deleted = 0 AND session_id = #{sessionId} AND to_user_id = #{userId} AND read_flag = 0")
    int markRead(@Param("sessionId") String sessionId, @Param("userId") Long userId);
}
