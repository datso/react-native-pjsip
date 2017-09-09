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

public class PjSipVideoPreviewViewManager extends SimpleViewManager<View>  {

    private String LOCAL_VIDEO_CLASS = "PjSipLocalVideoView";

    @Override
    public String getName() {
        return LOCAL_VIDEO_CLASS;
    }

//    @Override
//    protected View createViewInstance(ThemedReactContext reactContext) {
//
//        final RelativeLayout container = new RelativeLayout(reactContext);
//        TextView textView = new TextView(reactContext);
//        textView.setText("hello world");
//        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(960, 150);
//        container.addView(textView, lp);
//        return container;
//
//
//        // return new PjSipVideoPreviewView(reactContext);
//    }

    @Override
    protected View createViewInstance(ThemedReactContext reactContext) {
        Log.d("PjSipVideoPreviewView", "createViewInstance start");
        SurfaceView surface = new SurfaceView(reactContext);
//        surface.setBackgroundColor(Color.GREEN);
//        surface.setMinimumHeight(200);
//        surface.setMinimumWidth(200);

        WindowHandle winHandle = new WindowHandle();
        winHandle.setWindow(surface.getHolder().getSurface());

        VideoWindowHandle vidWinHandle = new VideoWindowHandle();
        vidWinHandle.setHandle(winHandle);

        Log.d("PjSipVideoPreviewView", "createViewInstance 2");

        VideoPreview preview = new VideoPreview(2);
        MediaFormat previewFormat = new MediaFormat();
        VideoPreviewOpParam previewParam = new VideoPreviewOpParam();

//        VideoDevInfo devInfo = ep->vidDevManager().getDevInfo(camIdx);
//        MediaFormatVector fmtVec = devInfo.getFmt();
//        MediaFormat fmt;
//        for (int i = 0; i < fmtVec.size(); i++) {
//            fmt = fmtVec.get(i);
//            if (fmt.getType().get.get == 480 && fmt->width == 640 && fmt->id == PJMEDIA_FORMAT_I420)
//                break;
//        }

        previewParam.setFormat(previewFormat);
        previewParam.setShow(true);
        previewParam.setWindow(vidWinHandle);

        Log.d("PjSipVideoPreviewView", "createViewInstance 3");

        try {
            preview.start(previewParam);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("PjSipVideoPreviewView", "createViewInstance 4");

        final RelativeLayout container = new RelativeLayout(reactContext);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(200, 200);
        container.addView(surface, lp);

        return container;
    }
}
