import request from './request'

export function getOverview() {
  return request.get('/stats/overview')
}

export function getPvUv(params) {
  return request.get('/stats/pv-uv', { params })
}

export function getFunnel() {
  return request.get('/stats/funnel')
}

export function getRegion() {
  return request.get('/stats/region')
}

export function getTaskStatus() {
  return request.get('/stats/task-status')
}
