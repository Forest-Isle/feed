import request from '@/utils/request'

/**
 * 发布内容
 */
export function publishPost(data) {
  return request({
    url: '/post/publish',
    method: 'post',
    data
  })
}

/**
 * 获取内容详情
 */
export function getPostDetail(postId) {
  return request({
    url: `/post/${postId}`,
    method: 'get'
  })
}

/**
 * 点赞内容
 */
export function likePost(postId) {
  return request({
    url: `/post/like/${postId}`,
    method: 'post'
  })
}
