package org.telegram.ui.Components.Premium;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.core.graphics.ColorUtils;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC$TL_availableReaction;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.BottomPagesView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Premium.PremiumGradient;
import org.telegram.ui.PremiumPreviewFragment;

public class PremiumFeatureBottomSheet extends BottomSheet implements NotificationCenter.NotificationCenterDelegate {
    private FrameLayout buttonContainer;
    boolean containerViewsForward;
    float containerViewsProgress;
    FrameLayout content;
    int contentHeight;
    boolean enterAnimationIsRunning;
    private PremiumButtonView premiumButtonView;
    ArrayList<PremiumPreviewFragment.PremiumFeatureData> premiumFeatures = new ArrayList<>();
    ViewPager viewPager;

    public PremiumFeatureBottomSheet(final BaseFragment baseFragment, int i) {
        super(baseFragment.getParentActivity(), false);
        setCanDismissWithSwipe(false);
        final Activity parentActivity = baseFragment.getParentActivity();
        FrameLayout frameLayout = new FrameLayout(parentActivity) {
            @Override
            protected void onMeasure(int i2, int i3) {
                PremiumFeatureBottomSheet.this.contentHeight = View.MeasureSpec.getSize(i2);
                super.onMeasure(i2, i3);
            }
        };
        PremiumPreviewFragment.fillPremiumFeaturesList(this.premiumFeatures, baseFragment.getCurrentAccount());
        int i2 = 0;
        while (true) {
            if (i2 >= this.premiumFeatures.size()) {
                i2 = 0;
                break;
            }
            if (this.premiumFeatures.get(i2).type == 0) {
                this.premiumFeatures.remove(i2);
                i2--;
            } else if (this.premiumFeatures.get(i2).type == i) {
                break;
            }
            i2++;
        }
        final PremiumPreviewFragment.PremiumFeatureData premiumFeatureData = this.premiumFeatures.get(i2);
        setApplyBottomPadding(false);
        this.useBackgroundTopPadding = false;
        final PremiumGradient.GradientTools gradientTools = new PremiumGradient.GradientTools("premiumGradientBottomSheet1", "premiumGradientBottomSheet2", "premiumGradientBottomSheet3", null);
        gradientTools.x1 = 0.0f;
        gradientTools.y1 = 1.1f;
        gradientTools.x2 = 1.5f;
        gradientTools.y2 = -0.2f;
        gradientTools.exactly = true;
        this.content = new FrameLayout(this, parentActivity) {
            @Override
            protected void onMeasure(int i3, int i4) {
                super.onMeasure(i3, View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i3) + AndroidUtilities.dp(2.0f), 1073741824));
            }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                gradientTools.gradientMatrix(0, 0, getMeasuredWidth(), getMeasuredHeight(), 0.0f, 0.0f);
                RectF rectF = AndroidUtilities.rectTmp;
                rectF.set(0.0f, AndroidUtilities.dp(2.0f), getMeasuredWidth(), getMeasuredHeight() + AndroidUtilities.dp(18.0f));
                canvas.save();
                canvas.clipRect(0, 0, getMeasuredWidth(), getMeasuredHeight());
                canvas.drawRoundRect(rectF, AndroidUtilities.dp(12.0f) - 1, AndroidUtilities.dp(12.0f) - 1, gradientTools.paint);
                canvas.restore();
                super.dispatchDraw(canvas);
            }
        };
        FrameLayout frameLayout2 = new FrameLayout(parentActivity);
        ImageView imageView = new ImageView(parentActivity);
        imageView.setImageResource(R.drawable.msg_close);
        imageView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(12.0f), ColorUtils.setAlphaComponent(-1, 40), ColorUtils.setAlphaComponent(-1, 100)));
        frameLayout2.addView(imageView, LayoutHelper.createFrame(24, 24, 17));
        frameLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                PremiumFeatureBottomSheet.this.lambda$new$0(view);
            }
        });
        frameLayout.addView(this.content, LayoutHelper.createLinear(-1, -2, 1, 0, 16, 0, 0));
        ViewPager viewPager = new ViewPager(parentActivity) {
            @Override
            public void onMeasure(int i3, int i4) {
                int dp = AndroidUtilities.dp(100.0f);
                if (getChildCount() > 0) {
                    getChildAt(0).measure(i3, View.MeasureSpec.makeMeasureSpec(0, 0));
                    dp = getChildAt(0).getMeasuredHeight();
                }
                super.onMeasure(i3, View.MeasureSpec.makeMeasureSpec(dp, 1073741824));
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
                try {
                    return super.onInterceptTouchEvent(motionEvent);
                } catch (Exception unused) {
                    return false;
                }
            }

            @Override
            public boolean onTouchEvent(MotionEvent motionEvent) {
                if (PremiumFeatureBottomSheet.this.enterAnimationIsRunning) {
                    return false;
                }
                return super.onTouchEvent(motionEvent);
            }
        };
        this.viewPager = viewPager;
        viewPager.setOffscreenPageLimit(0);
        this.viewPager.setAdapter(new PagerAdapter() {
            @Override
            public boolean isViewFromObject(View view, Object obj) {
                return view == obj;
            }

            @Override
            public int getCount() {
                return PremiumFeatureBottomSheet.this.premiumFeatures.size();
            }

            @Override
            public Object instantiateItem(ViewGroup viewGroup, int i3) {
                ViewPage viewPage = new ViewPage(parentActivity, i3);
                viewGroup.addView(viewPage);
                viewPage.position = i3;
                viewPage.setFeatureDate(PremiumFeatureBottomSheet.this.premiumFeatures.get(i3));
                return viewPage;
            }

            @Override
            public void destroyItem(ViewGroup viewGroup, int i3, Object obj) {
                viewGroup.removeView((View) obj);
            }
        });
        this.viewPager.setCurrentItem(i2);
        frameLayout.addView(this.viewPager, LayoutHelper.createFrame(-1, 100.0f, 0, 0.0f, 18.0f, 0.0f, 0.0f));
        frameLayout.addView(frameLayout2, LayoutHelper.createFrame(52, 52.0f, 53, 0.0f, 16.0f, 0.0f, 0.0f));
        final BottomPagesView bottomPagesView = new BottomPagesView(parentActivity, this.viewPager, this.premiumFeatures.size());
        this.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            float progress;
            int selectedPosition;
            int toPosition;

            @Override
            public void onPageScrollStateChanged(int i3) {
            }

            {
                new SparseArray();
                new ArrayList();
            }

            @Override
            public void onPageScrolled(int i3, float f, int i4) {
                bottomPagesView.setPageOffset(i3, f);
                this.selectedPosition = i3;
                this.toPosition = i4 > 0 ? i3 + 1 : i3 - 1;
                this.progress = f;
                checkPage();
            }

            @Override
            public void onPageSelected(int i3) {
                checkPage();
            }

            private void checkPage() {
                float measuredWidth;
                boolean z = false;
                for (int i3 = 0; i3 < PremiumFeatureBottomSheet.this.viewPager.getChildCount(); i3++) {
                    ViewPage viewPage = (ViewPage) PremiumFeatureBottomSheet.this.viewPager.getChildAt(i3);
                    float f = 0.0f;
                    if (!PremiumFeatureBottomSheet.this.enterAnimationIsRunning || !(viewPage.topView instanceof PremiumAppIconsPreviewView)) {
                        int i4 = viewPage.position;
                        if (i4 == this.selectedPosition) {
                            PagerHeaderView pagerHeaderView = viewPage.topHeader;
                            measuredWidth = (-viewPage.getMeasuredWidth()) * this.progress;
                            pagerHeaderView.setOffset(measuredWidth);
                        } else if (i4 == this.toPosition) {
                            PagerHeaderView pagerHeaderView2 = viewPage.topHeader;
                            measuredWidth = ((-viewPage.getMeasuredWidth()) * this.progress) + viewPage.getMeasuredWidth();
                            pagerHeaderView2.setOffset(measuredWidth);
                        } else {
                            viewPage.topHeader.setOffset(viewPage.getMeasuredWidth());
                        }
                        f = measuredWidth;
                    }
                    if (viewPage.topView instanceof PremiumAppIconsPreviewView) {
                        viewPage.setTranslationX(-f);
                        viewPage.title.setTranslationX(f);
                        viewPage.description.setTranslationX(f);
                    }
                }
                PremiumFeatureBottomSheet premiumFeatureBottomSheet = PremiumFeatureBottomSheet.this;
                premiumFeatureBottomSheet.containerViewsProgress = this.progress;
                if (this.toPosition > this.selectedPosition) {
                    z = true;
                }
                premiumFeatureBottomSheet.containerViewsForward = z;
            }
        });
        LinearLayout linearLayout = new LinearLayout(parentActivity);
        linearLayout.addView(frameLayout);
        linearLayout.setOrientation(1);
        bottomPagesView.setColor("chats_unreadCounterMuted", "chats_actionBackground");
        linearLayout.addView(bottomPagesView, LayoutHelper.createLinear(this.premiumFeatures.size() * 11, 5, 1, 0, 0, 0, 10));
        PremiumButtonView premiumButtonView = new PremiumButtonView(parentActivity, true);
        this.premiumButtonView = premiumButtonView;
        premiumButtonView.buttonTextView.setText(PremiumPreviewFragment.getPremiumButtonText(this.currentAccount));
        this.premiumButtonView.buttonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                PremiumFeatureBottomSheet.this.lambda$new$1(baseFragment, premiumFeatureData, view);
            }
        });
        this.premiumButtonView.overlayTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                PremiumFeatureBottomSheet.this.lambda$new$2(view);
            }
        });
        FrameLayout frameLayout3 = new FrameLayout(parentActivity);
        this.buttonContainer = frameLayout3;
        frameLayout3.addView(this.premiumButtonView, LayoutHelper.createFrame(-1, 48.0f, 16, 16.0f, 0.0f, 16.0f, 0.0f));
        this.buttonContainer.setBackgroundColor(getThemedColor("dialogBackground"));
        linearLayout.addView(this.buttonContainer, LayoutHelper.createLinear(-1, 68, 80));
        if (UserConfig.getInstance(this.currentAccount).isPremium()) {
            this.premiumButtonView.setOverlayText(LocaleController.getString("OK", R.string.OK), false);
        }
        ScrollView scrollView = new ScrollView(parentActivity);
        scrollView.addView(linearLayout);
        setCustomView(scrollView);
        MediaDataController.getInstance(this.currentAccount).preloadPremiumPreviewStickers();
    }

    public void lambda$new$0(View view) {
        dismiss();
    }

    public void lambda$new$1(BaseFragment baseFragment, PremiumPreviewFragment.PremiumFeatureData premiumFeatureData, View view) {
        if (baseFragment.getVisibleDialog() != null) {
            baseFragment.getVisibleDialog().dismiss();
        }
        if (baseFragment instanceof ChatActivity) {
            ((ChatActivity) baseFragment).closeMenu();
        }
        PremiumPreviewFragment.buyPremium(baseFragment, PremiumPreviewFragment.featureTypeToServerString(premiumFeatureData.type));
        dismiss();
    }

    public void lambda$new$2(View view) {
        dismiss();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.billingProductDetailsUpdated);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.premiumPromoUpdated);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.currentUserPremiumStatusChanged);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.billingProductDetailsUpdated);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.premiumPromoUpdated);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.currentUserPremiumStatusChanged);
    }

    @Override
    public void didReceivedNotification(int i, int i2, Object... objArr) {
        if (i == NotificationCenter.billingProductDetailsUpdated || i == NotificationCenter.premiumPromoUpdated) {
            this.premiumButtonView.buttonTextView.setText(PremiumPreviewFragment.getPremiumButtonText(this.currentAccount));
        } else if (i != NotificationCenter.currentUserPremiumStatusChanged) {
        } else {
            if (UserConfig.getInstance(this.currentAccount).isPremium()) {
                this.premiumButtonView.setOverlayText(LocaleController.getString("OK", R.string.OK), true);
            } else {
                this.premiumButtonView.clearOverlayText();
            }
        }
    }

    public class ViewPage extends LinearLayout {
        TextView description;
        public int position;
        TextView title;
        PagerHeaderView topHeader;
        View topView;

        public ViewPage(Context context, int i) {
            super(context);
            setOrientation(1);
            View viewForPosition = PremiumFeatureBottomSheet.this.getViewForPosition(context, i);
            this.topView = viewForPosition;
            addView(viewForPosition);
            this.topHeader = (PagerHeaderView) this.topView;
            TextView textView = new TextView(context);
            this.title = textView;
            textView.setGravity(1);
            this.title.setTextColor(Theme.getColor("dialogTextBlack"));
            this.title.setTextSize(1, 20.0f);
            this.title.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
            addView(this.title, LayoutHelper.createFrame(-1, -2.0f, 0, 21.0f, 20.0f, 21.0f, 0.0f));
            TextView textView2 = new TextView(context);
            this.description = textView2;
            textView2.setGravity(1);
            this.description.setTextSize(1, 15.0f);
            this.description.setTextColor(Theme.getColor("dialogTextBlack"));
            addView(this.description, LayoutHelper.createFrame(-1, -2.0f, 0, 21.0f, 10.0f, 21.0f, 16.0f));
            setClipChildren(false);
        }

        @Override
        protected void onMeasure(int i, int i2) {
            this.topView.getLayoutParams().height = PremiumFeatureBottomSheet.this.contentHeight;
            super.onMeasure(i, i2);
        }

        @Override
        protected boolean drawChild(Canvas canvas, View view, long j) {
            if (view != this.topView) {
                return super.drawChild(canvas, view, j);
            }
            if (view instanceof CarouselView) {
                return super.drawChild(canvas, view, j);
            }
            canvas.save();
            canvas.clipRect(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            boolean drawChild = super.drawChild(canvas, view, j);
            canvas.restore();
            return drawChild;
        }

        void setFeatureDate(PremiumPreviewFragment.PremiumFeatureData premiumFeatureData) {
            this.title.setText(premiumFeatureData.title);
            this.description.setText(premiumFeatureData.description);
        }
    }

    View getViewForPosition(Context context, int i) {
        PremiumPreviewFragment.PremiumFeatureData premiumFeatureData = this.premiumFeatures.get(i);
        int i2 = premiumFeatureData.type;
        if (i2 == 4) {
            ArrayList arrayList = new ArrayList();
            List<TLRPC$TL_availableReaction> enabledReactionsList = MediaDataController.getInstance(this.currentAccount).getEnabledReactionsList();
            ArrayList arrayList2 = new ArrayList();
            for (int i3 = 0; i3 < enabledReactionsList.size(); i3++) {
                if (enabledReactionsList.get(i3).premium) {
                    arrayList2.add(enabledReactionsList.get(i3));
                }
            }
            for (int i4 = 0; i4 < arrayList2.size(); i4++) {
                UnlockPremiumReactionsWindow$ReactionDrawingObject unlockPremiumReactionsWindow$ReactionDrawingObject = new UnlockPremiumReactionsWindow$ReactionDrawingObject(i4);
                unlockPremiumReactionsWindow$ReactionDrawingObject.set((TLRPC$TL_availableReaction) arrayList2.get(i4));
                arrayList.add(unlockPremiumReactionsWindow$ReactionDrawingObject);
            }
            final HashMap hashMap = new HashMap();
            hashMap.put("👌", 1);
            hashMap.put("😍", 2);
            hashMap.put("🤡", 3);
            hashMap.put("🕊", 4);
            hashMap.put("\u1f971", 5);
            hashMap.put("\u1f974", 6);
            hashMap.put("🐳", 7);
            Collections.sort(arrayList, new Comparator() {
                @Override
                public final int compare(Object obj, Object obj2) {
                    int lambda$getViewForPosition$3;
                    lambda$getViewForPosition$3 = PremiumFeatureBottomSheet.lambda$getViewForPosition$3(hashMap, (UnlockPremiumReactionsWindow$ReactionDrawingObject) obj, (UnlockPremiumReactionsWindow$ReactionDrawingObject) obj2);
                    return lambda$getViewForPosition$3;
                }
            });
            return new CarouselView(context, arrayList);
        } else if (i2 == 5) {
            return new PremiumStickersPreviewRecycler(this, context, this.currentAccount) {
                @Override
                public void setOffset(float f) {
                    setAutoPlayEnabled(f == 0.0f);
                }
            };
        } else {
            if (i2 == 10) {
                return new PremiumAppIconsPreviewView(context);
            }
            VideoScreenPreview videoScreenPreview = new VideoScreenPreview(context, this.currentAccount, premiumFeatureData.type);
            if (premiumFeatureData.type == 1) {
                videoScreenPreview.fromTop = true;
            }
            return videoScreenPreview;
        }
    }

    public static int lambda$getViewForPosition$3(HashMap hashMap, UnlockPremiumReactionsWindow$ReactionDrawingObject unlockPremiumReactionsWindow$ReactionDrawingObject, UnlockPremiumReactionsWindow$ReactionDrawingObject unlockPremiumReactionsWindow$ReactionDrawingObject2) {
        boolean containsKey = hashMap.containsKey(unlockPremiumReactionsWindow$ReactionDrawingObject.reaction.reaction);
        int i = ConnectionsManager.DEFAULT_DATACENTER_ID;
        int intValue = containsKey ? ((Integer) hashMap.get(unlockPremiumReactionsWindow$ReactionDrawingObject.reaction.reaction)).intValue() : ConnectionsManager.DEFAULT_DATACENTER_ID;
        if (hashMap.containsKey(unlockPremiumReactionsWindow$ReactionDrawingObject2.reaction.reaction)) {
            i = ((Integer) hashMap.get(unlockPremiumReactionsWindow$ReactionDrawingObject2.reaction.reaction)).intValue();
        }
        return i - intValue;
    }

    @Override
    public boolean onCustomOpenAnimation() {
        if (this.viewPager.getChildCount() > 0) {
            ViewPage viewPage = (ViewPage) this.viewPager.getChildAt(0);
            View view = viewPage.topView;
            if (view instanceof PremiumAppIconsPreviewView) {
                final PremiumAppIconsPreviewView premiumAppIconsPreviewView = (PremiumAppIconsPreviewView) view;
                ValueAnimator ofFloat = ValueAnimator.ofFloat(viewPage.getMeasuredWidth(), 0.0f);
                premiumAppIconsPreviewView.setOffset(viewPage.getMeasuredWidth());
                this.enterAnimationIsRunning = true;
                ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        premiumAppIconsPreviewView.setOffset(((Float) valueAnimator.getAnimatedValue()).floatValue());
                    }
                });
                ofFloat.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        PremiumFeatureBottomSheet.this.enterAnimationIsRunning = false;
                        premiumAppIconsPreviewView.setOffset(0.0f);
                        super.onAnimationEnd(animator);
                    }
                });
                ofFloat.setDuration(300L);
                ofFloat.setStartDelay(100L);
                ofFloat.setInterpolator(CubicBezierInterpolator.EASE_OUT);
                ofFloat.start();
            }
        }
        return super.onCustomOpenAnimation();
    }
}