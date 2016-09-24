package com.carusto.configuration;

import android.content.Intent;
import com.facebook.react.bridge.ReadableMap;
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

    public boolean isForegroundNotificationStatic() {
        return foregroundTitle != null && foregroundText != null && !foregroundTitle.isEmpty() && !foregroundText.isEmpty();
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
        ServiceConfiguration c = defaultConfiguration();

        if (intent.hasExtra("ua")) {
            c.ua = intent.getStringExtra("ua");
        }
        if (intent.hasExtra("foreground")) {
            c.foreground = intent.getBooleanExtra("foreground", false);
        }
        if (intent.hasExtra("foregroundTitle")) {
            c.foregroundTitle = intent.getStringExtra("foregroundTitle");
        }
        if (intent.hasExtra("foregroundText")) {
            c.foregroundText = intent.getStringExtra("foregroundText");
        }
        if (intent.hasExtra("foregroundInfo")) {
            c.foregroundInfo = intent.getStringExtra("foregroundInfo");
        }
        if (intent.hasExtra("foregroundTicker")) {
            c.foregroundTicker = intent.getStringExtra("foregroundTicker");
        }
        if (intent.hasExtra("foregroundSmallIcon")) {
            c.foregroundSmallIcon = intent.getStringExtra("foregroundSmallIcon");
        }
        if (intent.hasExtra("foregroundLargeIcon")) {
            c.foregroundLargeIcon = intent.getStringExtra("foregroundLargeIcon");
        }

        return c;
    }

    public static ServiceConfiguration fromConfiguration(ReadableMap data) {
        ServiceConfiguration c = defaultConfiguration();

        if (data.hasKey("ua")) {
            c.ua = data.getString("ua");
        }
        if (data.hasKey("foreground")) {
            c.foreground = data.getBoolean("foreground");
        }
        if (data.hasKey("foregroundTitle")) {
            c.foregroundTitle = data.getString("foregroundTitle");
        }
        if (data.hasKey("foregroundText")) {
            c.foregroundText = data.getString("foregroundText");
        }
        if (data.hasKey("foregroundInfo")) {
            c.foregroundInfo = data.getString("foregroundInfo");
        }
        if (data.hasKey("foregroundTicker")) {
            c.foregroundTicker = data.getString("foregroundTicker");
        }
        if (data.hasKey("foregroundSmallIcon")) {
            c.foregroundSmallIcon = data.getString("foregroundSmallIcon");
        }
        if (data.hasKey("foregroundLargeIcon")) {
            c.foregroundLargeIcon = data.getString("foregroundLargeIcon");
        }

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
