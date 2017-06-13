package com.carusto.ReactNativePjSip.configuration;

import android.util.Log;

import org.json.JSONObject;

public class ConfigurationUtils {

    private static String TAG = "ConfigurationUtils";

    public static JSONObject getSettingsAsJson(ServiceConfiguration serviceConfiguration,
                                               NetworkConfiguration networkConfiguration) {
        JSONObject settings = new JSONObject();

        try {
            settings.put("network", networkConfiguration.toJson());
            settings.put("service", serviceConfiguration.toJson());
        } catch (Exception e) {
            Log.d(TAG, "Failed to format settings json", e);
        }

        return settings;
    }

}
