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

  /**
   * 分享纯文本到qq
   * @param text
   * @param scene 0=qq 1=qzone
   */
  static shareTextToQQ = async (text = '', scene = 0) => {
    return new Promise((resolve, reject) => {
      if (!text) {
        reject('share text to qq cannot be empty')
      }
      if (scene === 0) {
        return QqOpenSdk.shareToQQ({type: 'text', text});
      } else {
        return QqOpenSdk.shareToQZone({type: 'text', text});
      }
    })
  }

  /**
   * 分享纯图片到qq 图片大小要求在5M以下 sdk会对大于5M的图片进行压缩但不能保证成功
   * @param imageLocalUrl 本地图片路径 形如：/var/mobile/Containers/Data/Application/AD2FDFBB-6A91-4E3B-8761-3F657A78D507/Library/Caches/ImagePicker/BB1B87D8-5C1E-4C4B-9C01-4C20C0444119.jpg
   * @param scene 0=qq 1=qzone
   */
  static shareImageToQQ = async (imageLocalUrl = '', scene = 0) => {
    return new Promise((resolve, reject) => {
      if (!imageLocalUrl) {
        reject('share image local url cannot be empty');
      }
      if (scene === 0) {
        return QqOpenSdk.shareToQQ({type: 'image', imageUrl: imageLocalUrl});
      } else {
        return QqOpenSdk.shareToQZone({type: 'image', imageUrl: imageLocalUrl});
      }
    });
  };

  /**
   * 分享新闻到qq
   *
   * @param title 标题 长度(0,128]
   * @param description 描述内容 长度(0,512]
   * @param preImage 预览缩略图 preViewImageRemoteUrl 图片大小要求(0,1M]
   * @param url 点击跳转连接 remoteUrl 长度(0,1024]
   * @param scene 0=qq 1=qzone
   */
  static shareNewsToQQ = async (title, description, preImage, url, scene = 0) => {
    return new Promise((resolve, reject) => {
      if (title.length === 0 || title.length > 128) {
        reject('share news title length invalid');
      }
      if (description.length === 0 || description.length > 512) {
        reject('share news description length invalid');
      }
      if (url.length === 0 || url.length > 1024) {
        reject('share news url length invalid');
      }
      if (scene === 0) {
        return QqOpenSdk.shareToQQ({type: 'news', title, description, imageUrl: preImage, webpageUrl: url});
      } else {
        return QqOpenSdk.shareToQZone({type: 'news', title, description, imageUrl: preImage, webpageUrl: url});
      }
    });
  };
}
