package com.carusto.ReactNativePjSip;

import android.view.SurfaceHolder;

import org.pjsip.pjsua2.VideoWindow;

public interface PjSipVideoWindowHandler {
    VideoWindow start(SurfaceHolder surfaceHolder) throws Exception;
}
