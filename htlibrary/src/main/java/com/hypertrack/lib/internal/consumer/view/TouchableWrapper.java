package com.hypertrack.lib.internal.consumer.view;

/**
 * Created by ulhas on 24/06/16.
 */

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/** package */ class TouchableWrapper extends FrameLayout {

    private TouchActionDown mTouchActionDown;
    private TouchActionUp mTouchActionUp;

    public TouchableWrapper(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchActionDown.onTouchDown(event);
                break;
            case MotionEvent.ACTION_UP:
                mTouchActionUp.onTouchUp(event);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    public void setCallback(HyperTrackMapFragment fragment) {
        mTouchActionUp = fragment;
        mTouchActionDown = fragment;
    }

    public interface TouchActionDown {
        void onTouchDown(MotionEvent event);
    }

    public interface TouchActionUp {
        void onTouchUp(MotionEvent event);
    }
}
