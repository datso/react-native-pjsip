package com.carusto.ReactNativePjSip;

import android.util.Log;
import org.pjsip.pjsua2.LogEntry;
import org.pjsip.pjsua2.LogWriter;
import org.pjsip.pjsua2.pjsua2JNI;

public class PjSipLogWriter extends LogWriter {

    private static String TAG = "PjSipLogWriter";

    public void write(LogEntry entry) {
        Log.d(TAG, entry.getMsg());
    }

}
