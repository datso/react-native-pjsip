package com.carusto.ReactNativePjSip;

import android.graphics.Color;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewProps;

import org.pjsip.pjsua2.MediaFormat;
import org.pjsip.pjsua2.MediaFormatVector;
import org.pjsip.pjsua2.VideoDevInfo;
import org.pjsip.pjsua2.VideoPreview;
import org.pjsip.pjsua2.VideoPreviewOpParam;
import org.pjsip.pjsua2.VideoWindowHandle;
import org.pjsip.pjsua2.WindowHandle;

public class PjSipPreviewVideoViewManager extends SimpleViewManager<PjSipPreviewVideo>  {

    private String LOCAL_VIDEO_CLASS = "PjSipPreviewVideoView";

    @Override
    public String getName() {
        return LOCAL_VIDEO_CLASS;
    }

    @ReactProp(name = "deviceId")
    public void setDeviceId(PjSipPreviewVideo view, int deviceId) {
        view.setDeviceId(deviceId);
    }

    @ReactProp(name = "objectFit")
    public void setObjectFit(PjSipPreviewVideo view, String objectFit) {
        view.setObjectFit(objectFit);
    }

    @Override
    protected PjSipPreviewVideo createViewInstance(ThemedReactContext reactContext) {
        return new PjSipPreviewVideo(reactContext);
    }

}
