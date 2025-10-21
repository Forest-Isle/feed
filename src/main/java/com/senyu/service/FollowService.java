package com.senyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.senyu.common.ResultCode;
import com.senyu.entity.Follow;
import com.senyu.entity.User;
import com.senyu.mapper.FollowMapper;
import com.senyu.mapper.UserMapper;
import com.senyu.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 关注服务
 *
 * @author senyu
 */
@Slf4j
@Service
public class FollowService {

    @Resource
    private FollowMapper followMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 关注用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long followerId, Long followeeId) {
        log.info("用户{}关注用户{}", followerId, followeeId);

        // 1. 参数校验
        if (followerId.equals(followeeId)) {
            throw new RuntimeException(ResultCode.CANNOT_FOLLOW_SELF.getMessage());
        }

        // 2. 检查是否已关注
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowerId, followerId)
                .eq(Follow::getFolloweeId, followeeId);
        Follow existFollow = followMapper.selectOne(wrapper);

        if (existFollow != null) {
            throw new RuntimeException(ResultCode.ALREADY_FOLLOWED.getMessage());
        }

        // 3. 检查被关注用户是否存在
        User followee = userMapper.selectById(followeeId);
        if (followee == null) {
            throw new RuntimeException(ResultCode.USER_NOT_FOUND.getMessage());
        }

        // 4. 创建关注关系
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        followMapper.insert(follow);

        // 5. 更新粉丝数和关注数
        userMapper.increaseFollowerCount(followeeId, 1);   // 被关注者粉丝数+1
        userMapper.increaseFollowingCount(followerId, 1);  // 关注者关注数+1

        // 6. 更新Redis缓存
        updateFollowCache(followerId, followeeId, true);

        log.info("关注成功");
    }

    /**
     * 取消关注
     */
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long followerId, Long followeeId) {
        log.info("用户{}取消关注用户{}", followerId, followeeId);

        // 1. 查询关注关系
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowerId, followerId)
                .eq(Follow::getFolloweeId, followeeId);
        Follow follow = followMapper.selectOne(wrapper);

        if (follow == null) {
            throw new RuntimeException(ResultCode.NOT_FOLLOWED.getMessage());
        }

        // 2. 删除关注关系（逻辑删除）
        followMapper.deleteById(follow.getId());

        // 3. 更新粉丝数和关注数
        userMapper.increaseFollowerCount(followeeId, -1);   // 被关注者粉丝数-1
        userMapper.increaseFollowingCount(followerId, -1);  // 关注者关注数-1

        // 4. 更新Redis缓存
        updateFollowCache(followerId, followeeId, false);

        log.info("取消关注成功");
    }

    /**
     * 检查是否已关注
     */
    public boolean isFollowing(Long followerId, Long followeeId) {
        // 先从缓存查询
        String followingKey = RedisKeyUtil.getUserFollowingKey(followerId);
        Boolean isMember = redisTemplate.opsForSet().isMember(followingKey, followeeId);

        if (isMember != null) {
            return isMember;
        }

        // 缓存未命中，查询数据库
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowerId, followerId)
                .eq(Follow::getFolloweeId, followeeId);
        return followMapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取粉丝列表
     */
    public List<Long> getFollowerIds(Long userId) {
        // 先从缓存获取
        String followersKey = RedisKeyUtil.getUserFollowersKey(userId);
        Long size = redisTemplate.opsForSet().size(followersKey);

        if (size != null && size > 0) {
            return followMapper.selectFollowerIds(userId);
        }

        // 缓存未命中，从数据库查询并缓存
        List<Long> followerIds = followMapper.selectFollowerIds(userId);
        if (!followerIds.isEmpty()) {
            redisTemplate.opsForSet().add(followersKey, followerIds.toArray());
            redisTemplate.expire(followersKey, 3600, TimeUnit.SECONDS);
        }

        return followerIds;
    }

    /**
     * 获取关注列表
     */
    public List<Long> getFollowingIds(Long userId) {
        // 先从缓存获取
        String followingKey = RedisKeyUtil.getUserFollowingKey(userId);
        Long size = redisTemplate.opsForSet().size(followingKey);

        if (size != null && size > 0) {
            return followMapper.selectFollowingIds(userId);
        }

        // 缓存未命中，从数据库查询并缓存
        List<Long> followingIds = followMapper.selectFollowingIds(userId);
        if (!followingIds.isEmpty()) {
            redisTemplate.opsForSet().add(followingKey, followingIds.toArray());
            redisTemplate.expire(followingKey, 3600, TimeUnit.SECONDS);
        }

        return followingIds;
    }

    /**
     * 更新关注关系缓存
     */
    private void updateFollowCache(Long followerId, Long followeeId, boolean isFollow) {
        String followersKey = RedisKeyUtil.getUserFollowersKey(followeeId);
        String followingKey = RedisKeyUtil.getUserFollowingKey(followerId);

        if (isFollow) {
            // 关注：添加到集合
            redisTemplate.opsForSet().add(followersKey, followerId);
            redisTemplate.opsForSet().add(followingKey, followeeId);
        } else {
            // 取消关注：从集合移除
            redisTemplate.opsForSet().remove(followersKey, followerId);
            redisTemplate.opsForSet().remove(followingKey, followeeId);
        }

        // 设置过期时间
        redisTemplate.expire(followersKey, 3600, TimeUnit.SECONDS);
        redisTemplate.expire(followingKey, 3600, TimeUnit.SECONDS);
    }
}
