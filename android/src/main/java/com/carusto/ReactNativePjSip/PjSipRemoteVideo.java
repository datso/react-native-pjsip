package com.carusto.ReactNativePjSip;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.VideoWindowHandle;
import org.pjsip.pjsua2.WindowHandle;

public class PjSipRemoteVideo extends PjSipVideo implements PjSipVideoMediaChange {

    private static String TAG = "PjSipRemoteVideo";

    public PjSipRemoteVideo(Context context) {
        super(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        PjSipCall.mediaListeners.add(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        PjSipCall.mediaListeners.remove(this);
    }

    public void setWindowId(int windowId) {
        final VideoWindow videoWindow = new VideoWindow(windowId);

        setCallback(new PjSipVideoWindowHandler() {
            @Override
            public VideoWindow start(SurfaceHolder surfaceHolder) throws Exception {
                WindowHandle winHandle = new WindowHandle();
                winHandle.setWindow(surfaceHolder.getSurface());

                VideoWindowHandle handle = new VideoWindowHandle();
                handle.setHandle(winHandle);

                videoWindow.setWindow(handle);
                return videoWindow;
            }
        });
    }

    @Override
    public void onChange() {
        Handler mainHandler = new Handler(getContext().getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    doLayout();
                } catch (Exception e) {
                    Log.e(TAG, "An error occurs while layout a video", e);
                }
            }
        });
    }
}
