// QqOpenSdk.h

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import "QQApiManager.h"

@interface QqOpenSdk : RCTEventEmitter <RCTBridgeModule, QQApiManagerDelegate>

@end
