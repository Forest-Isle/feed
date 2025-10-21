package com.senyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.senyu.entity.FeedInbox;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Feed收件箱Mapper
 *
 * @author senyu
 */
@Mapper
public interface FeedInboxMapper extends BaseMapper<FeedInbox> {

    /**
     * 查询用户Feed流
     */
    @Select("SELECT * FROM feed_inbox " +
            "WHERE user_id = #{userId} " +
            "<if test='maxId != null'> AND id &lt; #{maxId} </if>" +
            "ORDER BY id DESC " +
            "LIMIT #{limit}")
    List<FeedInbox> selectUserFeed(@Param("userId") Long userId,
                                    @Param("maxId") Long maxId,
                                    @Param("limit") int limit);

    /**
     * 批量插入Feed
     */
    int insertBatch(@Param("list") List<FeedInbox> list);
}
