// QqOpenSdkModule.java

package com.byronqqopensdk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.util.Date;

public class QqOpenSdkModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private final ReactApplicationContext reactContext;
    public DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter;
    private Tencent mTencent;

    private static final String NOT_INIT = "initApp required";
    private static final String RCTQQShareTypeNews = "news";
    private static final String RCTQQShareTypeImage = "image";
    private static final String RCTQQShareTypeText = "text";
    private static final String RCTQQShareTypeVideo = "video";
    private static final String RCTQQShareTypeAudio = "audio";

    private static final String RCTQQShareType = "type";
    private static final String RCTQQShareText = "text";
    private static final String RCTQQShareTitle = "title";
    private static final String RCTQQShareDescription = "description";
    private static final String RCTQQShareWebpageUrl = "webpageUrl";
    private static final String RCTQQShareImageUrl = "imageUrl";

    private static final int SHARE_RESULT_CODE_SUCCESSFUL = 0;
    private static final int SHARE_RESULT_CODE_FAILED = 1;
    private static final int SHARE_RESULT_CODE_CANCEL = 2;

    public QqOpenSdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "QqOpenSdk";
    }

    @ReactMethod
    public void initWithAppId(String appId) {
        if (mTencent != null) {
            return;
        }
        Tencent.setIsPermissionGranted(true);
        mTencent = Tencent.createInstance(appId, reactContext);
        eventEmitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
    }

    @ReactMethod
    public void isQQInstalled(Promise promise) {
        if (mTencent == null) {
            promise.resolve(false);
        }
        promise.resolve(mTencent.isQQInstalled(reactContext));
    }

    @ReactMethod
    public void authorize(ReadableArray permissions, Promise promise) {
        if (mTencent == null) {
            promise.resolve(false);
        }
        if (mTencent.isSessionValid()) {
            promise.resolve(false);
        }
        String scopes = permissions.getString(0);
        promise.resolve(mTencent.login(getCurrentActivity(), scopes, iUiListener));
    }

    @ReactMethod
    public void incrAuthWithPermissions(ReadableArray permissions, Promise promise) {
        if (mTencent == null) {
            promise.resolve(false);
        }
        promise.resolve(true);
    }

    @ReactMethod
    public void reauthorizeWithPermissions(ReadableArray permissions, Promise promise) {
        if (mTencent == null) {
            promise.resolve(false);
        }
        promise.resolve(true);
    }


    @ReactMethod
    public void logout() {
        if (mTencent == null) {
            return;
        }
        mTencent.logout(reactContext);
        mTencent = null;
        eventEmitter = null;
    }

    @ReactMethod
    public void shareToQQ(final ReadableMap data, final Promise promise) {
        if (mTencent == null) {
            promise.reject(NOT_INIT);
            return;
        }

        UiThreadUtil.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        _shareToQQ(data, 0);
                        promise.resolve(null);
                    }
                }
        );
    }

    private void _shareToQQ(ReadableMap data, int scene) {
        String type = RCTQQShareTypeNews;
        if (data.hasKey(RCTQQShareType)) {
            type = data.getString(RCTQQShareType);
        }

        if (type.equals(RCTQQShareText)) {
            try {
                Intent shareIntent = new Intent();
                ComponentName componentName = null;
                if (scene == 0) {
                    componentName =
                            new ComponentName(
                                    "com.tencent.mobileqq",
                                    "com.tencent.mobileqq.activity.JumpActivity"
                            );
                } else if (scene == 1) {
                    componentName =
                            new ComponentName(
                                    "com.qzone",
                                    "com.qzonex.module.operation.ui.QZonePublishMoodActivity"
                            );
                } else {
                    return;
                }
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        data.getString(RCTQQShareTypeText)
                );
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.setType("text/plain");
                shareIntent.setComponent(componentName);
                getReactApplicationContext().startActivity(shareIntent);

                WritableMap resultMap = Arguments.createMap();
                resultMap.putString("type", "QQShareResponse");
                resultMap.putInt("errCode", SHARE_RESULT_CODE_SUCCESSFUL);
                resultMap.putString("message", "Share successfully.");
                this.resolvePromise(resultMap);
            } catch (Exception e) {
                e.printStackTrace();

                WritableMap resultMap = Arguments.createMap();
                resultMap.putString("type", "QQShareResponse");
                resultMap.putInt("errCode", SHARE_RESULT_CODE_FAILED);
                resultMap.putString("message", "Share failed.");
                this.resolvePromise(resultMap);
            }
            return;
        }

        Bundle bundle = new Bundle();
        if (data.hasKey(RCTQQShareTitle)) {
            bundle.putString(
                    QQShare.SHARE_TO_QQ_TITLE,
                    data.getString(RCTQQShareTitle)
            );
        }
        if (data.hasKey(RCTQQShareDescription)) {
            bundle.putString(
                    QQShare.SHARE_TO_QQ_SUMMARY,
                    data.getString(RCTQQShareDescription)
            );
        }
        if (data.hasKey(RCTQQShareWebpageUrl)) {
            bundle.putString(
                    QQShare.SHARE_TO_QQ_TARGET_URL,
                    data.getString(RCTQQShareWebpageUrl)
            );
        }
        if (data.hasKey(RCTQQShareImageUrl)) {
            bundle.putString(
                    QQShare.SHARE_TO_QQ_IMAGE_URL,
                    data.getString(RCTQQShareImageUrl)
            );
        }
        if (data.hasKey("appName")) {
            bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, data.getString("appName"));
        }

        if (type.equals(RCTQQShareTypeNews)) {
            bundle.putInt(
                    QQShare.SHARE_TO_QQ_KEY_TYPE,
                    QQShare.SHARE_TO_QQ_TYPE_DEFAULT
            );
        } else if (type.equals(RCTQQShareTypeImage)) {
            String image = data.getString(RCTQQShareImageUrl);
            if (image.startsWith("content://") || image.startsWith("file://")) {
                image = getImageAbsolutePath(getCurrentActivity(), Uri.parse(image));
            }

            bundle.putInt(
                    QQShare.SHARE_TO_QQ_KEY_TYPE,
                    QQShare.SHARE_TO_QQ_TYPE_IMAGE
            );
            bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, image);
            bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, image);
        } else if (type.equals(RCTQQShareTypeAudio)) {
            bundle.putInt(
                    QQShare.SHARE_TO_QQ_KEY_TYPE,
                    QQShare.SHARE_TO_QQ_TYPE_AUDIO
            );
            if (data.hasKey("flashUrl")) {
                bundle.putString(
                        QQShare.SHARE_TO_QQ_AUDIO_URL,
                        data.getString("flashUrl")
                );
            }
        } else if (type.equals("app")) {
            // TODO: 腾讯SDK 3.5.2.15 弃用了分享APP功能
            //（模式4） 分享应用（已废弃）相关文档地址: https://wiki.connect.qq.com/%E5%88%86%E4%BA%AB%E6%B6%88%E6%81%AF%E5%88%B0qq%EF%BC%88%E6%97%A0%E9%9C%80qq%E7%99%BB%E5%BD%95%EF%BC%89
            // bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        }

        Log.e("QQShare", bundle.toString());

        if (scene == 0) {
            // Share to QQ.
            bundle.putInt(
                    QQShare.SHARE_TO_QQ_EXT_INT,
                    QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE
            );
            mTencent.shareToQQ(getCurrentActivity(), bundle, this);
        } else if (scene == 1) {
            // Share to Qzone.
            bundle.putInt(
                    QQShare.SHARE_TO_QQ_EXT_INT,
                    QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN
            );
            mTencent.shareToQQ(getCurrentActivity(), bundle, this);
        }
    }

    private void resolvePromise(ReadableMap resultMap) {
        getReactApplicationContext()
                .getJSModule(RCTNativeAppEventEmitter.class)
                .emit("QQ_Resp", resultMap);
    }

    public static String getImageAbsolutePath(Activity context, Uri imageUri) {
        if (context == null || imageUri == null) return null;
        if (
                android.os.Build.VERSION.SDK_INT >=
                        android.os.Build.VERSION_CODES.KITKAT &&
                        DocumentsContract.isDocumentUri(context, imageUri)
        ) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id)
                );
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[] { split[1] };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri)) return imageUri.getLastPathSegment();
            return getDataColumn(context, imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }

    public static String getDataColumn(
            Context context,
            Uri uri,
            String selection,
            String[] selectionArgs
    ) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = { column };
        try {
            cursor =
                    context
                            .getContentResolver()
                            .query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(
                uri.getAuthority()
        );
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    @Override
    public void initialize() {
        super.initialize();
        getReactApplicationContext().addActivityEventListener(this);
    }

    @Override
    public void onCatalystInstanceDestroy() {
        if (mTencent != null) {
            mTencent = null;
            eventEmitter = null;
        }
        getReactApplicationContext().removeActivityEventListener(this);
        super.onCatalystInstanceDestroy();
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode, resultCode, data, iUiListener);
    }

    public void onNewIntent(Intent intent) {}

    private final IUiListener iUiListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {
            try {
                JSONObject obj = (JSONObject) (o);
                WritableMap data = Arguments.createMap();
                data.putString("accessToken", obj.getString(Constants.PARAM_ACCESS_TOKEN));
                data.putDouble("expirationDate", (new Date().getTime() + obj.getLong(Constants.PARAM_EXPIRES_IN)));
                data.putString("openId", obj.getString(Constants.PARAM_OPEN_ID));
                data.putString("unionid", "");
                eventEmitter.emit("QQ_LOGIN", data);
            } catch (Exception e) {
                WritableMap data = Arguments.createMap();
                data.putBoolean("cancelled", false);
                data.putString("error", e.getLocalizedMessage());
                eventEmitter.emit("QQ_LOGIN", data);
            }
        }

        @Override
        public void onError(UiError uiError) {
            WritableMap data = Arguments.createMap();
            data.putBoolean("cancelled", false);
            data.putString("error", uiError.errorMessage);
            eventEmitter.emit("QQ_LOGIN", data);
        }

        @Override
        public void onCancel() {
            WritableMap data = Arguments.createMap();
            data.putBoolean("cancelled", true);
            data.putString("error", "Cancel login");
            eventEmitter.emit("QQ_LOGIN", data);
        }

        @Override
        public void onWarning(int i) {
            WritableMap data = Arguments.createMap();
            data.putString("error", "Warning login");
            eventEmitter.emit("QQ_LOGIN", data);
        }
    };
}
