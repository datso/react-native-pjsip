package com.carusto.ReactNativePjSip.dto;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.google.gson.Gson;

import org.pjsip.pjsua2.SipHeaderVector;

import java.util.HashMap;
import java.util.Map;

public class SipMessageDTO {

    private String targetUri;
    private Map<String, String> headers;
    private String contentType;
    private String body;

    public String getTargetUri() {
        return targetUri;
    }

    public void setTargetUri(String targetUri) {
        this.targetUri = targetUri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String toJson () {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static SipMessageDTO fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, SipMessageDTO.class);
    }

    public static SipMessageDTO fromReadableMap(ReadableMap data) {
        SipMessageDTO result = new SipMessageDTO();

        if (data.hasKey("targetURI")) {
            result.setTargetUri(data.getString("targetURI"));
        }
        if (data.hasKey("headers")) {
            ReadableMap headersData = data.getMap("headers");
            ReadableMapKeySetIterator headersIt = headersData.keySetIterator();
            Map<String, String> headers = new HashMap<>();

            while (headersIt.hasNextKey()) {
                String key = headersIt.nextKey();
                headers.put(key, headersData.getString(key));
            }

            result.setHeaders(headers);
        }
        if (data.hasKey("contentType")) {
            result.setContentType(data.getString("contentType"));
        }
        if (data.hasKey("body")) {
            result.setBody(data.getString("body"));
        }

        return result;
    }

}
