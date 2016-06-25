package io.hypertrack.meta.view;

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
        // Force the host activity to implement the TouchActionDown Interface
        try {
            mTouchActionDown = (TouchActionDown) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement TouchActionDown");
        }
        // Force the host activity to implement the TouchActionDown Interface
        try {
            mTouchActionUp = (TouchActionUp) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement mTouchActionUp");
        }
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

    public interface TouchActionDown {
        void onTouchDown(MotionEvent event);
    }

    public interface TouchActionUp {
        void onTouchUp(MotionEvent event);
    }
}
