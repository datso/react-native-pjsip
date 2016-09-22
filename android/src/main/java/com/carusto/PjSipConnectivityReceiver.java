package com.carusto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import com.carusto.configuration.AccountConfiguration;
import com.carusto.configuration.NetworkConfiguration;

import java.util.List;

public class PjSipConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();

        List<AccountConfiguration> accounts = PjSipSharedPreferences.getAccounts(context);

        //
        // ACTION_DATA_STATE_CHANGED
        // Data state change is used to detect changes in the mobile
        // network such as a switch of network type (GPRS, EDGE, 3G)
        // which are not detected by the Connectivity changed broadcast.
        //
        //
        // ACTION_CONNECTIVITY_CHANGED
        // Connectivity change is used to detect changes in the overall
        // data network status as well as a switch between wifi and mobile
        // networks.
        //
        if (intentAction.equals(ConnectivityManager.CONNECTIVITY_ACTION) && accounts.size() > 0) {
            Intent serviceIntent = PjActions.createConnectivityChangedIntent(context);
            context.startService(serviceIntent);
        }
    }

}
