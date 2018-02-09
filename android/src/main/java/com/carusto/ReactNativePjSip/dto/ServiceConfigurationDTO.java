package com.carusto.ReactNativePjSip.dto;

import android.content.Intent;

import com.facebook.react.bridge.ReadableMap;
import org.json.JSONObject;
import org.pjsip.pjsua2.StringVector;

import java.util.Map;
import java.util.ArrayList;

public class ServiceConfigurationDTO {

    public String ua;
    public ArrayList<String> stun;

    public String getUserAgent() {
        return ua;
    }

    public StringVector getStunServers() {
        StringVector serversVector = new StringVector();
        for (String server : stun) {
            serversVector.add(server);
        }
        return serversVector;
    }

    public boolean isUserAgentNotEmpty() {
        return ua != null && !ua.isEmpty();
    }

    public boolean isStunServersNotEmpty() {
        return stun != null && stun.size() > 0;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            json.put("ua", ua);

            return json;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ServiceConfigurationDTO fromIntent(Intent intent) {
        ServiceConfigurationDTO c = new ServiceConfigurationDTO();

        if (intent.hasExtra("ua")) {
            c.ua = intent.getStringExtra("ua");
        }

        return c;
    }

    public static ServiceConfigurationDTO fromMap(Map conf) {
        ServiceConfigurationDTO c = new ServiceConfigurationDTO();

        if (conf.containsKey("ua")) {
            c.ua = conf.get("ua").toString();
        }

        if (conf.containsKey("stun")) {
            c.stun = (ArrayList) conf.get("stun");
        }

        return c;
    }

    public static ServiceConfigurationDTO fromConfiguration(ReadableMap data) {
        ServiceConfigurationDTO c = new ServiceConfigurationDTO();

        if (data.hasKey("ua")) {
            c.ua = data.getString("ua");
        }

        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceConfigurationDTO that = (ServiceConfigurationDTO) o;

        return ua != null ? ua.equals(that.ua) : that.ua == null;
    }

    @Override
    public int hashCode() {
        return ua != null ? ua.hashCode() : 0;
    }
}
