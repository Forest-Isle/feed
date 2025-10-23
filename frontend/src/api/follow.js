import request from '@/utils/request'

/**
 * 关注用户
 */
export function followUser(userId) {
  return request({
    url: `/follow/${userId}`,
    method: 'post'
  })
}

/**
 * 取消关注
 */
export function unfollowUser(userId) {
  return request({
    url: `/follow/${userId}`,
    method: 'delete'
  })
}

/**
 * 获取粉丝列表
 */
export function getFollowers(userId, params) {
  return request({
    url: `/follow/followers/${userId}`,
    method: 'get',
    params
  })
}

/**
 * 获取关注列表
 */
export function getFollowing(userId, params) {
  return request({
    url: `/follow/following/${userId}`,
    method: 'get',
    params
  })
}
