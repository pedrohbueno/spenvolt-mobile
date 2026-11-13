package com.spenvolt.mobile.ui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.spenvolt.mobile.R;

public class BarView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private float progress = 0f;
    private float marker = -1f;
    private int startColor;
    private int endColor;

    public BarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        trackPaint.setColor(ContextCompat.getColor(context, R.color.gray_200));
        markerPaint.setColor(ContextCompat.getColor(context, R.color.dark_blue));
        startColor = ContextCompat.getColor(context, R.color.green_start);
        endColor = ContextCompat.getColor(context, R.color.green_end);
    }

    public void setProgress(float fraction, int colorRes) {
        setProgress(fraction, colorRes, colorRes);
    }

    public void setProgress(float fraction, int startColorRes, int endColorRes) {
        progress = Math.max(0f, Math.min(fraction, 1f));
        startColor = ContextCompat.getColor(getContext(), startColorRes);
        endColor = ContextCompat.getColor(getContext(), endColorRes);
        invalidate();
    }

    public void setMarker(float fraction) {
        marker = fraction < 0 ? -1f : Math.min(fraction, 1f);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float radius = height / 2f;

        rect.set(0, 0, width, height);
        canvas.drawRoundRect(rect, radius, radius, trackPaint);

        float fillWidth = width * progress;
        if (fillWidth > 0) {
            fillPaint.setShader(new LinearGradient(0, 0, fillWidth, 0, startColor, endColor, Shader.TileMode.CLAMP));
            rect.set(0, 0, Math.max(fillWidth, height), height);
            canvas.drawRoundRect(rect, radius, radius, fillPaint);
        }

        if (marker >= 0) {
            float x = width * marker;
            float markerWidth = getResources().getDisplayMetrics().density * 2;
            canvas.drawRect(x - markerWidth / 2, 0, x + markerWidth / 2, height, markerPaint);
        }
    }
}
