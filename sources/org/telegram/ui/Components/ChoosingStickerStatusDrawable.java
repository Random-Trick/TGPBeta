package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class ChoosingStickerStatusDrawable extends StatusDrawable {
    int color;
    Paint fillPaint;
    float progress;
    Paint strokePaint;
    private long lastUpdateTime = 0;
    private boolean started = false;
    boolean increment = true;

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public void setAlpha(int i) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public void setIsChat(boolean z) {
    }

    public ChoosingStickerStatusDrawable(boolean z) {
        if (z) {
            this.strokePaint = new Paint(1);
            this.fillPaint = new Paint(1);
            this.strokePaint.setStyle(Paint.Style.STROKE);
            this.strokePaint.setStrokeWidth(AndroidUtilities.dpf2(1.2f));
        }
    }

    @Override
    public void start() {
        this.lastUpdateTime = System.currentTimeMillis();
        this.started = true;
        invalidateSelf();
    }

    @Override
    public void stop() {
        this.started = false;
    }

    @Override
    public void setColor(int i) {
        if (this.color != i) {
            this.fillPaint.setColor(i);
            this.strokePaint.setColor(i);
        }
        this.color = i;
    }

    @Override
    public void draw(Canvas canvas) {
        float f;
        float f2;
        float min = Math.min(this.progress, 1.0f);
        float interpolation = CubicBezierInterpolator.EASE_IN.getInterpolation(min < 0.3f ? min / 0.3f : 1.0f);
        CubicBezierInterpolator cubicBezierInterpolator = CubicBezierInterpolator.EASE_OUT;
        float interpolation2 = cubicBezierInterpolator.getInterpolation(min < 0.3f ? 0.0f : (min - 0.3f) / 0.7f);
        float f3 = 2.0f;
        if (this.increment) {
            f2 = (AndroidUtilities.dp(2.1f) * interpolation) + ((AndroidUtilities.dp(7.0f) - AndroidUtilities.dp(2.1f)) * (1.0f - interpolation));
            f = AndroidUtilities.dpf2(1.5f) * (1.0f - cubicBezierInterpolator.getInterpolation(this.progress / 2.0f));
        } else {
            f2 = (AndroidUtilities.dp(2.1f) * (1.0f - interpolation)) + ((AndroidUtilities.dp(7.0f) - AndroidUtilities.dp(2.1f)) * interpolation);
            f = AndroidUtilities.dpf2(1.5f) * CubicBezierInterpolator.EASE_OUT_QUINT.getInterpolation(this.progress / 2.0f);
        }
        float dp = AndroidUtilities.dp(11.0f) / 2.0f;
        float dpf2 = AndroidUtilities.dpf2(2.0f);
        float dpf22 = (AndroidUtilities.dpf2(0.5f) * interpolation) - (AndroidUtilities.dpf2(0.5f) * interpolation2);
        Paint paint = this.strokePaint;
        if (paint == null) {
            paint = Theme.chat_statusRecordPaint;
        }
        Paint paint2 = this.fillPaint;
        if (paint2 == null) {
            paint2 = Theme.chat_statusPaint;
        }
        if (paint.getStrokeWidth() != AndroidUtilities.dp(0.8f)) {
            paint.setStrokeWidth(AndroidUtilities.dp(0.8f));
        }
        int i = 0;
        while (i < 2) {
            canvas.save();
            canvas.translate((paint.getStrokeWidth() / f3) + f + (AndroidUtilities.dp(9.0f) * i) + getBounds().left + AndroidUtilities.dpf2(0.2f), (paint.getStrokeWidth() / f3) + AndroidUtilities.dpf2(f3) + getBounds().top);
            RectF rectF = AndroidUtilities.rectTmp;
            rectF.set(0.0f, dpf22, AndroidUtilities.dp(7.0f), AndroidUtilities.dp(11.0f) - dpf22);
            canvas.drawOval(rectF, paint);
            canvas.drawCircle(f2, dp, dpf2, paint2);
            canvas.restore();
            i++;
            f3 = 2.0f;
        }
        if (this.started) {
            update();
        }
    }

    private void update() {
        long currentTimeMillis = System.currentTimeMillis();
        long j = currentTimeMillis - this.lastUpdateTime;
        this.lastUpdateTime = currentTimeMillis;
        if (j > 16) {
            j = 16;
        }
        float f = this.progress + (((float) j) / 500.0f);
        this.progress = f;
        if (f >= 2.0f) {
            this.progress = 0.0f;
            this.increment = !this.increment;
        }
        invalidateSelf();
    }

    @Override
    public int getIntrinsicWidth() {
        return AndroidUtilities.dp(20.0f);
    }

    @Override
    public int getIntrinsicHeight() {
        return AndroidUtilities.dp(18.0f);
    }
}
