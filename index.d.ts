declare module "@byron-react-native/qqopensdk" {
  export interface QQOpenSDKAuthorize {
    error: string;
    cancelled: boolean;
    accessToken: string;
    expirationDate: number;
    openId: string;
    unionid: string;
  }

  export type QQLibShareType = 'news' | 'text' | 'image';

  export interface QQLibSharePropTypes {
    type: QQLibShareType,
  }

  export interface QQLibShareNewsPropTypes extends QQLibSharePropTypes {
    title: string,
    description: string,
    webpageUrl: string,
    imageUrl: string,
  }

  export interface QQLibShareTextPropTypes extends QQLibSharePropTypes {
    text: string,
  }

  export interface QQLibShareImagePropTypes extends QQLibSharePropTypes {
    imageUrl: string,
    imageLocalUrl: string,
  }

  class QQOpenSDK {
    /**
     * 初始化TencentOAuth对象
     * @param appId 不可为nil，第三方应用在互联开放平台申请的唯一标识
     */
    static initWithAppId(appId: string): void;
    /**
     * 检测是否支持分享
     * 如果当前已安装QQ且QQ版本支持API调用 或者 当前已安装TIM且TIM版本支持API调用则返回YES，否则返回NO
     */
    static isQQInstalled(): Promise<boolean>;
    /**
     * 登录授权
     * @param permissions 授权信息列
     * @default ['get_user_info']
     */
    static authorize(
      permissions?: string[]
    ): Promise<undefined | Partial<QQOpenSDKAuthorize>>;
    /**
     * 增量授权，因用户没有授予相应接口调用的权限，需要用户确认是否授权
     * @param permissions 需增量授权的信息列表
     */
    static incrAuthWithPermissions(permissions: string[]): Promise<boolean>;
    /**
     * 重新授权，因token废除或失效导致接口调用失败，需用户重新授权
     * @param permissions 授权信息列表，同登录授权
     */
    static reauthorizeWithPermissions(permissions: string[]): Promise<boolean>;
    /**
     * 退出登录(退出登录后，TecentOAuth失效，需要重新初始化)
     */
    static logout(): void;

    static shareToQQ(data:QQLibSharePropTypes):Promise<any>;
  }
  export default QQOpenSDK;
}
