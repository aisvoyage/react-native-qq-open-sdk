// QqOpenSdk.m

#import "QqOpenSdk.h"

@interface QqOpenSdk()<TencentSessionDelegate> {
    TencentOAuth* mTencent;
}

@end

@implementation QqOpenSdk

- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(initWithAppId:(NSString *)appid) {
    if (mTencent != nil) {
        return;
    }
    [QQApiManager sharedManager].delegate = self;
    mTencent = [[TencentOAuth alloc] initWithAppId:appid andDelegate:self];
    #if DEBUG
    [QQApiInterface startLogWithBlock:^(NSString *logStr) {
        NSLog(@"%@",logStr);
    }];
    #endif
}

RCT_EXPORT_METHOD(isQQInstalled:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    resolve(@([QQApiInterface isSupportShareToQQ]));
}

RCT_EXPORT_METHOD(authorize:(NSArray *)permissions resolve:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    if (mTencent == nil) {
        resolve(@(NO));
        return;
    }
    resolve(@([mTencent authorize:permissions]));
}

RCT_EXPORT_METHOD(incrAuthWithPermissions:(NSArray *)permissions resolve:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    if (mTencent == nil) {
        resolve(@(NO));
        return;
    }
    resolve(@([mTencent incrAuthWithPermissions:permissions]));
}

RCT_EXPORT_METHOD(reauthorizeWithPermissions:(NSArray *)permissions resolve:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    if (mTencent == nil) {
        resolve(@(NO));
        return;
    }
    resolve(@([mTencent reauthorizeWithPermissions:permissions]));
}

RCT_EXPORT_METHOD(logout) {
    if (mTencent == nil) {
        return;
    }
    [mTencent logout:self];
    mTencent = nil;
}

#pragma mark - TencentLoginDelegate(授权登录回调协议)
/**
 * 登录成功后的回调
 */
- (void)tencentDidLogin {
    [self sendEventWithName:@"QQ_LOGIN" body:@{
        @"accessToken": mTencent.accessToken ? mTencent.accessToken : @"",
        @"expirationDate": @([mTencent.expirationDate timeIntervalSince1970] * 1000),
        @"openId": mTencent.openId ? mTencent.openId : @"",
        @"unionid": mTencent.unionid ? mTencent.unionid : @"",
    }];
}

/**
 * 登录失败后的回调
 * \param cancelled 代表用户是否主动退出登录
 */
- (void)tencentDidNotLogin:(BOOL)cancelled {
    NSString * err = [TencentOAuth getLastErrorMsg];
    [self sendEventWithName:@"QQ_LOGIN" body:@{
        @"cancelled":@(cancelled),
        @"error": err
    }];
}

/**
 * 登录时网络有问题的回调
 */
- (void)tencentDidNotNetWork {
    [self sendEventWithName:@"QQ_LOGIN" body:@{
        @"error": @"Network exception"
    }];
}

#pragma mark - 其它回调暂时不写

- (NSArray<NSString *> *)supportedEvents {
  return @[
      @"QQ_LOGIN"
  ];
}

@end
