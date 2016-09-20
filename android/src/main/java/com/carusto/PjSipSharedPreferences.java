package com.carusto;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.carusto.configuration.AccountConfiguration;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.pjsip.pjsua2.AccountConfig;
import java.lang.reflect.Type;
import java.util.List;

public class PjSipSharedPreferences {

    private static String TAG = "PjSipSharedPreferences";

    private static String NAME = "PJSIP_PREFERENCES";

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

    private static void saveAccounts(Context context, List<AccountConfiguration> accounts) {
        String json = new Gson().toJson(accounts);
        getPreferences(context).edit().putString("accounts", json).commit();
    }

    public static List<AccountConfiguration> getAccounts(Context context) {
        String json = getPreferences(context).getString("accounts", "[]");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<AccountConfiguration>>(){}.getType();

        return gson.fromJson(json, listType);
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

}
