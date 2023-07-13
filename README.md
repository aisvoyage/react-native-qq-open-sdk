# react-native-qq-open-sdk
  *~~暂时不建议使用，目前只做了登录~~<br>

在原有登陆的基础上加入了分享功能 支持纯文本和图片分享，新闻格式分享到qq目前存在未知问题只有url没有标题描述和缩略图

目前只测试了ios平台

react 版本

react 18.2.0

react-native 0.71.8



iOS如果报错包含 Undefined symbols for architecture x86_64:
  "_SCNetworkReachabilitySetCallback", referenced from
则需要添加 SystemConfiguration.framework 到 Link Binary With Libraries

贴出我添加的包作为参考:

CoreTelephony.framework

libc++.tbd

libPods-app.a

libz.tbd

SystemConfiguration.framework

WebKit.framework





## Getting started

~~$ yarn add @byron-react-native/qqopensdk~~

$ yarn add git+https://github.com/Flano-123/react-native-qq-open-sdk.git

$ cd ios

$ pod install 或者 update



### iOS(iOS_SDK_V3.5.11)

```javascript
// （1）配置URL Scheme
<>在XCode中，选择你的工程设置项，选中“TARGETS”一栏，在“info”标签栏的“URL type”添加一条新的“URL scheme”，新的Identifier为tencentopenapi，URL schemes 为 tencent + 你的appid。</>

// （2）配置LSApplicationQueriesSchemes
<key>LSApplicationQueriesSchemes</key>
	<array>
    <string>mqq://</string>
    <string>mqq</string>
    <string>mqqapi://</string>
    <string>mqqapi</string>
    <string>tim://</string>
    <string>tim</string>
    <string>mqqopensdknopasteboard://</string>
    <string>mqqopensdknopasteboard</string>
    <string>mqqopensdkapiV2://</string>
    <string>mqqconnect</string>
    <string>mqqopensdkdataline</string>
    <string>mqqopensdkgrouptribeshare</string>
    <string>mqqopensdkfriend</string>
    <string>mqqopensdkapi</string>
    <string>mqqopensdkapiV2</string>
	</array>

// （3）AppDelegate.mm 添加
#import "QQApiManager.h"

// openURL
- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options
{
  if ([TencentOAuth CanHandleOpenURL:url]) {
    return [TencentOAuth HandleOpenURL:url];
  }
  
  return ...
}

  // Lingking URL
- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void(^)(NSArray<id<UIUserActivityRestoring>> * __nullable restorableObjects))restorationHandler
{
    
  if ([TencentOAuth CanHandleUniversalLink:userActivity.webpageURL]) {
    return [TencentOAuth HandleUniversalLink:userActivity.webpageURL];
  }
    
  return ...
}

```

### Android(Android_SDK_V3.5.12) 安卓我没有测试过
```javascript
// app/build.gradle add
android {
  defaultConfig {
    manifestPlaceholders = [
      QQ_APPID: "QQ appId"
    ]
  }
}

```

### Api
```javascript
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

/**
* 分享纯文本到qq好友
* @param text
*/
static shareTextToQQ(text: string): Promise<any>;

/**
* 分享纯图片到qq好友 图片大小要求在5M以下 sdk会对大于5M的图片进行压缩但不能保证成功
* @param imageLocalUrl 本地图片路径 形如：/var/mobile/Containers/Data/Application/AD2FDFBB-6A91-4E3B-8761-3F657A78D507/Library/Caches/ImagePicker/BB1B87D8-5C1E-4C4B-9C01-4C20C0444119.jpg
*/
static shareImageToQQ(imageLocalUrl: string): Promise<any>;

/**
* 分享新闻到qq好友
*
* @param title 标题 长度(0,128]
* @param description 描述内容 长度(0,512]
* @param preImage 预览缩略图 preViewImageRemoteUrl 图片大小要求(0,1M]
* @param url 点击跳转连接 remoteUrl 长度(0,1024]
*/
static shareNewsToQQ(title: string, description: string, preImage: string, url: string): Promise<any>;
```
