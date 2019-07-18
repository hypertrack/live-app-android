package com.hypertrack.live.ui;

import android.app.Dialog;
import android.content.Context;

import com.airbnb.lottie.LottieAnimationView;
import com.hypertrack.live.R;

public class LoaderDecorator extends Dialog {

    public LoaderDecorator(Context context) {
        super(context, R.style.LoaderDialog);
        setContentView(R.layout.dialog_loader);
    }

    public void start() {
        show();
        ((LottieAnimationView) findViewById(R.id.loader)).playAnimation();
    }

    public void stop() {
        ((LottieAnimationView) findViewById(R.id.loader)).cancelAnimation();
        dismiss();
    }
}
