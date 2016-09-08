package com.carusto;

import android.util.Log;
import org.json.JSONObject;
import org.pjsip.pjsua2.*;

public class PjSipAccount extends Account {

    private static String TAG = "PjSipAccount";

    /**
     * Last registration reason.
     */
    private String reason;

    private PjSipService service;

    private String name;

    private String username;

    private String domain;

    private String password;

    private String proxy;

    private String transport;

    private String regServer;

    private Integer regTimeout;

    private Integer transportId;

    public PjSipAccount(PjSipService service, int transportId,
                        String name, String username, String domain, String password,
                        String proxy, String transport, String regServer, Integer regTimeout) {
        this.service = service;
        this.transportId = transportId;

        this.name = name;
        this.username = username;
        this.domain = domain;
        this.password = password;
        this.proxy = proxy;
        this.transport = transport;
        this.regServer = regServer;
        this.regTimeout = regTimeout;
    }

    public PjSipService getService() {
        return service;
    }

    public int getTransportId() {
        return transportId;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getDomain() {
        return domain;
    }

    public String getProxy() {
        return proxy;
    }

    public String getTransport() {
        return transport;
    }

    public String getRegServer() {
        return regServer;
    }

    public int getRegTimeout() {
        return regTimeout;
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
        service.handleCallReceived(call);
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
            json.put("name", name);
            json.put("username", username);
            json.put("domain", domain);
            json.put("password", password);
            json.put("proxy", proxy);
            json.put("transport", transport);
            json.put("regServer", regServer);
            json.put("regTimeout", regTimeout > 0 ? String.valueOf(regTimeout) : "");
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
