package com.senyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.senyu.entity.FeedOutbox;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Feed发件箱Mapper
 *
 * @author senyu
 */
@Mapper
public interface FeedOutboxMapper extends BaseMapper<FeedOutbox> {

    /**
     * 查询用户发件箱
     */
    @Select("SELECT * FROM feed_outbox " +
            "WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC " +
            "LIMIT #{limit}")
    List<FeedOutbox> selectUserOutbox(@Param("userId") Long userId, @Param("limit") int limit);
}
