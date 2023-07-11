// main index.js

import { NativeModules, NativeEventEmitter, Platform } from "react-native";

const { QqOpenSdk } = NativeModules;
const emitter = new NativeEventEmitter(Platform.OS === "ios" ? QqOpenSdk : null);

export default class QQOpenSDK {
  static subs = {};

  static initWithAppId = (appid) => {
    return QqOpenSdk.initWithAppId(appid);
  };
  static isQQInstalled = () => {
    return QqOpenSdk.isQQInstalled();
  };
  static authorize = async (permissions = ["get_user_info"]) => {
    const event = "QQ_LOGIN";
    const res = await QqOpenSdk.authorize(permissions);
    if (!res) return void 0;
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  };
  static incrAuthWithPermissions = async (permissions) => {
    return QqOpenSdk.incrAuthWithPermissions(permissions);
  };
  static reauthorizeWithPermissions = async (permissions) => {
    return QqOpenSdk.reauthorizeWithPermissions(permissions);
  };
  static logout = () => {
    return QqOpenSdk.logout();
  };

  static shareToQQ = async (data) => {
    return QqOpenSdk.shareToQQ(data);
  }
}
