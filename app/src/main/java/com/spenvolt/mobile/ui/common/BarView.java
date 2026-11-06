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

/**
 * Barra de progresso arredondada, usada em vários lugares:
 * consumo da residência, comparativo entre casas e consumo por membro.
 *
 * Uso:
 *   bar.setProgress(0.75f, R.color.green_start, R.color.green_end); // 75%, com gradiente
 *   bar.setMarker(0.9f); // tracinho escuro marcando o limite em 90% da largura
 */
public class BarView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private float progress = 0f;   // 0 a 1
    private float marker = -1f;    // 0 a 1; -1 = sem marcador
    private int startColor;
    private int endColor;

    public BarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        trackPaint.setColor(ContextCompat.getColor(context, R.color.gray_200));
        markerPaint.setColor(ContextCompat.getColor(context, R.color.dark_blue));
        startColor = ContextCompat.getColor(context, R.color.green_start);
        endColor = ContextCompat.getColor(context, R.color.green_end);
    }

    /** Preenche uma fração da barra (0 a 1) com uma cor só. */
    public void setProgress(float fraction, int colorRes) {
        setProgress(fraction, colorRes, colorRes);
    }

    /** Preenche uma fração da barra (0 a 1) com um gradiente da esquerda para a direita. */
    public void setProgress(float fraction, int startColorRes, int endColorRes) {
        progress = Math.max(0f, Math.min(fraction, 1f));
        startColor = ContextCompat.getColor(getContext(), startColorRes);
        endColor = ContextCompat.getColor(getContext(), endColorRes);
        invalidate();
    }

    /** Mostra um tracinho na posição (0 a 1); use -1 para esconder. */
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

        // Trilho cinza
        rect.set(0, 0, width, height);
        canvas.drawRoundRect(rect, radius, radius, trackPaint);

        // Parte preenchida
        float fillWidth = width * progress;
        if (fillWidth > 0) {
            fillPaint.setShader(new LinearGradient(0, 0, fillWidth, 0, startColor, endColor, Shader.TileMode.CLAMP));
            rect.set(0, 0, Math.max(fillWidth, height), height);
            canvas.drawRoundRect(rect, radius, radius, fillPaint);
        }

        // Marcador do limite
        if (marker >= 0) {
            float x = width * marker;
            float markerWidth = getResources().getDisplayMetrics().density * 2;
            canvas.drawRect(x - markerWidth / 2, 0, x + markerWidth / 2, height, markerPaint);
        }
    }
}
