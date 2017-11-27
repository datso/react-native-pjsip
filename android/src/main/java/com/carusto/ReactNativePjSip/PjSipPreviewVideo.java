package com.carusto.ReactNativePjSip;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;

import org.pjsip.pjsua2.VideoPreview;
import org.pjsip.pjsua2.VideoPreviewOpParam;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.VideoWindowHandle;

public class PjSipPreviewVideo extends PjSipVideo {

    private int deviceId = -1;

    public PjSipPreviewVideo(Context context) {
        super(context);
    }

    public void setDeviceId(int deviceId) {
        if (this.deviceId == deviceId) {
            return;
        }

        final VideoPreview windowPreview = new VideoPreview(deviceId);

        setCallback(new PjSipVideoWindowHandler() {
            @Override
            public VideoWindow start(SurfaceHolder surfaceHolder) throws Exception {
                VideoWindowHandle handle = new VideoWindowHandle();
                handle.getHandle().setWindow(surfaceHolder.getSurface());

                VideoPreviewOpParam op = new VideoPreviewOpParam();
                op.setWindow(handle);

                windowPreview.start(op);
                return windowPreview.getVideoWindow();
            }
        });
    }
}
