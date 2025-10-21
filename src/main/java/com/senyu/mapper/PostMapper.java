package com.senyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.senyu.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 内容Mapper
 *
 * @author senyu
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 增加点赞数
     */
    @Update("UPDATE post SET like_count = like_count + #{count} WHERE id = #{postId}")
    int increaseLikeCount(@Param("postId") Long postId, @Param("count") int count);

    /**
     * 增加评论数
     */
    @Update("UPDATE post SET comment_count = comment_count + 1 WHERE id = #{postId}")
    int increaseCommentCount(@Param("postId") Long postId);

    /**
     * 增加分享数
     */
    @Update("UPDATE post SET share_count = share_count + 1 WHERE id = #{postId}")
    int increaseShareCount(@Param("postId") Long postId);

    /**
     * 增加浏览数
     */
    @Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{postId}")
    int increaseViewCount(@Param("postId") Long postId);

    /**
     * 根据用户ID列表查询最新内容
     */
    @Select("<script>" +
            "SELECT * FROM post " +
            "WHERE user_id IN " +
            "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            " AND status = 1 AND deleted = 0 " +
            "ORDER BY created_at DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Post> selectLatestByUserIds(@Param("userIds") List<Long> userIds, @Param("limit") int limit);
}
