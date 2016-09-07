package io.hypertrack.sendeta.util;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by piyush on 07/09/16.
 */
public class SpeedyRecyclerView extends RecyclerView{
    public SpeedyRecyclerView(Context context) {
        super(context);
    }

    public SpeedyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SpeedyRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {

        Log.d("test", "before velocity = " + velocityY);
        velocityY *= 5;
        Log.d("test", "after velocity = " + velocityY);

        return super.fling(velocityX, velocityY);
    }
}
