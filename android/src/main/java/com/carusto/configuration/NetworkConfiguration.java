package com.carusto.configuration;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import org.json.JSONObject;

public class NetworkConfiguration {

    private static final String TAG = "NetworkConfiguration";

    public boolean useAnyway;

    public boolean useWifi;

    public boolean use3g;

    public boolean useEdge;

    public boolean useGprs;

    public boolean useInRoaming;

    public boolean useOtherNetworks;

    public boolean isUseAnyway() {
        return useAnyway;
    }

    public boolean isUseWifi() {
        return useWifi;
    }

    public boolean isUse3g() {
        return use3g;
    }

    public boolean isUseEdge() {
        return useEdge;
    }

    public boolean isUseGprs() {
        return useGprs;
    }

    public boolean isUseInRoaming() {
        return useInRoaming;
    }

    public boolean isUseOtherNetworks() {
        return useOtherNetworks;
    }

    /**
     * Check for acceptable network connection according to configuration
     */
    public boolean isMatches(NetworkInfo ni) {
        if (useAnyway) {
            return true;
        }

        if (ni == null) {
            return false;
        }

        // WiFi (we consider ethernet as wifi)
        if (useWifi) {
            int type = ni.getType();
            if (ni.isConnected() &&
                    // 9 = ConnectivityManager.TYPE_ETHERNET
                    (type == ConnectivityManager.TYPE_WIFI || type == 9)) {
                return true;
            }
        }

        // In Roaming
        if (!useInRoaming && ni.isRoaming()) {
            Log.d(TAG, "Application in roaming");
            return false;
        }

        int type = ni.getType();

        // Any mobile network connected
        if (ni.isConnected() &&
                // Type 3,4,5 are other mobile data ways
                (type == ConnectivityManager.TYPE_MOBILE || (type <= 5 && type >= 3))) {
            int subType = ni.getSubtype();

            // 3G (or better)
            if (use3g && subType >= TelephonyManager.NETWORK_TYPE_UMTS) {
                return true;
            }

            // GPRS (or unknown)
            if (useGprs && (subType == TelephonyManager.NETWORK_TYPE_GPRS || subType == TelephonyManager.NETWORK_TYPE_UNKNOWN)) {
                return true;
            }

            // EDGE
            if (useEdge && subType == TelephonyManager.NETWORK_TYPE_EDGE) {
                return true;
            }
        }

        // Other networks
        if (useOtherNetworks && ni.getType() != ConnectivityManager.TYPE_MOBILE && ni.getType() != ConnectivityManager.TYPE_WIFI) {
            return true;
        }

        Log.d(TAG, "Application not match any type (" + type + ")" );

        return false;
    }

    /**
     * Returns configuration by default (only wifi and 3g enabled)
     */
    public static NetworkConfiguration defaultConfiguration() {
        NetworkConfiguration c = new NetworkConfiguration();
        c.useWifi = true;
        c.use3g = true;

        return c;
    }

    public static NetworkConfiguration fromIntent(Intent intent) {
        NetworkConfiguration c = new NetworkConfiguration();
        c.useAnyway = intent.getBooleanExtra("useAnyway", false);
        c.useWifi = intent.getBooleanExtra("useWifi", true);
        c.use3g = intent.getBooleanExtra("use3g", true);
        c.useEdge = intent.getBooleanExtra("useEdge", false);
        c.useGprs = intent.getBooleanExtra("useGprs", false);
        c.useInRoaming = intent.getBooleanExtra("useInRoaming", false);
        c.useOtherNetworks = intent.getBooleanExtra("useOtherNetworks", false);

        return c;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            json.put("useAnyway", useAnyway);
            json.put("useWifi", useWifi);
            json.put("use3g", use3g);
            json.put("useEdge", useEdge);
            json.put("useGprs", useGprs);
            json.put("useInRoaming", useInRoaming);
            json.put("useOtherNetworks", useOtherNetworks);

            return json;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
