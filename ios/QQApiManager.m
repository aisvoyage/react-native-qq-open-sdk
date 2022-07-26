//
//  QQApiManager.m
//  react-native-qq-open-sdk
//
//  Created by 朱文波 on 2022/7/25.
//
#import "QQApiManager.h"

#pragma GCC diagnostic ignored "-Wundeclared-selector"

@implementation QQApiManager

#pragma mark - LifeCycle
+(instancetype)sharedManager {
    static dispatch_once_t onceToken;
    static QQApiManager *instance;
    dispatch_once(&onceToken, ^{
        instance = [[QQApiManager alloc] init];
    });
    return instance;
}

#pragma mark - TencentLoginDelegate(授权登录回调协议)
/**
 * 登录成功后的回调
 */
- (void)tencentDidLogin {
    if (_delegate && [_delegate respondsToSelector:@selector(tencentDidLogin:)]) {
        [_delegate tencentDidLogin];
    }
}

/**
 * 登录失败后的回调
 * \param cancelled 代表用户是否主动退出登录
 */
- (void)tencentDidNotLogin:(BOOL)cancelled {
    if (_delegate && [_delegate respondsToSelector:@selector(tencentDidNotLogin:)]) {
        [_delegate tencentDidNotLogin:cancelled];
    }
}

/**
 * 登录时网络有问题的回调
 */
- (void)tencentDidNotNetWork {
    if (_delegate && [_delegate respondsToSelector:@selector(tencentDidNotNetWork:)]) {
        [_delegate tencentDidNotNetWork];
    }
}


#pragma mark - TencentSessionDelegate(开放接口回调协议)
/**
 * 退出登录的回调
 */
- (void)tencentDidLogout {
    if (_delegate && [_delegate respondsToSelector:@selector(tencentDidLogout:)]) {
        [_delegate tencentDidLogout];
    }
}

/**
 * 因用户未授予相应权限而需要执行增量授权。在用户调用某个api接口时，如果服务器返回操作未被授权，则触发该回调协议接口，由第三方决定是否跳转到增量授权页面，让用户重新授权。
 * \param tencentOAuth 登录授权对象。
 * \param permissions 需增量授权的权限列表。
 * \return 是否仍然回调返回原始的api请求结果。
 * \note 不实现该协议接口则默认为不开启增量授权流程。若需要增量授权请调用\ref TencentOAuth#incrAuthWithPermissions: \n注意：增量授权时用户可能会修改登录的帐号
 */
- (BOOL)tencentNeedPerformIncrAuth:(TencentOAuth *)tencentOAuth withPermissions:(NSArray *)permissions {
    if (_delegate && [_delegate respondsToSelector:@selector(tencentNeedPerformIncrAuth:)]) {
        return [_delegate tencentNeedPerformIncrAuth:tencentOAuth withPermissions:permissions];
    }
    return YES;
}

/**
 * [该逻辑未实现]因token失效而需要执行重新登录授权。在用户调用某个api接口时，如果服务器返回token失效，则触发该回调协议接口，由第三方决定是否跳转到登录授权页面，让用户重新授权。
 * \param tencentOAuth 登录授权对象。
 * \return 是否仍然回调返回原始的api请求结果。
 * \note 不实现该协议接口则默认为不开启重新登录授权流程。若需要重新登录授权请调用\ref TencentOAuth#reauthorizeWithPermissions: \n注意：重新登录授权时用户可能会修改登录的帐号
 */
- (BOOL)tencentNeedPerformReAuth:(TencentOAuth *)tencentOAuth {
    if (_delegate && [_delegate respondsToSelector:@selector(tencentNeedPerformReAuth:)]) {
        return [_delegate tencentNeedPerformReAuth:tencentOAuth];
    }
    return YES;
}

/**
 * 用户通过增量授权流程重新授权登录，token及有效期限等信息已被更新。
 * \param tencentOAuth token及有效期限等信息更新后的授权实例对象
 * \note 第三方应用需更新已保存的token及有效期限等信息。
 */
- (void)tencentDidUpdate:(TencentOAuth *)tencentOAuth {
    if (_delegate && [_delegate respondsToSelector:@selector(tencentDidUpdate:)]) {
        [_delegate tencentDidUpdate:tencentOAuth];
    }
}

/**
 * 用户增量授权过程中因取消或网络问题导致授权失败
 * \param reason 授权失败原因，具体失败原因参见sdkdef.h文件中\ref UpdateFailType
 */
- (void)tencentFailedUpdate:(UpdateFailType)reason {
    if (_delegate && [_delegate respondsToSelector:@selector(tencentFailedUpdate:)]) {
        [_delegate tencentFailedUpdate:reason];
    }
}

/**
 * 获取用户个人信息回调
 * \param response API返回结果，具体定义参见sdkdef.h文件中\ref APIResponse
 * \remarks 正确返回示例: \snippet example/getUserInfoResponse.exp success
 *          错误返回示例: \snippet example/getUserInfoResponse.exp fail
 */
- (void)getUserInfoResponse:(APIResponse*) response {
    if (_delegate && [_delegate respondsToSelector:@selector(getUserInfoResponse:)]) {
        [_delegate getUserInfoResponse:response];
    }
}

/**
 * 社交API统一回调接口
 * \param response API返回结果，具体定义参见sdkdef.h文件中\ref APIResponse
 * \param message 响应的消息，目前支持‘SendStory’,‘AppInvitation’，‘AppChallenge’，‘AppGiftRequest’
 */
- (void)responseDidReceived:(APIResponse*)response forMessage:(NSString *)message {
    if (_delegate && [_delegate respondsToSelector:@selector(responseDidReceived:)]) {
        [_delegate responseDidReceived:response forMessage:message];
    }
}

/**
 * post请求的上传进度
 * \param tencentOAuth 返回回调的tencentOAuth对象
 * \param bytesWritten 本次回调上传的数据字节数
 * \param totalBytesWritten 总共已经上传的字节数
 * \param totalBytesExpectedToWrite 总共需要上传的字节数
 * \param userData 用户自定义数据
 */
- (void)tencentOAuth:(TencentOAuth *)tencentOAuth didSendBodyData:(NSInteger)bytesWritten totalBytesWritten:(NSInteger)totalBytesWritten totalBytesExpectedToWrite:(NSInteger)totalBytesExpectedToWrite userData:(id)userData {
    if (_delegate && [_delegate respondsToSelector:@selector(tencentOAuth:)]) {
        [_delegate tencentOAuth:tencentOAuth didSendBodyData:bytesWritten totalBytesWritten:totalBytesWritten totalBytesExpectedToWrite:totalBytesExpectedToWrite userData:userData];
    }
}

/**
 处理来至QQ的请求
 */
- (void)onReq:(QQBaseReq *)req {
    if (_delegate && [_delegate respondsToSelector:@selector(onReq:)]) {
        [_delegate onReq:req];
    }
}

/**
 处理来至QQ的响应
 */
- (void)onResp:(QQBaseResp *)resp {
    if (_delegate && [_delegate respondsToSelector:@selector(onResp:)]) {
        [_delegate onResp:resp];
    }
}

/**
 处理QQ在线状态的回调
 */
- (void)isOnlineResponse:(NSDictionary *)response {
    if (_delegate && [_delegate respondsToSelector:@selector(isOnlineResponse:)]) {
        [_delegate isOnlineResponse:response];
    }
}

@end
