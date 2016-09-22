package com.carusto.configuration;

import android.content.Intent;
import org.json.JSONObject;

public class ServiceConfiguration {

    public String ua;

    public boolean foreground;

    public String foregroundTitle;

    public String foregroundText;

    public String foregroundInfo;

    public String foregroundTicker;

    public String foregroundSmallIcon;

    public String foregroundLargeIcon;

    public String getUserAgent() {
        return ua;
    }

    public boolean isUserAgentNotEmpty() {
        return ua != null && !ua.isEmpty();
    }

    public boolean isForeground() {
        return foreground;
    }

    public String getForegroundTitle() {
        return foregroundTitle;
    }

    public String getForegroundText() {
        return foregroundText;
    }

    public String getForegroundInfo() {
        return foregroundInfo;
    }

    public String getForegroundTicker() {
        return foregroundTicker;
    }

    public String getForegroundSmallIcon() {
        return foregroundSmallIcon;
    }

    public String getForegroundLargeIcon() {
        return foregroundLargeIcon;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            json.put("ua", ua);
            json.put("foreground", foreground);
            json.put("foregroundTitle", foregroundTitle);
            json.put("foregroundText", foregroundText);
            json.put("foregroundInfo", foregroundInfo);
            json.put("foregroundTicker", foregroundTicker);
            json.put("foregroundSmallIcon", foregroundSmallIcon);
            json.put("foregroundLargeIcon", foregroundLargeIcon);

            return json;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ServiceConfiguration fromIntent(Intent intent) {
        ServiceConfiguration c = new ServiceConfiguration();
        c.ua = intent.getStringExtra("ua");
        c.foreground = intent.getBooleanExtra("foreground", false);
        c.foregroundTitle = intent.getStringExtra("foregroundTitle");
        c.foregroundText = intent.getStringExtra("foregroundText");
        c.foregroundInfo = intent.getStringExtra("foregroundInfo");
        c.foregroundTicker = intent.getStringExtra("foregroundTicker");
        c.foregroundSmallIcon = intent.getStringExtra("foregroundSmallIcon");
        c.foregroundLargeIcon = intent.getStringExtra("foregroundLargeIcon");

        return c;
    }

    /**
     * Returns configuration by default (only wifi and 3g enabled)
     */
    public static ServiceConfiguration defaultConfiguration() {
        ServiceConfiguration c = new ServiceConfiguration();
        c.foreground = true;

        return c;
    }

}
