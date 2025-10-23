import request from '@/utils/request'

/**
 * 获取用户Feed流
 */
export function getUserFeed(userId, params) {
  return request({
    url: `/feed/user/${userId}`,
    method: 'get',
    params
  })
}

/**
 * 获取推荐Feed流
 */
export function getRecommendFeed(params) {
  return request({
    url: '/feed/recommend',
    method: 'get',
    params
  })
}
