package io.hypertrack.sendeta.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import io.hypertrack.sendeta.R;

/**
 * Created by Aman Jain on 06/06/17.
 */

public class DashedLine extends View {
    static public int ORIENTATION_HORIZONTAL = 0;
    static public int ORIENTATION_VERTICAL = 1;
    private Paint mPaint;
    private int orientation;

    public DashedLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        int dashGap, dashLength, dashThickness;
        int color;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DashedLine, 0, 0);

        try {
            dashGap = a.getDimensionPixelSize(R.styleable.DashedLine_dashGap, 5);
            dashLength = a.getDimensionPixelSize(R.styleable.DashedLine_dashLength, 5);
            dashThickness = a.getDimensionPixelSize(R.styleable.DashedLine_dashThickness, 3);
            color = a.getColor(R.styleable.DashedLine_color, 0xff000000);
            orientation = a.getInt(R.styleable.DashedLine_orientation, ORIENTATION_HORIZONTAL);
        } finally {
            a.recycle();
        }

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(dashThickness);
        mPaint.setPathEffect(new DashPathEffect(new float[] { dashLength, dashGap, }, 0));
    }

    public DashedLine(Context context) {
        this(context, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (orientation == ORIENTATION_HORIZONTAL) {
            float center = getHeight() * .5f;
            canvas.drawLine(0, center, getWidth(), center, mPaint);
        } else {
            float center = getWidth() * .5f;
            canvas.drawLine(center, 0, center, getHeight(), mPaint);
        }
    }
}
