package com.carusto.ReactNativePjSip;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import org.pjsip.pjsua2.MediaSize;
import org.pjsip.pjsua2.VideoWindow;

public abstract class PjSipVideo extends ViewGroup implements SurfaceHolder.Callback {

    private static String TAG = "PjSipVideo";

    private String objectFit = "cover";

    private SurfaceHolder surfaceHolder;

    private SurfaceView surfaceView;

    private PjSipVideoWindowHandler videoWindowHandler;

    private VideoWindow videoWindow;

    private int layoutLeft = 0;

    private int layoutTop = 0;

    private int layoutRight = 0;

    private int layoutBottom = 0;

    public PjSipVideo(Context context) {
        super(context);

        surfaceView = new SurfaceView(context);
        surfaceView.getHolder().addCallback(this);
        surfaceView.setZOrderOnTop(false);
        addView(surfaceView);
    }

    protected void setCallback(PjSipVideoWindowHandler callback) {
        videoWindowHandler = callback;

        if (surfaceHolder != null) {
            try {
                videoWindow = this.videoWindowHandler.start(surfaceHolder);
                doLayout();
            } catch (Exception e) {
                Log.e(TAG, "An error occurs during getting video window by surface", e);
            }
        }
    }

    public void setObjectFit(String type) {
        if (objectFit.equals(type.toLowerCase())) {
            return;
        }

        objectFit = type.toLowerCase();

        try {
            doLayout();
        } catch (Exception e) {
            Log.e(TAG, "An error occurs during setting object fit with active video window.", e);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutTop = t;
        layoutLeft = l;
        layoutBottom = b;
        layoutRight = r;

        if (surfaceHolder == null) {
            surfaceView.layout(0, 0, r - l, b - t);
        } else {
            try {
                doLayout();
            } catch (Exception e) {
                Log.e(TAG, "An error occurs during layout", e);
            }
        }
    }

    protected void doLayout() throws Exception {
        int layoutWidth = layoutRight - layoutLeft;
        int layoutHeight = layoutBottom - layoutTop;

        if (surfaceView == null || videoWindow == null) {
            return;
        }

        MediaSize size = videoWindow.getInfo().getSize();
        int height;
        int width;

        switch (objectFit) {
            case "cover": {
                if (size.getH() == size.getW()) {
                    int max = Math.max(layoutWidth, layoutHeight);
                    width = max;
                    height = max;
                } else if (size.getW() / layoutWidth > size.getH() / layoutHeight) {
                    height = layoutHeight;
                    width = Math.round(((float) layoutHeight / size.getH()) * size.getW());
                } else {
                    width = layoutWidth;
                    height = Math.round(((float) layoutWidth / size.getW()) * size.getH());
                }

                break;
            }
            case "contain":
            default: {
                if (size.getH() == size.getW()) {
                    int min = Math.min(layoutWidth, layoutHeight);
                    width = min;
                    height = min;
                } else if (size.getW() > size.getH()) {
                    width = layoutWidth;
                    height = Math.round(((float) layoutWidth / size.getW()) * size.getH());
                } else {
                    height = layoutHeight;
                    width = Math.round(((float) layoutHeight / size.getH()) * size.getW());
                }
            }
        }

        int offsetLeft = (layoutWidth - width) / 2;
        int offsetTop = (layoutHeight - height) / 2;

        Log.d(TAG, "resize to video width-" + size.getW());
        Log.d(TAG, "resize to video height-" + size.getH());
        Log.d(TAG, "resize to width-" + width);
        Log.d(TAG, "resize to height-" + height);
        Log.d(TAG, "resize to layoutWidth-" + layoutWidth);
        Log.d(TAG, "resize to layoutHeight-" + layoutHeight);
        Log.d(TAG, "resize to offsetLeft-" + offsetLeft);
        Log.d(TAG, "resize to offsetTop-" + offsetTop);

        surfaceView.layout(
            offsetLeft,
            offsetTop,
            offsetLeft + width,
            offsetTop + height
        );
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        this.surfaceHolder = holder;

        if (this.videoWindowHandler != null) {
            try {
                videoWindow = this.videoWindowHandler.start(holder);
                doLayout();
            } catch (Exception e) {
                Log.e(TAG, "An error occurs during getting video window by surface", e);
            }
        }
    }

    @Override
    public void surfaceChanged(final SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // Nothing
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // Nothing
    }
}
