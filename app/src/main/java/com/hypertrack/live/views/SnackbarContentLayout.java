package com.hypertrack.live.views;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.hypertrack.live.R;

@SuppressWarnings("unused")
public class SnackbarContentLayout extends FrameLayout implements com.google.android.material.snackbar.ContentViewCallback {
    private View contentView;
    private Button actionView;

    public SnackbarContentLayout(@NonNull Context context) {
        super(context);
    }

    public SnackbarContentLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SnackbarContentLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SnackbarContentLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public View getContentView() {
        return contentView;
    }

    public Button getActionView() {
        return actionView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        contentView = findViewById(R.id.ht_snackbar_content);
        actionView = findViewById(R.id.ht_action);
    }

    @Override
    public void animateContentIn(int delay, int duration) {
        if (contentView != null) {
            contentView.setAlpha(0.0F);
            contentView.animate().alpha(1.0F).setDuration((long) duration).setStartDelay((long) delay).start();
        }
        if (actionView != null && actionView.getVisibility() == VISIBLE) {
            actionView.setAlpha(0.0F);
            actionView.animate().alpha(1.0F).setDuration((long) duration).setStartDelay((long) delay).start();
        }

    }

    @Override
    public void animateContentOut(int delay, int duration) {
        if (contentView != null) {
            contentView.setAlpha(1.0F);
            contentView.animate().alpha(0.0F).setDuration((long) duration).setStartDelay((long) delay).start();
        }
        if (actionView != null && actionView.getVisibility() == VISIBLE) {
            actionView.setAlpha(1.0F);
            actionView.animate().alpha(0.0F).setDuration((long) duration).setStartDelay((long) delay).start();
        }

    }
}