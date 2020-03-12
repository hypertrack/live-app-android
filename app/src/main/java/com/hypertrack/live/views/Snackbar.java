package com.hypertrack.live.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.BaseTransientBottomBar;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public final class Snackbar extends BaseTransientBottomBar<Snackbar> {
    private final AccessibilityManager accessibilityManager;
    private boolean hasAction;
    public static final int LENGTH_INDEFINITE = -2;
    public static final int LENGTH_SHORT = -1;
    public static final int LENGTH_LONG = 0;

    @Nullable
    private BaseCallback<Snackbar> callback;

    private Snackbar(ViewGroup parent, View content, com.google.android.material.snackbar.ContentViewCallback contentViewCallback) {
        super(parent, content, contentViewCallback);
        accessibilityManager = (AccessibilityManager) parent.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        getView().setBackground(null);
    }

    public void show() {
        super.show();
    }

    public void dismiss() {
        super.dismiss();
    }

    public boolean isShown() {
        return super.isShown();
    }

    @NonNull
    public static Snackbar make(@NonNull View view, int layoutResourceId, int duration) {
        ViewGroup parent = findSuitableParent(view);
        if (parent == null) {
            throw new IllegalArgumentException("No suitable parent found from the given view. Please provide a valid view.");
        } else {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            SnackbarContentLayout content =
                    (SnackbarContentLayout) inflater.inflate(layoutResourceId, parent, false);
            Snackbar snackbar = new Snackbar(parent, content, content);
            snackbar.setDuration(duration);
            return snackbar;
        }
    }

    @NonNull
    public Snackbar setAction(final View.OnClickListener listener) {
        SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
        View view = contentLayout.getActionView();
        if (view != null) {
            if (listener != null) {
                hasAction = true;
                view.setVisibility(View.VISIBLE);
                view.setOnClickListener(listener);
            } else {
                view.setVisibility(View.GONE);
                view.setOnClickListener(null);
                hasAction = false;
            }
        }

        return this;
    }

    @NonNull
    public Snackbar setAction(int viewId, final View.OnClickListener listener) {
        SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
        View view = contentLayout.findViewById(viewId);
        if (view != null) {
            if (listener != null) {
                view.setVisibility(View.VISIBLE);
                view.setOnClickListener(listener);
            } else {
                view.setVisibility(View.GONE);
                view.setOnClickListener(null);
            }
        }

        return this;
    }

    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;

        do {
            if (view instanceof CoordinatorLayout) {
                return (ViewGroup) view;
            }

            if (view instanceof FrameLayout) {
                fallback = (ViewGroup) view;
            }

            if (view != null) {
                ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        return fallback;
    }

    public static class Callback extends BaseCallback<Snackbar> {
        public static final int DISMISS_EVENT_SWIPE = 0;
        public static final int DISMISS_EVENT_ACTION = 1;
        public static final int DISMISS_EVENT_TIMEOUT = 2;
        public static final int DISMISS_EVENT_MANUAL = 3;
        public static final int DISMISS_EVENT_CONSECUTIVE = 4;

        public Callback() {
        }

        public void onShown(Snackbar sb) {
        }

        public void onDismissed(Snackbar transientBottomBar, int event) {
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({Scope.LIBRARY_GROUP})
    @IntRange(
            from = 1L
    )
    public @interface Duration {
    }

}