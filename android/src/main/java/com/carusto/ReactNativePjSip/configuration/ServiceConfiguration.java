package com.carusto.ReactNativePjSip.configuration;

import android.content.Intent;
import android.util.Log;
import com.facebook.react.bridge.ReadableMap;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

public class ServiceConfiguration {

    public String ua;

    public ServiceNotificationConfiguration accountNotification;

    public ServiceNotificationConfiguration callNotification;

    public String getUserAgent() {
        return ua;
    }

    public boolean isUserAgentNotEmpty() {
        return ua != null && !ua.isEmpty();
    }

    public ServiceNotificationConfiguration getCallNotification() {
        return callNotification;
    }

    public ServiceNotificationConfiguration getAccountNotification() {
        return accountNotification;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            json.put("ua", ua);

            JSONObject notifications = new JSONObject();
            notifications.put("account", accountNotification.toJson());
            notifications.put("call", accountNotification.toJson());
            json.put("notifications", notifications);

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

        if (intent.hasExtra("notifications")) {
            try {
                boolean enabled = intent.getBooleanExtra("notifications", true);
                c.accountNotification = new ServiceNotificationConfiguration();
                c.accountNotification.setEnabled(enabled);
                c.callNotification = new ServiceNotificationConfiguration();
                c.callNotification.setEnabled(enabled);
            } catch (Exception e) {
                Map notifications = (Map) intent.getSerializableExtra("notifications");
                if (notifications.containsKey("account")) {
                    c.accountNotification = ServiceNotificationConfiguration.fromMap(notifications.get("account"));
                }
                if (notifications.containsKey("call")) {
                    c.callNotification = ServiceNotificationConfiguration.fromMap(notifications.get("call"));
                }
            }
        }

        if (c.accountNotification == null) {
            c.accountNotification = new ServiceNotificationConfiguration();
        }
        if (c.callNotification == null) {
            c.callNotification = new ServiceNotificationConfiguration();
        }

        return c;
    }

    public static ServiceConfiguration fromConfiguration(ReadableMap data) {
        ServiceConfiguration c = defaultConfiguration();

        if (data.hasKey("ua")) {
            c.ua = data.getString("ua");
        }

        if (data.hasKey("notifications")) {
            switch (data.getType("notifications")) {
                case Boolean:
                    boolean enabled = data.getBoolean("notifications");
                    c.accountNotification = new ServiceNotificationConfiguration();
                    c.accountNotification.setEnabled(enabled);
                    c.callNotification = new ServiceNotificationConfiguration();
                    c.callNotification.setEnabled(enabled);
                    break;
                case Map:
                    ReadableMap notifications = data.getMap("notifications");
                    if (notifications.hasKey("account")) {
                        switch (notifications.getType("account")) {
                            case Boolean:
                                c.accountNotification = new ServiceNotificationConfiguration();
                                c.accountNotification.setEnabled(notifications.getBoolean("account"));
                                break;
                            case Map:
                                c.accountNotification = ServiceNotificationConfiguration.fromReadableMap(notifications.getMap("account"));
                                break;
                        }
                    }
                    if (notifications.hasKey("call")) {
                        switch (notifications.getType("call")) {
                            case Boolean:
                                c.callNotification = new ServiceNotificationConfiguration();
                                c.callNotification.setEnabled(notifications.getBoolean("call"));
                                break;
                            case Map:
                                c.callNotification = ServiceNotificationConfiguration.fromReadableMap(notifications.getMap("call"));
                                break;
                        }
                    }
                    break;
            }
        }

        if (c.accountNotification == null) {
            c.accountNotification = new ServiceNotificationConfiguration();
        }
        if (c.callNotification == null) {
            c.callNotification = new ServiceNotificationConfiguration();
        }

        return c;
    }

    /**
     * Returns configuration by default (only wifi and 3g enabled)
     */
    public static ServiceConfiguration defaultConfiguration() {
        ServiceConfiguration c = new ServiceConfiguration();
        c.accountNotification = new ServiceNotificationConfiguration();
        c.callNotification = new ServiceNotificationConfiguration();

        return c;
    }

}
