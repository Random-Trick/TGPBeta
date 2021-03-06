package org.telegram.ui.Components.voip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import androidx.core.graphics.ColorUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.voip.VoIPService;

public class VoIPTimerView extends View {
    String currentTimeStr;
    StaticLayout timerLayout;
    RectF rectF = new RectF();
    Paint activePaint = new Paint(1);
    Paint inactivePaint = new Paint(1);
    TextPaint textPaint = new TextPaint(1);
    private int signalBarCount = 4;
    Runnable updater = new Runnable() {
        @Override
        public final void run() {
            VoIPTimerView.this.lambda$new$0();
        }
    };

    public void lambda$new$0() {
        if (getVisibility() == 0) {
            updateTimer();
        }
    }

    public VoIPTimerView(Context context) {
        super(context);
        this.textPaint.setTextSize(AndroidUtilities.dp(15.0f));
        this.textPaint.setColor(-1);
        this.textPaint.setShadowLayer(AndroidUtilities.dp(3.0f), 0.0f, AndroidUtilities.dp(0.6666667f), 1275068416);
        this.activePaint.setColor(ColorUtils.setAlphaComponent(-1, 229));
        this.inactivePaint.setColor(ColorUtils.setAlphaComponent(-1, 102));
    }

    @Override
    protected void onMeasure(int i, int i2) {
        StaticLayout staticLayout = this.timerLayout;
        if (staticLayout != null) {
            setMeasuredDimension(View.MeasureSpec.getSize(i), staticLayout.getHeight());
        } else {
            setMeasuredDimension(View.MeasureSpec.getSize(i), AndroidUtilities.dp(15.0f));
        }
    }

    public void updateTimer() {
        removeCallbacks(this.updater);
        VoIPService sharedInstance = VoIPService.getSharedInstance();
        if (sharedInstance != null) {
            String formatLongDuration = AndroidUtilities.formatLongDuration((int) (sharedInstance.getCallDuration() / 1000));
            String str = this.currentTimeStr;
            if (str == null || !str.equals(formatLongDuration)) {
                this.currentTimeStr = formatLongDuration;
                if (this.timerLayout == null) {
                    requestLayout();
                }
                String str2 = this.currentTimeStr;
                TextPaint textPaint = this.textPaint;
                this.timerLayout = new StaticLayout(str2, textPaint, (int) textPaint.measureText(str2), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            }
            postDelayed(this.updater, 300L);
            invalidate();
        }
    }

    @Override
    public void setVisibility(int i) {
        if (getVisibility() != i) {
            if (i == 0) {
                this.currentTimeStr = "00:00";
                String str = this.currentTimeStr;
                TextPaint textPaint = this.textPaint;
                this.timerLayout = new StaticLayout(str, textPaint, (int) textPaint.measureText(str), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                updateTimer();
            } else {
                this.currentTimeStr = null;
                this.timerLayout = null;
            }
        }
        super.setVisibility(i);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        StaticLayout staticLayout = this.timerLayout;
        int i = 0;
        int width = staticLayout == null ? 0 : staticLayout.getWidth() + AndroidUtilities.dp(21.0f);
        canvas.save();
        canvas.translate((getMeasuredWidth() - width) / 2.0f, 0.0f);
        canvas.save();
        canvas.translate(0.0f, (getMeasuredHeight() - AndroidUtilities.dp(11.0f)) / 2.0f);
        while (i < 4) {
            int i2 = i + 1;
            Paint paint = i2 > this.signalBarCount ? this.inactivePaint : this.activePaint;
            float f = i;
            this.rectF.set(AndroidUtilities.dpf2(4.16f) * f, AndroidUtilities.dpf2(2.75f) * (3 - i), (AndroidUtilities.dpf2(4.16f) * f) + AndroidUtilities.dpf2(2.75f), AndroidUtilities.dp(11.0f));
            canvas.drawRoundRect(this.rectF, AndroidUtilities.dpf2(0.7f), AndroidUtilities.dpf2(0.7f), paint);
            i = i2;
        }
        canvas.restore();
        if (staticLayout != null) {
            canvas.translate(AndroidUtilities.dp(21.0f), 0.0f);
            staticLayout.draw(canvas);
        }
        canvas.restore();
    }

    public void setSignalBarCount(int i) {
        this.signalBarCount = i;
        invalidate();
    }
}
