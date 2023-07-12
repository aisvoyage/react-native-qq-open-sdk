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
    [TencentOAuth setIsUserAgreedAuthorization:YES];
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

RCT_EXPORT_METHOD(shareToQQ:(NSDictionary *)data resolve:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [self _shareToQQWithData:data resolve:resolve reject:reject];
}

#pragma mark

- (void)_shareToQQWithData:(NSDictionary *)aData resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject{

    NSString *type = aData[@"type"]
    QQApiObject *content = nil;
    if ([type isEqualToString: @"text"]) {
        NSString *text = aData[@"text"];
        content = [QQApiTextObject objectWithText:text];
    }
    else if ([type isEqualToString: @"image"]) {
        NSString *imageUrl = aData[@"imageUrl"];
        UIImage *image = [UIImage imageWithContentsOfFile:imageUrl];
        NSData *imgData = UIImageJPEGRepresentation(image, 1);

        // 图片大小如果大于5M就进行压缩
        if(imgData.length >= 5242880) {
            imgData =[self compressImage: image toByte: 55242880];
        }

        content = [QQApiImageObject objectWithData:imgData
                                  previewImageData:imgData
                                             title:@"title"
                                       description :@"description"];
    }
    else if ([type isEqualToString: @"news"]) {
        NSString *title = aData[@"title"];
        NSString *description = aData[@"description"];
        NSString *preImage = aData[@"preImage"];
        NSString *url = aData[@"url"];

        content = [QQApiNewsObject
                           objectWithURL:[NSURL URLWithString:url]
                           title:title
                           description:description
                           previewImageURL:[NSURL URLWithString:preImage]];
    }

    if (content != nil) {
        SendMessageToQQReq *req = [SendMessageToQQReq reqWithContent:content];
        QQApiSendResultCode sent = [QQApiInterface sendReq:req];
        resolve(@[[NSNull null]]);
    }
    else {
        reject(@"-1",@"QQ API invoke returns false.",nil);
    }
}

// 压缩图片
- (NSData *)compressImage:(UIImage *)image toByte:(NSUInteger)maxLength {
    // Compress by quality
    CGFloat compression = 1;
    NSData *data = UIImageJPEGRepresentation(image, compression);
    if (data.length < maxLength) return data;

    CGFloat max = 1;
    CGFloat min = 0;
    for (int i = 0; i < 6; ++i) {
        compression = (max + min) / 2;
        data = UIImageJPEGRepresentation(image, compression);
        if (data.length < maxLength * 0.9) {
            min = compression;
        } else if (data.length > maxLength) {
            max = compression;
        } else {
            break;
        }
    }
    UIImage *resultImage = [UIImage imageWithData:data];
    if (data.length < maxLength) return data;

    // Compress by size
    NSUInteger lastDataLength = 0;
    while (data.length > maxLength && data.length != lastDataLength) {
        lastDataLength = data.length;
        CGFloat ratio = (CGFloat)maxLength / data.length;
        CGSize size = CGSizeMake((NSUInteger)(resultImage.size.width * sqrtf(ratio)),
                                 (NSUInteger)(resultImage.size.height * sqrtf(ratio))); // Use NSUInteger to prevent white blank
        UIGraphicsBeginImageContext(size);
        [resultImage drawInRect:CGRectMake(0, 0, size.width, size.height)];
        resultImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        data = UIImageJPEGRepresentation(resultImage, compression);
    }

    if (data.length > maxLength) {
        return [self compressImage:resultImage toByte:maxLength];
    }

    return data;
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
