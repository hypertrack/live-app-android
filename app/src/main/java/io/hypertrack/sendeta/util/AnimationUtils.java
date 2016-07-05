package io.hypertrack.sendeta.util;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by piyush on 22/06/16.
 */
public class AnimationUtils {

    // Duration Constants (in millisecond)
    private static final int DURATION_DEFAULT = 200;
    private static final int DURATION_DEFAULT_VALUE_ANIMATION = 300;

    public static void expand(final View v) {
        expand(v, DURATION_DEFAULT);
    }

    public static void expand(final View v, int duration) {

        //check if view already visible
        expand(v, duration, true);

    }

    public static Animation expand(final View v, int duration, boolean startAnim) {
        if (v.getVisibility() == View.VISIBLE)
            return null;

        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }

        };

        a.setDuration(duration);
        if (startAnim)
            v.startAnimation(a);
        return a;
    }


    public static void collapse(final View v) {
        collapse(v, DURATION_DEFAULT);
    }

    public static void collapse(final View v, int duration) {

        //check if view already gone
        if (v.getVisibility() == View.GONE)
            return;

        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(duration);
        v.startAnimation(a);
    }

    public static Animation collapse(final View v, int duration, boolean startAnim) {

        //check if view already gone
        if (v.getVisibility() == View.GONE)
            return null;

        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(duration);
        if (startAnim)
            v.startAnimation(a);
        return a;
    }
}
