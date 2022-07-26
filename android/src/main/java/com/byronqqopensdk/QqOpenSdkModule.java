// QqOpenSdkModule.java

package com.byronqqopensdk;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import com.tencent.connect.common.Constants;

import java.util.Date;

public class QqOpenSdkModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private final ReactApplicationContext reactContext;
    public DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter;
    private Tencent mTencent;

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
