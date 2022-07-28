# react-native-qq-open-sdk
  *暂时不建议使用，目前只做了登录<br>

iOS如果报错包含 Undefined symbols for architecture x86_64:
  "_SCNetworkReachabilitySetCallback", referenced from
则需要添加 SystemConfiguration.framework 到 Link Binary With Libraries

## Getting started

`$ yarn add @byron-react-native/qqopensdk`

### iOS

```javascript
// （1）配置URL Scheme
<>在XCode中，选择你的工程设置项，选中“TARGETS”一栏，在“info”标签栏的“URL type”添加一条新的“URL scheme”，新的scheme = tencent + appid。如果您使用的是XCode3或者更低的版本，则需要在plist文件中添加。</>

// （2）配置LSApplicationQueriesSchemes
<key>LSApplicationQueriesSchemes</key>
	<array>
		<string>mqq://</string>
		<string>mqqapi://</string>
		<!-- 更多请参考 https://wiki.connect.qq.com/ios_sdk%e7%8e%af%e5%a2%83%e6%90%ad%e5%bb%ba -->
    <string>tim://</string>
    <string>mqqopensdknopasteboard://</string>
    <string>mqqopensdkapiV2://</string>
    <string>mqqconnect</string>
    <string>mqqopensdkdataline</string>
    <string>mqqopensdkgrouptribeshare</string>
    <string>mqqopensdkfriend</string>
    <string>mqqopensdkapi</string>
    <string>mqqopensdkapiV2</string>
	</array>

// （3）AppDelegate.mm 添加
#import "WXApiManager.h" // 如果有集成微信
#import "QQApiManager.h"

// weixin qq
- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options
{
  if ([TencentOAuth CanHandleOpenURL:url]) {
    return [TencentOAuth HandleOpenURL:url];
  }
  // 如果有集成微信
  return [WXApi handleOpenURL:url delegate:[WXApiManager sharedManager]]; 
}

- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void(^)(NSArray<id<UIUserActivityRestoring>> * __nullable restorableObjects))restorationHandler
{
  if ([TencentOAuth CanHandleUniversalLink:userActivity.webpageURL]) {
    return [TencentOAuth HandleUniversalLink:userActivity.webpageURL];
  }
  // 如果有集成微信
  return [WXApi handleOpenUniversalLink:userActivity delegate:[WXApiManager sharedManager]];
}

```

### Android
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
```
