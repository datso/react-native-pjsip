package com.carusto.ReactNativePjSip.configuration;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import org.json.JSONObject;

import java.util.Map;

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
        c.useAnyway = true;
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

    public static NetworkConfiguration fromMap(Map conf) {
        NetworkConfiguration c = defaultConfiguration();
        if (conf.containsKey("useAnyway") && conf.get("useAnyway") instanceof Boolean) {
            c.useAnyway = (Boolean) conf.get("useAnyway");
        }
        if (conf.containsKey("useWifi") && conf.get("useWifi") instanceof Boolean) {
            c.useWifi = (Boolean) conf.get("useWifi");
        }
        if (conf.containsKey("use3g") && conf.get("use3g") instanceof Boolean) {
            c.use3g = (Boolean) conf.get("use3g");
        }
        if (conf.containsKey("useEdge") && conf.get("useEdge") instanceof Boolean) {
            c.useEdge = (Boolean) conf.get("useEdge");
        }
        if (conf.containsKey("useGprs") && conf.get("useGprs") instanceof Boolean) {
            c.useGprs = (Boolean) conf.get("useGprs");
        }
        if (conf.containsKey("useInRoaming") && conf.get("useInRoaming") instanceof Boolean) {
            c.useInRoaming = (Boolean) conf.get("useInRoaming");
        }
        if (conf.containsKey("useOtherNetworks") && conf.get("useOtherNetworks") instanceof Boolean) {
            c.useOtherNetworks = (Boolean) conf.get("useOtherNetworks");
        }

        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NetworkConfiguration that = (NetworkConfiguration) o;

        if (useAnyway != that.useAnyway) return false;
        if (useWifi != that.useWifi) return false;
        if (use3g != that.use3g) return false;
        if (useEdge != that.useEdge) return false;
        if (useGprs != that.useGprs) return false;
        if (useInRoaming != that.useInRoaming) return false;
        return useOtherNetworks == that.useOtherNetworks;

    }

    @Override
    public int hashCode() {
        int result = (useAnyway ? 1 : 0);
        result = 31 * result + (useWifi ? 1 : 0);
        result = 31 * result + (use3g ? 1 : 0);
        result = 31 * result + (useEdge ? 1 : 0);
        result = 31 * result + (useGprs ? 1 : 0);
        result = 31 * result + (useInRoaming ? 1 : 0);
        result = 31 * result + (useOtherNetworks ? 1 : 0);
        return result;
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
