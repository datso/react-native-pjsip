package com.carusto.ReactNativePjSip.configuration;

import com.facebook.react.bridge.ReadableMap;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

public class ServiceNotificationConfiguration {

    private boolean enabled = true;

    private String title;

    private String text;

    private String info;

    private String ticker;

    private String smallIcon;

    private String largeIcon;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(String smallIcon) {
        this.smallIcon = smallIcon;
    }

    public String getLargeIcon() {
        return largeIcon;
    }

    public void setLargeIcon(String largeIcon) {
        this.largeIcon = largeIcon;
    }

    public boolean isForegroundNotificationStatic() {
        return title != null && text != null && !title.isEmpty() && !text.isEmpty();
    }

    public ServiceNotificationConfiguration copy(String title, String text) {
        ServiceNotificationConfiguration c = new ServiceNotificationConfiguration();
        c.setEnabled(enabled);
        c.setTitle(title);
        c.setText(text);
        c.setTicker(ticker);
        c.setSmallIcon(smallIcon);
        c.setLargeIcon(largeIcon);

        return c;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            json.put("enabled", enabled);
            json.put("title", title);
            json.put("text", text);
            json.put("info", info);
            json.put("ticker", ticker);
            json.put("smallIcon", smallIcon);
            json.put("largeIcon", largeIcon);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return json;
    }

    public static ServiceNotificationConfiguration fromReadableMap(ReadableMap data) {
        ServiceNotificationConfiguration config = new ServiceNotificationConfiguration();

        if (data.hasKey("enabled")) {
            config.setEnabled(data.getBoolean("enabled"));
        }
        if (data.hasKey("title")) {
            config.setTitle(data.getString("title"));
        }
        if (data.hasKey("text")) {
            config.setText(data.getString("text"));
        }
        if (data.hasKey("info")) {
            config.setInfo(data.getString("info"));
        }
        if (data.hasKey("ticker")) {
            config.setTicker(data.getString("ticker"));
        }
        if (data.hasKey("smallIcon")) {
            config.setSmallIcon(data.getString("smallIcon"));
        }
        if (data.hasKey("largeIcon")) {
            config.setLargeIcon(data.getString("largeIcon"));
        }

        return config;
    }

    public static ServiceNotificationConfiguration fromMap(Object value) {
        ServiceNotificationConfiguration config = new ServiceNotificationConfiguration();

        if (value instanceof Map) {
            Map data = (Map) value;

            if (data.containsKey("enabled")) {
                config.setEnabled((Boolean) data.get("enabled"));
            }
            if (data.containsKey("title")) {
                config.setTitle((String) data.get("title"));
            }
            if (data.containsKey("text")) {
                config.setText((String) data.get("text"));
            }
            if (data.containsKey("info")) {
                config.setInfo((String) data.get("info"));
            }
            if (data.containsKey("ticker")) {
                config.setTicker((String) data.get("ticker"));
            }
            if (data.containsKey("smallIcon")) {
                config.setSmallIcon((String) data.get("smallIcon"));
            }
            if (data.containsKey("largeIcon")) {
                config.setLargeIcon((String) data.get("largeIcon"));
            }
        }

        return config;
    }
}
