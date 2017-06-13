package com.carusto.ReactNativePjSip.configuration;

import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.ReadableMap;
import org.json.JSONObject;

import java.util.Map;

public class ServiceConfiguration {

    public String ua;

    public ServiceNotificationConfiguration accountNotificationConfiguration;

    public ServiceNotificationConfiguration callNotificationConfiguration;

    public String getUserAgent() {
        return ua;
    }

    public boolean isUserAgentNotEmpty() {
        return ua != null && !ua.isEmpty();
    }

    public ServiceNotificationConfiguration getCallNotificationConfiguration() {
        return callNotificationConfiguration;
    }

    public ServiceNotificationConfiguration getAccountNotificationConfiguration() {
        return accountNotificationConfiguration;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            json.put("ua", ua);

            JSONObject notifications = new JSONObject();
            notifications.put("account", accountNotificationConfiguration.toJson());
            notifications.put("call", callNotificationConfiguration.toJson());
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
                c.accountNotificationConfiguration = new ServiceNotificationConfiguration();
                c.accountNotificationConfiguration.setEnabled(enabled);
                c.callNotificationConfiguration = new ServiceNotificationConfiguration();
                c.callNotificationConfiguration.setEnabled(enabled);
            } catch (Exception e) {
                Map notifications = (Map) intent.getSerializableExtra("notifications");
                if (notifications.containsKey("account")) {
                    c.accountNotificationConfiguration = ServiceNotificationConfiguration.fromMap(notifications.get("account"));
                }
                if (notifications.containsKey("call")) {
                    c.callNotificationConfiguration = ServiceNotificationConfiguration.fromMap(notifications.get("call"));
                }
            }
        }

        if (c.accountNotificationConfiguration == null) {
            c.accountNotificationConfiguration = new ServiceNotificationConfiguration();
        }
        if (c.callNotificationConfiguration == null) {
            c.callNotificationConfiguration = new ServiceNotificationConfiguration();
        }

        return c;
    }

    public static ServiceConfiguration fromMap(Map conf) {
        ServiceConfiguration c = defaultConfiguration();

        if (conf.containsKey("ua")) {
            c.ua = conf.get("ua").toString();
        }

        if (conf.containsKey("notifications")) {
            Object notifications = conf.get("notifications");

            if (notifications instanceof Boolean) {
                boolean enabled = (Boolean) notifications;
                c.accountNotificationConfiguration.setEnabled(enabled);
                c.callNotificationConfiguration.setEnabled(enabled);
            } else if (notifications instanceof Map) {
                Map entityConfiguration = (Map) notifications;
                if (entityConfiguration.containsKey("account")) {
                    Object entity = entityConfiguration.get("account");

                    if (entity instanceof Boolean) {
                        c.accountNotificationConfiguration.setEnabled((Boolean) entity);
                    } else if (entity instanceof Map) {
                        c.accountNotificationConfiguration = ServiceNotificationConfiguration.fromMap(entity);
                    }
                }
                if (entityConfiguration.containsKey("call")) {
                    Object entity = entityConfiguration.get("call");

                    if (entity instanceof Boolean) {
                        c.callNotificationConfiguration.setEnabled((Boolean) entity);
                    } else if (entity instanceof Map) {
                        c.callNotificationConfiguration = ServiceNotificationConfiguration.fromMap(entity);
                    }
                }
            }
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
                    c.accountNotificationConfiguration = new ServiceNotificationConfiguration();
                    c.accountNotificationConfiguration.setEnabled(enabled);
                    c.callNotificationConfiguration = new ServiceNotificationConfiguration();
                    c.callNotificationConfiguration.setEnabled(enabled);
                    break;
                case Map:
                    ReadableMap notifications = data.getMap("notifications");
                    if (notifications.hasKey("account")) {
                        switch (notifications.getType("account")) {
                            case Boolean:
                                c.accountNotificationConfiguration = new ServiceNotificationConfiguration();
                                c.accountNotificationConfiguration.setEnabled(notifications.getBoolean("account"));
                                break;
                            case Map:
                                c.accountNotificationConfiguration = ServiceNotificationConfiguration.fromReadableMap(notifications.getMap("account"));
                                break;
                        }
                    }
                    if (notifications.hasKey("call")) {
                        switch (notifications.getType("call")) {
                            case Boolean:
                                c.callNotificationConfiguration = new ServiceNotificationConfiguration();
                                c.callNotificationConfiguration.setEnabled(notifications.getBoolean("call"));
                                break;
                            case Map:
                                c.callNotificationConfiguration = ServiceNotificationConfiguration.fromReadableMap(notifications.getMap("call"));
                                break;
                        }
                    }
                    break;
            }
        }

        if (c.accountNotificationConfiguration == null) {
            c.accountNotificationConfiguration = new ServiceNotificationConfiguration();
        }
        if (c.callNotificationConfiguration == null) {
            c.callNotificationConfiguration = new ServiceNotificationConfiguration();
        }

        return c;
    }

    /**
     * Returns configuration by default (only wifi and 3g enabled)
     */
    public static ServiceConfiguration defaultConfiguration() {
        ServiceConfiguration c = new ServiceConfiguration();
        c.accountNotificationConfiguration = new ServiceNotificationConfiguration();
        c.callNotificationConfiguration = new ServiceNotificationConfiguration();

        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceConfiguration that = (ServiceConfiguration) o;

        if (ua != null ? !ua.equals(that.ua) : that.ua != null) return false;
        if (accountNotificationConfiguration != null ? !accountNotificationConfiguration.equals(that.accountNotificationConfiguration) : that.accountNotificationConfiguration != null)
            return false;
        return callNotificationConfiguration != null ? callNotificationConfiguration.equals(that.callNotificationConfiguration) : that.callNotificationConfiguration == null;

    }

    @Override
    public int hashCode() {
        int result = ua != null ? ua.hashCode() : 0;
        result = 31 * result + (accountNotificationConfiguration != null ? accountNotificationConfiguration.hashCode() : 0);
        result = 31 * result + (callNotificationConfiguration != null ? callNotificationConfiguration.hashCode() : 0);
        return result;
    }
}
