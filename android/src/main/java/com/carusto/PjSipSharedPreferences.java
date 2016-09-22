package com.carusto;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.carusto.configuration.AccountConfiguration;
import com.carusto.configuration.NetworkConfiguration;
import com.carusto.configuration.ServiceConfiguration;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class PjSipSharedPreferences {

    private static String TAG = "PjSipSharedPreferences";

    private static String NAME = "PJSIP_PREFERENCES";

    private static String ACCOUNTS_CONFIGURATION = "ACCOUNTS_CONFIGURATION";

    private static String NETWORK_CONFIGURATION = "NETWORK_CONFIGURATION";

    private static String SERVICE_CONFIGURATION = "SERVICE_CONFIGURATION";

    public static void addAccount(Context context, AccountConfiguration configuration) {
        List<AccountConfiguration> accounts = getAccounts(context);
        accounts.add(configuration);

        saveAccounts(context, accounts);
    }

    public static void deleteAccount(Context context, AccountConfiguration configuration) {
        List<AccountConfiguration> accounts = getAccounts(context);
        accounts.remove(configuration);

        saveAccounts(context, accounts);
    }

    public static JSONObject getSettingsAsJson(Context context) {
        // Format settings
        NetworkConfiguration networkConfiguration = PjSipSharedPreferences.getNetworkSettings(context);
        ServiceConfiguration serviceConfiguration = PjSipSharedPreferences.getServiceSettings(context);
        JSONObject settings = new JSONObject();

        try {
            settings.put("network", networkConfiguration.toJson());
            settings.put("service", serviceConfiguration.toJson());
        } catch (Exception e) {
            Log.d(TAG, "Failed to format settings json", e);
        }

        return settings;
    }

    private static void saveAccounts(Context context, List<AccountConfiguration> accounts) {
        String json = new Gson().toJson(accounts);
        getPreferences(context).edit().putString(ACCOUNTS_CONFIGURATION, json).commit();
    }

    public static List<AccountConfiguration> getAccounts(Context context) {
        String json = getPreferences(context).getString(ACCOUNTS_CONFIGURATION, "[]");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<AccountConfiguration>>(){}.getType();

        return gson.fromJson(json, listType);
    }

    public static NetworkConfiguration getNetworkSettings(Context context) {
        String json = getPreferences(context).getString(NETWORK_CONFIGURATION, "");

        if (json.isEmpty()) {
            return NetworkConfiguration.defaultConfiguration();
        }

        try {
            Gson gson = new Gson();
            return gson.fromJson(json, NetworkConfiguration.class);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse network configuration json ("+  json +")", e);
            return NetworkConfiguration.defaultConfiguration();
        }
    }

    public static void saveNetworkSettings(Context context, NetworkConfiguration configuration) {
        String json = new Gson().toJson(configuration);
        getPreferences(context).edit().putString(NETWORK_CONFIGURATION, json).commit();
    }

    public static ServiceConfiguration getServiceSettings(Context context) {
        String json = getPreferences(context).getString(SERVICE_CONFIGURATION, "");

        if (json.isEmpty()) {
            return ServiceConfiguration.defaultConfiguration();
        }

        try {
            Gson gson = new Gson();
            return gson.fromJson(json, ServiceConfiguration.class);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse service configuration json ("+  json +")", e);
            return ServiceConfiguration.defaultConfiguration();
        }
    }

    public static void saveServiceSettings(Context context, ServiceConfiguration configuration) {
        String json = new Gson().toJson(configuration);
        getPreferences(context).edit().putString(SERVICE_CONFIGURATION, json).commit();
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }
}
