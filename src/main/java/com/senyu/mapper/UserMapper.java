package com.senyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.senyu.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户Mapper
 *
 * @author senyu
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 增加粉丝数
     */
    @Update("UPDATE user SET follower_count = follower_count + #{count} WHERE id = #{userId}")
    int increaseFollowerCount(@Param("userId") Long userId, @Param("count") int count);

    /**
     * 增加关注数
     */
    @Update("UPDATE user SET following_count = following_count + #{count} WHERE id = #{userId}")
    int increaseFollowingCount(@Param("userId") Long userId, @Param("count") int count);

    /**
     * 增加发帖数
     */
    @Update("UPDATE user SET post_count = post_count + 1 WHERE id = #{userId}")
    int increasePostCount(@Param("userId") Long userId);
}
