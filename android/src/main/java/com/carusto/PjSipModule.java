package com.carusto;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.facebook.react.bridge.*;

public class PjSipModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private static String TAG = "PjSipModule";

    private static PjSipBroadcastReceiver receiver;

    public PjSipModule(ReactApplicationContext context) {
        super(context);

        // Module could be started several times, but we have to register receiver only once.
        if (receiver == null) {
            receiver = new PjSipBroadcastReceiver(context);
            this.getReactApplicationContext().registerReceiver(receiver, receiver.getFilter());
        } else {
            receiver.setContext(context);
        }
    }

    @Override
    public void initialize() {
        getReactApplicationContext().addLifecycleEventListener(this);

        Intent intent = PjActions.createAppVisibleIntent(getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @Override
    public String getName() {
        return "PjSipModule";
    }

    @ReactMethod
    public void start(Callback callback) {
        boolean foreground = false;
        final Activity activity = getCurrentActivity();

        if (activity != null) {
            Intent activityIntent = activity.getIntent();
            if (activityIntent != null) {
                foreground = activityIntent.getBooleanExtra("foreground", false);
            }
        }

        Intent intent = new Intent(getReactApplicationContext(), PjSipService.class);
        intent.setAction(PjActions.ACTION_START);
        intent.putExtra("callback_id", receiver.register(callback));
        intent.putExtra("foreground", foreground);

        getReactApplicationContext().startService(intent);
    }

    // TODO: set network configuration
    // TODO: set media configuration

    @ReactMethod
    public void createAccount(ReadableMap configuration, Callback callback) {
        int id = receiver.register(callback);
        Intent intent = PjActions.createAccountCreateIntent(id, configuration, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void deleteAccount(int accountId, Callback callback) {
        int callbackId = receiver.register(callback);
        Intent intent = PjActions.createAccountDeleteIntent(callbackId, accountId, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void makeCall(int accountId, String destination, Callback callback) {
        int callbackId = receiver.register(callback);
        Intent intent = PjActions.createMakeCallIntent(callbackId, accountId, destination, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void hangupCall(int callId, Callback callback) {
        int callbackId = receiver.register(callback);
        Intent intent = PjActions.createHangupCallIntent(callbackId, callId, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void answerCall(int callId, Callback callback) {
        int callbackId = receiver.register(callback);
        Intent intent = PjActions.createAnswerCallIntent(callbackId, callId, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void holdCall(int callId, Callback callback) {
        int callbackId = receiver.register(callback);
        Intent intent = PjActions.createHoldCallIntent(callbackId, callId, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void unholdCall(int callId, Callback callback) {
        int callbackId = receiver.register(callback);
        Intent intent = PjActions.createUnholdCallIntent(callbackId, callId, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void muteCall(int callId, Callback callback) {
        int callbackId = receiver.register(callback);
        Intent intent = PjActions.createMuteCallIntent(callbackId, callId, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void unMuteCall(int callId, Callback callback) {
        int callbackId = receiver.register(callback);
        Intent intent = PjActions.createUnMuteCallIntent(callbackId, callId, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void useSpeaker(int callId, Callback callback) {
        int callbackId = receiver.register(callback);
        Intent intent = PjActions.createUseSpeakerCallIntent(callbackId, callId, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void useEarpiece(int callId, Callback callback) {
        int callbackId = receiver.register(callback);
        Intent intent = PjActions.createUseEarpieceCallIntent(callbackId, callId, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void xferCall(int callId, String destination, Callback callback) {
        int callbackId = receiver.register(callback);
        Intent intent = PjActions.createXFerCallIntent(callbackId, callId, destination, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void xferReplacesCall(int callId, int destCallId, Callback callback) {
        int callbackId = receiver.register(callback);
        Intent intent = PjActions.createXFerReplacesCallIntent(callbackId, callId, destCallId, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void redirectCall(int callId, String destination, Callback callback) {
        int callbackId = receiver.register(callback);
        Intent intent = PjActions.createRedirectCallIntent(callbackId, callId, destination, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void dtmfCall(int callId, String digits, Callback callback) {
        int callbackId = receiver.register(callback);
        Intent intent = PjActions.createDtmfCallIntent(callbackId, callId, digits, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void startForeground(ReadableMap configuration, Callback callback) {
        int id = receiver.register(callback);
        Intent intent = PjActions.createStartForegroundIntent(id, configuration, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @ReactMethod
    public void stopForeground(Callback callback) {
        int id = receiver.register(callback);
        Intent intent = PjActions.createStopForegroundIntent(id, getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @Override
    public void onHostResume() {
        Intent intent = PjActions.createAppVisibleIntent(getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @Override
    public void onHostPause() {
        Intent intent = PjActions.createAppHiddenIntent(getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }

    @Override
    public void onHostDestroy() {
        Intent intent = PjActions.createAppHiddenIntent(getReactApplicationContext());
        getReactApplicationContext().startService(intent);
    }
}
