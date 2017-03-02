package com.carusto.ReactNativePjSip;

import org.json.JSONObject;
import org.pjsip.pjsua2.OnInstantMessageParam;

public class PjSipMessage {

    private PjSipAccount account;

    private OnInstantMessageParam prm;

    public PjSipMessage(PjSipAccount account, OnInstantMessageParam prm) {
        this.account = account;
        this.prm = prm;
    }

    public OnInstantMessageParam getParam() {
        return prm;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            // -----
            json.put("accountId", account.getId());

            // -----
            json.put("contactUri", prm.getContactUri());
            json.put("fromUri", prm.getFromUri());
            json.put("toUri", prm.getToUri());
            json.put("body", prm.getMsgBody());
            json.put("contentType", prm.getContentType());

            return json;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toJsonString() {
        return toJson().toString();
    }

}
