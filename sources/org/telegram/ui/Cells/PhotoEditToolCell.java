package org.telegram.ui.Cells;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PhotoEditorSeekBar;

public class PhotoEditToolCell extends FrameLayout {
    private Runnable hideValueRunnable = new Runnable() {
        @Override
        public void run() {
            PhotoEditToolCell.this.valueTextView.setTag(null);
            PhotoEditToolCell.this.valueAnimation = new AnimatorSet();
            PhotoEditToolCell.this.valueAnimation.playTogether(ObjectAnimator.ofFloat(PhotoEditToolCell.this.valueTextView, View.ALPHA, 0.0f), ObjectAnimator.ofFloat(PhotoEditToolCell.this.nameTextView, View.ALPHA, 1.0f));
            PhotoEditToolCell.this.valueAnimation.setDuration(250L);
            PhotoEditToolCell.this.valueAnimation.setInterpolator(new DecelerateInterpolator());
            PhotoEditToolCell.this.valueAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    if (animator.equals(PhotoEditToolCell.this.valueAnimation)) {
                        PhotoEditToolCell.this.valueAnimation = null;
                    }
                }
            });
            PhotoEditToolCell.this.valueAnimation.start();
        }
    };
    private TextView nameTextView;
    private final Theme.ResourcesProvider resourcesProvider;
    private PhotoEditorSeekBar seekBar;
    private AnimatorSet valueAnimation;
    private TextView valueTextView;

    public PhotoEditToolCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        TextView textView = new TextView(context);
        this.nameTextView = textView;
        textView.setGravity(5);
        this.nameTextView.setTextColor(-1);
        this.nameTextView.setTextSize(1, 12.0f);
        this.nameTextView.setMaxLines(1);
        this.nameTextView.setSingleLine(true);
        this.nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        addView(this.nameTextView, LayoutHelper.createFrame(80, -2.0f, 19, 0.0f, 0.0f, 0.0f, 0.0f));
        TextView textView2 = new TextView(context);
        this.valueTextView = textView2;
        textView2.setTextColor(getThemedColor("dialogFloatingButton"));
        this.valueTextView.setTextSize(1, 12.0f);
        this.valueTextView.setGravity(5);
        this.valueTextView.setSingleLine(true);
        addView(this.valueTextView, LayoutHelper.createFrame(80, -2.0f, 19, 0.0f, 0.0f, 0.0f, 0.0f));
        PhotoEditorSeekBar photoEditorSeekBar = new PhotoEditorSeekBar(context);
        this.seekBar = photoEditorSeekBar;
        addView(photoEditorSeekBar, LayoutHelper.createFrame(-1, 40.0f, 19, 96.0f, 0.0f, 24.0f, 0.0f));
    }

    public void setSeekBarDelegate(final PhotoEditorSeekBar.PhotoEditorSeekBarDelegate photoEditorSeekBarDelegate) {
        this.seekBar.setDelegate(new PhotoEditorSeekBar.PhotoEditorSeekBarDelegate() {
            @Override
            public final void onProgressChanged(int i, int i2) {
                PhotoEditToolCell.this.lambda$setSeekBarDelegate$0(photoEditorSeekBarDelegate, i, i2);
            }
        });
    }

    public void lambda$setSeekBarDelegate$0(PhotoEditorSeekBar.PhotoEditorSeekBarDelegate photoEditorSeekBarDelegate, int i, int i2) {
        photoEditorSeekBarDelegate.onProgressChanged(i, i2);
        if (i2 > 0) {
            TextView textView = this.valueTextView;
            textView.setText("+" + i2);
        } else {
            TextView textView2 = this.valueTextView;
            textView2.setText("" + i2);
        }
        if (this.valueTextView.getTag() == null) {
            AnimatorSet animatorSet = this.valueAnimation;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            this.valueTextView.setTag(1);
            AnimatorSet animatorSet2 = new AnimatorSet();
            this.valueAnimation = animatorSet2;
            animatorSet2.playTogether(ObjectAnimator.ofFloat(this.valueTextView, View.ALPHA, 1.0f), ObjectAnimator.ofFloat(this.nameTextView, View.ALPHA, 0.0f));
            this.valueAnimation.setDuration(250L);
            this.valueAnimation.setInterpolator(new DecelerateInterpolator());
            this.valueAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    AndroidUtilities.runOnUIThread(PhotoEditToolCell.this.hideValueRunnable, 1000L);
                }
            });
            this.valueAnimation.start();
            return;
        }
        AndroidUtilities.cancelRunOnUIThread(this.hideValueRunnable);
        AndroidUtilities.runOnUIThread(this.hideValueRunnable, 1000L);
    }

    @Override
    public void setTag(Object obj) {
        super.setTag(obj);
        this.seekBar.setTag(obj);
    }

    @Override
    protected void onMeasure(int i, int i2) {
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40.0f), 1073741824));
    }

    public void setIconAndTextAndValue(String str, float f, int i, int i2) {
        AnimatorSet animatorSet = this.valueAnimation;
        if (animatorSet != null) {
            animatorSet.cancel();
            this.valueAnimation = null;
        }
        AndroidUtilities.cancelRunOnUIThread(this.hideValueRunnable);
        this.valueTextView.setTag(null);
        TextView textView = this.nameTextView;
        textView.setText(str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase());
        if (f > 0.0f) {
            TextView textView2 = this.valueTextView;
            textView2.setText("+" + ((int) f));
        } else {
            TextView textView3 = this.valueTextView;
            textView3.setText("" + ((int) f));
        }
        this.valueTextView.setAlpha(0.0f);
        this.nameTextView.setAlpha(1.0f);
        this.seekBar.setMinMax(i, i2);
        this.seekBar.setProgress((int) f, false);
    }

    private int getThemedColor(String str) {
        Theme.ResourcesProvider resourcesProvider = this.resourcesProvider;
        Integer color = resourcesProvider != null ? resourcesProvider.getColor(str) : null;
        return color != null ? color.intValue() : Theme.getColor(str);
    }
}
