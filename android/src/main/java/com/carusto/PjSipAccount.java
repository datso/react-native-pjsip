package com.carusto;

import android.util.Log;
import com.carusto.configuration.AccountConfiguration;
import org.json.JSONObject;
import org.pjsip.pjsua2.*;

public class PjSipAccount extends Account {

    private static String TAG = "PjSipAccount";

    /**
     * Last registration reason.
     */
    private String reason;

    private PjSipService service;

    private AccountConfiguration configuration;

    private Integer transportId;

    public PjSipAccount(PjSipService service, int transportId, AccountConfiguration configuration) {
        this.service = service;
        this.transportId = transportId;
        this.configuration = configuration;
    }

    public PjSipService getService() {
        return service;
    }

    public int getTransportId() {
        return transportId;
    }

    public AccountConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void onRegStarted(OnRegStartedParam prm) {
        Log.d(TAG, "onRegStarted: " + prm.getRenew());

        // Do not track registration renewal
        // if (!prm.getRenew()) {
        //     service.getEmitter().fireRegistrationChangeEvent(this);
        // }
    }

    @Override
    public void onRegState(OnRegStateParam prm) {
        reason = prm.getReason();
        service.getEmitter().fireRegistrationChangeEvent(this);
    }

    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {
        PjSipCall call = new PjSipCall(this, prm.getCallId());
        service.emmitCallReceived(call);
    }

    @Override
    public void onIncomingSubscribe(OnIncomingSubscribeParam prm) {
        super.onIncomingSubscribe(prm);
        Log.d(TAG, "onIncomingSubscribe");
    }

    @Override
    public void onInstantMessage(OnInstantMessageParam prm) {
        super.onInstantMessage(prm);
        Log.d(TAG, "onInstantMessage");
    }

    @Override
    public void onInstantMessageStatus(OnInstantMessageStatusParam prm) {
        super.onInstantMessageStatus(prm);
        Log.d(TAG, "onInstantMessageStatus");
    }

    @Override
    public void onTypingIndication(OnTypingIndicationParam prm) {
        super.onTypingIndication(prm);
        Log.d(TAG, "onTypingIndication");
    }

    @Override
    public void onMwiInfo(OnMwiInfoParam prm) {
        super.onMwiInfo(prm);
        Log.d(TAG, "onMwiInfo");
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            JSONObject registration = new JSONObject();
            registration.put("status", getInfo().getRegStatus());
            registration.put("statusText", getInfo().getRegStatusText());
            registration.put("active", getInfo().getRegIsActive());
            registration.put("reason", reason);

            json.put("id", getId());
            json.put("uri", getInfo().getUri());
            json.put("name", configuration.getName());
            json.put("username", configuration.getUsername());
            json.put("domain", configuration.getDomain());
            json.put("password", configuration.getPassword());
            json.put("proxy", configuration.getProxy());
            json.put("transport", configuration.getTransport());
            json.put("regServer", configuration.getRegServer());
            json.put("regTimeout", configuration.isRegTimeoutNotEmpty() ? String.valueOf(configuration.getRegTimeout()) : "");
            json.put("registration", registration);

            return json;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toJsonString() {
        return toJson().toString();
    }
}
