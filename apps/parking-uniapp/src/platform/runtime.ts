export type RuntimePlatform = 'app' | 'h5' | 'mp-weixin' | 'other'

/**
 * 平台差异统一收口在 platform 目录，避免业务页面散落条件编译逻辑。
 */
export function getRuntimePlatform(): RuntimePlatform {
  // #ifdef APP
  return 'app'
  // #endif

  // #ifdef H5
  return 'h5'
  // #endif

  // #ifdef MP-WEIXIN
  return 'mp-weixin'
  // #endif

  return 'other'
}
