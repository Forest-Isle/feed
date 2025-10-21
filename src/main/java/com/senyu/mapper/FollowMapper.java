package com.senyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.senyu.entity.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 关注关系Mapper
 *
 * @author senyu
 */
@Mapper
public interface FollowMapper extends BaseMapper<Follow> {

    /**
     * 查询用户的粉丝ID列表
     */
    @Select("SELECT follower_id FROM follow WHERE followee_id = #{userId} AND deleted = 0")
    List<Long> selectFollowerIds(@Param("userId") Long userId);

    /**
     * 查询用户的关注ID列表
     */
    @Select("SELECT followee_id FROM follow WHERE follower_id = #{userId} AND deleted = 0")
    List<Long> selectFollowingIds(@Param("userId") Long userId);

    /**
     * 查询活跃粉丝ID列表
     */
    @Select("SELECT f.follower_id FROM follow f " +
            "INNER JOIN user u ON f.follower_id = u.id " +
            "WHERE f.followee_id = #{userId} AND f.deleted = 0 " +
            "AND u.is_active = 1 AND u.deleted = 0")
    List<Long> selectActiveFollowerIds(@Param("userId") Long userId);
}
