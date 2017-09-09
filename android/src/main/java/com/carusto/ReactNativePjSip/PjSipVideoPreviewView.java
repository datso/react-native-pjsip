package com.carusto.ReactNativePjSip;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pjsip.pjsua2.MediaFormat;
import org.pjsip.pjsua2.VideoPreview;
import org.pjsip.pjsua2.VideoPreviewOpParam;
import org.pjsip.pjsua2.VideoWindowHandle;
import org.pjsip.pjsua2.WindowHandle;

public class PjSipVideoPreviewView extends ViewGroup {

    private static String TAG = "PjSipVideoPreviewView";

    public PjSipVideoPreviewView(Context context) {
        super(context);

        SurfaceView surface = new SurfaceView(context);
        surface.setBackgroundColor(Color.GREEN);
        surface.setLayoutParams(new ViewGroup.LayoutParams(200, 200));

        // this.setBackgroundColor(Color.GREEN);
        // this.setLayoutMode(LAYOUT_MODE_CLIP_BOUNDS);

        TextView tv = new TextView(context);
        tv.setText("Hi BRO!");
        tv.setLayoutParams(new ViewGroup.LayoutParams(200, 200));

        addView(tv);

        // addView(surface);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout changed:" + changed);
        Log.d(TAG, "onLayout l:" + l);
        Log.d(TAG, "onLayout t:" + t);
        Log.d(TAG, "onLayout r:" + r);
        Log.d(TAG, "onLayout b:" + b);
    }
}
