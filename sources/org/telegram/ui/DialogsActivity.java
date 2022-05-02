package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Property;
import android.util.StateSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScrollerCustom;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Iterator;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.FilesMigrationService;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.XiaomiUtilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$Dialog;
import org.telegram.tgnet.TLRPC$EncryptedChat;
import org.telegram.tgnet.TLRPC$TL_dialogFolder;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_messages_checkHistoryImportPeer;
import org.telegram.tgnet.TLRPC$TL_messages_checkedHistoryImportPeer;
import org.telegram.tgnet.TLRPC$TL_messages_updateDialogFilter;
import org.telegram.tgnet.TLRPC$TL_userEmpty;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.DialogsAdapter;
import org.telegram.ui.Adapters.DialogsSearchAdapter;
import org.telegram.ui.Adapters.FiltersView;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.HintDialogCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AnimationProperties;
import org.telegram.ui.Components.BlurredRecyclerView;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.ChatAvatarContainer;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.DialogsItemAnimator;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.FilterTabsView;
import org.telegram.ui.Components.FiltersListBottomSheet;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Components.FragmentContextView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberTextView;
import org.telegram.ui.Components.PacmanAnimation;
import org.telegram.ui.Components.ProxyDrawable;
import org.telegram.ui.Components.PullForegroundDrawable;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.RadialProgress2;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerAnimationScrollHelper;
import org.telegram.ui.Components.RecyclerItemsEnterAnimator;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SearchViewPager;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.Components.ViewPagerFixed;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.GroupCreateFinalActivity;

public class DialogsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    public static boolean[] dialogsLoaded = new boolean[3];
    private static final Interpolator interpolator = DialogsActivity$$ExternalSyntheticLambda25.INSTANCE;
    public static float viewOffset = 0.0f;
    private ValueAnimator actionBarColorAnimator;
    private ActionBarMenuSubItem addToFolderItem;
    private String addToGroupAlertString;
    private float additionalFloatingTranslation;
    private float additionalFloatingTranslation2;
    private float additionalOffset;
    private boolean afterSignup;
    private boolean allowMoving;
    private boolean allowSwipeDuringCurrentTouch;
    private boolean allowSwitchAccount;
    private boolean animatingForward;
    private ActionBarMenuItem archive2Item;
    private ActionBarMenuSubItem archiveItem;
    private boolean askingForPermissions;
    private ChatAvatarContainer avatarContainer;
    private boolean backAnimation;
    private BackDrawable backDrawable;
    private ActionBarMenuSubItem blockItem;
    private View blurredView;
    private int canClearCacheCount;
    private boolean canDeletePsaSelected;
    private int canMuteCount;
    private int canPinCount;
    private int canReadCount;
    private int canReportSpamCount;
    private boolean canShowFilterTabsView;
    private boolean canShowHiddenArchive;
    private int canUnarchiveCount;
    private int canUnmuteCount;
    private boolean cantSendToChannels;
    private boolean checkCanWrite;
    private boolean checkingImportDialog;
    private ActionBarMenuSubItem clearItem;
    private boolean closeFragment;
    private boolean closeSearchFieldOnHide;
    private ChatActivityEnterView commentView;
    private AnimatorSet commentViewAnimator;
    private View commentViewBg;
    private ValueAnimator contactsAlphaAnimator;
    private int currentConnectionState;
    View databaseMigrationHint;
    private DialogsActivityDelegate delegate;
    private ActionBarMenuItem deleteItem;
    private int dialogChangeFinished;
    private int dialogInsertFinished;
    private int dialogRemoveFinished;
    private boolean dialogsListFrozen;
    private boolean disableActionBarScrolling;
    private ActionBarMenuItem doneItem;
    private AnimatorSet doneItemAnimator;
    private ActionBarMenuItem downloadsItem;
    private boolean downloadsItemVisible;
    private float filterTabsMoveFrom;
    private float filterTabsProgress;
    private FilterTabsView filterTabsView;
    private boolean filterTabsViewIsVisible;
    private ValueAnimator filtersTabAnimator;
    private FiltersView filtersView;
    private RLottieImageView floatingButton;
    private FrameLayout floatingButtonContainer;
    private float floatingButtonHideProgress;
    private float floatingButtonTranslation;
    private boolean floatingForceVisible;
    private boolean floatingHidden;
    private AnimatorSet floatingProgressAnimator;
    private RadialProgressView floatingProgressView;
    private boolean floatingProgressVisible;
    private int folderId;
    private FragmentContextView fragmentContextView;
    private FragmentContextView fragmentLocationContextView;
    private ArrayList<TLRPC$Dialog> frozenDialogsList;
    private boolean hasInvoice;
    private int hasPoll;
    private int initialDialogsType;
    private String initialSearchString;
    boolean isDrawerTransition;
    boolean isSlideBackTransition;
    private int lastMeasuredTopPadding;
    private int maximumVelocity;
    private boolean maybeStartTracking;
    private MenuDrawable menuDrawable;
    private int messagesCount;
    private DialogCell movingView;
    private boolean movingWas;
    private ActionBarMenuItem muteItem;
    private boolean onlySelect;
    private long openedDialogId;
    private PacmanAnimation pacmanAnimation;
    private RLottieDrawable passcodeDrawable;
    private ActionBarMenuItem passcodeItem;
    private boolean passcodeItemVisible;
    private AlertDialog permissionDialog;
    private ActionBarMenuSubItem pin2Item;
    private ActionBarMenuItem pinItem;
    private int prevPosition;
    private int prevTop;
    private float progressToActionMode;
    private ProxyDrawable proxyDrawable;
    private ActionBarMenuItem proxyItem;
    private boolean proxyItemVisible;
    private ActionBarMenuSubItem readItem;
    private ActionBarMenuSubItem removeFromFolderItem;
    private AnimatorSet scrimAnimatorSet;
    private Paint scrimPaint;
    private ActionBarPopupWindow scrimPopupWindow;
    private ActionBarMenuSubItem[] scrimPopupWindowItems;
    private View scrimView;
    private boolean scrimViewSelected;
    private float scrollAdditionalOffset;
    private boolean scrollUpdated;
    private boolean scrollingManually;
    private float searchAnimationProgress;
    private boolean searchAnimationTabsDelayedCrossfade;
    private AnimatorSet searchAnimator;
    private long searchDialogId;
    private boolean searchFiltersWasShowed;
    private boolean searchIsShowed;
    private ActionBarMenuItem searchItem;
    private TLObject searchObject;
    private String searchString;
    private ViewPagerFixed.TabsView searchTabsView;
    private SearchViewPager searchViewPager;
    private boolean searchWas;
    private boolean searchWasFullyShowed;
    private boolean searching;
    private String selectAlertString;
    private String selectAlertStringGroup;
    private View selectedCountView;
    private NumberTextView selectedDialogsCountTextView;
    private ActionBarPopupWindow sendPopupWindow;
    private boolean showSetPasswordConfirm;
    private String showingSuggestion;
    private RecyclerView sideMenu;
    ValueAnimator slideBackTransitionAnimator;
    private DialogCell slidingView;
    private long startArchivePullingTime;
    private boolean startedTracking;
    private ActionBarMenuItem switchItem;
    private Animator tabsAlphaAnimator;
    private AnimatorSet tabsAnimation;
    private boolean tabsAnimationInProgress;
    private float tabsYOffset;
    private int topPadding;
    private FrameLayout updateLayout;
    private AnimatorSet updateLayoutAnimator;
    private RadialProgress2 updateLayoutIcon;
    private boolean updatePullAfterScroll;
    private TextView updateTextView;
    private ViewPage[] viewPages;
    private boolean waitingForScrollFinished;
    private boolean whiteActionBar;
    private ImageView[] writeButton;
    private FrameLayout writeButtonContainer;
    private int initialSearchType = -1;
    private float contactsAlpha = 1.0f;
    private UndoView[] undoView = new UndoView[2];
    private int[] scrimViewLocation = new int[2];
    private ArrayList<MessagesController.DialogFilter> movingDialogFilters = new ArrayList<>();
    private Paint actionBarDefaultPaint = new Paint();
    private ArrayList<View> actionModeViews = new ArrayList<>();
    private RectF rect = new RectF();
    private Paint paint = new Paint(1);
    private TextPaint textPaint = new TextPaint(1);
    private boolean askAboutContacts = true;
    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();
    private boolean checkPermission = true;
    private boolean resetDelegate = true;
    private ArrayList<Long> selectedDialogs = new ArrayList<>();
    public boolean notify = true;
    private int animationIndex = -1;
    private int debugLastUpdateAction = -1;
    public final Property<DialogsActivity, Float> SCROLL_Y = new AnimationProperties.FloatProperty<DialogsActivity>("animationValue") {
        public void setValue(DialogsActivity dialogsActivity, float f) {
            dialogsActivity.setScrollY(f);
        }

        public Float get(DialogsActivity dialogsActivity) {
            return Float.valueOf(((BaseFragment) DialogsActivity.this).actionBar.getTranslationY());
        }
    };
    private boolean scrollBarVisible = true;
    private boolean isNextButton = false;
    float slideFragmentProgress = 1.0f;

    public interface DialogsActivityDelegate {
        void didSelectDialogs(DialogsActivity dialogsActivity, ArrayList<Long> arrayList, CharSequence charSequence, boolean z);
    }

    public static boolean lambda$createActionMode$14(View view, MotionEvent motionEvent) {
        return true;
    }

    public static float lambda$static$0(float f) {
        float f2 = f - 1.0f;
        return (f2 * f2 * f2 * f2 * f2) + 1.0f;
    }

    public void updateCommentView() {
    }

    public boolean shouldShowNextButton(DialogsActivity dialogsActivity, ArrayList<Long> arrayList, CharSequence charSequence, boolean z) {
        return false;
    }

    public static class ViewPage extends FrameLayout {
        private int archivePullViewState;
        private DialogsAdapter dialogsAdapter;
        private DialogsItemAnimator dialogsItemAnimator;
        private int dialogsType;
        private ItemTouchHelper itemTouchhelper;
        private int lastItemsCount;
        private LinearLayoutManager layoutManager;
        private DialogsRecyclerView listView;
        private FlickerLoadingView progressView;
        private PullForegroundDrawable pullForegroundDrawable;
        private RecyclerItemsEnterAnimator recyclerItemsEnterAnimator;
        private RecyclerAnimationScrollHelper scrollHelper;
        private int selectedType;
        private SwipeController swipeController;

        static int access$10808(ViewPage viewPage) {
            int i = viewPage.lastItemsCount;
            viewPage.lastItemsCount = i + 1;
            return i;
        }

        static int access$10810(ViewPage viewPage) {
            int i = viewPage.lastItemsCount;
            viewPage.lastItemsCount = i - 1;
            return i;
        }

        public ViewPage(Context context) {
            super(context);
        }

        public boolean isDefaultDialogType() {
            int i = this.dialogsType;
            return i == 0 || i == 7 || i == 8;
        }
    }

    public class ContentView extends SizeNotifierFrameLayout {
        private int inputFieldHeight;
        private int startedTrackingPointerId;
        private int startedTrackingX;
        private int startedTrackingY;
        private VelocityTracker velocityTracker;
        private Paint actionBarSearchPaint = new Paint(1);
        private Paint windowBackgroundPaint = new Paint();
        private int[] pos = new int[2];

        @Override
        public boolean hasOverlappingRendering() {
            return false;
        }

        public ContentView(Context context) {
            super(context);
            this.needBlur = true;
            this.blurBehindViews.add(this);
        }

        private boolean prepareForMoving(MotionEvent motionEvent, boolean z) {
            int nextPageId = DialogsActivity.this.filterTabsView.getNextPageId(z);
            if (nextPageId < 0) {
                return false;
            }
            getParent().requestDisallowInterceptTouchEvent(true);
            DialogsActivity.this.maybeStartTracking = false;
            DialogsActivity.this.startedTracking = true;
            this.startedTrackingX = (int) (motionEvent.getX() + DialogsActivity.this.additionalOffset);
            ((BaseFragment) DialogsActivity.this).actionBar.setEnabled(false);
            DialogsActivity.this.filterTabsView.setEnabled(false);
            DialogsActivity.this.viewPages[1].selectedType = nextPageId;
            DialogsActivity.this.viewPages[1].setVisibility(0);
            DialogsActivity.this.animatingForward = z;
            DialogsActivity.this.showScrollbars(false);
            DialogsActivity.this.switchToCurrentSelectedMode(true);
            if (z) {
                DialogsActivity.this.viewPages[1].setTranslationX(DialogsActivity.this.viewPages[0].getMeasuredWidth());
            } else {
                DialogsActivity.this.viewPages[1].setTranslationX(-DialogsActivity.this.viewPages[0].getMeasuredWidth());
            }
            return true;
        }

        @Override
        public void setPadding(int i, int i2, int i3, int i4) {
            DialogsActivity.this.topPadding = i2;
            DialogsActivity.this.updateContextViewPosition();
            if (!DialogsActivity.this.whiteActionBar || DialogsActivity.this.searchViewPager == null) {
                requestLayout();
            } else {
                DialogsActivity.this.searchViewPager.setTranslationY(DialogsActivity.this.topPadding - DialogsActivity.this.lastMeasuredTopPadding);
            }
        }

        public boolean checkTabsAnimationInProgress() {
            boolean z;
            if (!DialogsActivity.this.tabsAnimationInProgress) {
                return false;
            }
            int i = -1;
            if (DialogsActivity.this.backAnimation) {
                if (Math.abs(DialogsActivity.this.viewPages[0].getTranslationX()) < 1.0f) {
                    DialogsActivity.this.viewPages[0].setTranslationX(0.0f);
                    ViewPage viewPage = DialogsActivity.this.viewPages[1];
                    int measuredWidth = DialogsActivity.this.viewPages[0].getMeasuredWidth();
                    if (DialogsActivity.this.animatingForward) {
                        i = 1;
                    }
                    viewPage.setTranslationX(measuredWidth * i);
                    z = true;
                }
                z = false;
            } else {
                if (Math.abs(DialogsActivity.this.viewPages[1].getTranslationX()) < 1.0f) {
                    ViewPage viewPage2 = DialogsActivity.this.viewPages[0];
                    int measuredWidth2 = DialogsActivity.this.viewPages[0].getMeasuredWidth();
                    if (!DialogsActivity.this.animatingForward) {
                        i = 1;
                    }
                    viewPage2.setTranslationX(measuredWidth2 * i);
                    DialogsActivity.this.viewPages[1].setTranslationX(0.0f);
                    z = true;
                }
                z = false;
            }
            if (z) {
                DialogsActivity.this.showScrollbars(true);
                if (DialogsActivity.this.tabsAnimation != null) {
                    DialogsActivity.this.tabsAnimation.cancel();
                    DialogsActivity.this.tabsAnimation = null;
                }
                DialogsActivity.this.tabsAnimationInProgress = false;
            }
            return DialogsActivity.this.tabsAnimationInProgress;
        }

        public int getActionBarFullHeight() {
            float height = ((BaseFragment) DialogsActivity.this).actionBar.getHeight();
            float f = 0.0f;
            float measuredHeight = (DialogsActivity.this.filterTabsView == null || DialogsActivity.this.filterTabsView.getVisibility() == 8) ? 0.0f : DialogsActivity.this.filterTabsView.getMeasuredHeight() - ((1.0f - DialogsActivity.this.filterTabsProgress) * DialogsActivity.this.filterTabsView.getMeasuredHeight());
            if (!(DialogsActivity.this.searchTabsView == null || DialogsActivity.this.searchTabsView.getVisibility() == 8)) {
                f = DialogsActivity.this.searchTabsView.getMeasuredHeight();
            }
            return (int) (height + (measuredHeight * (1.0f - DialogsActivity.this.searchAnimationProgress)) + (f * DialogsActivity.this.searchAnimationProgress));
        }

        @Override
        protected boolean drawChild(Canvas canvas, View view, long j) {
            boolean z;
            if ((view == DialogsActivity.this.fragmentContextView && DialogsActivity.this.fragmentContextView.isCallStyle()) || view == DialogsActivity.this.blurredView) {
                return true;
            }
            int i = 0;
            if (view == DialogsActivity.this.viewPages[0] || ((DialogsActivity.this.viewPages.length > 1 && view == DialogsActivity.this.viewPages[1]) || view == DialogsActivity.this.fragmentContextView || view == DialogsActivity.this.fragmentLocationContextView || view == DialogsActivity.this.searchViewPager)) {
                canvas.save();
                canvas.clipRect(0.0f, (-getY()) + ((BaseFragment) DialogsActivity.this).actionBar.getY() + getActionBarFullHeight(), getMeasuredWidth(), getMeasuredHeight());
                DialogsActivity dialogsActivity = DialogsActivity.this;
                float f = dialogsActivity.slideFragmentProgress;
                if (f != 1.0f) {
                    float f2 = 1.0f - ((1.0f - f) * 0.05f);
                    canvas.translate((dialogsActivity.isDrawerTransition ? AndroidUtilities.dp(4.0f) : -AndroidUtilities.dp(4.0f)) * (1.0f - DialogsActivity.this.slideFragmentProgress), 0.0f);
                    canvas.scale(f2, f2, DialogsActivity.this.isDrawerTransition ? getMeasuredWidth() : 0.0f, (-getY()) + ((BaseFragment) DialogsActivity.this).actionBar.getY() + getActionBarFullHeight());
                }
                z = super.drawChild(canvas, view, j);
                canvas.restore();
            } else if (view != ((BaseFragment) DialogsActivity.this).actionBar || DialogsActivity.this.slideFragmentProgress == 1.0f) {
                z = super.drawChild(canvas, view, j);
            } else {
                canvas.save();
                DialogsActivity dialogsActivity2 = DialogsActivity.this;
                float f3 = 1.0f - ((1.0f - dialogsActivity2.slideFragmentProgress) * 0.05f);
                canvas.translate((dialogsActivity2.isDrawerTransition ? AndroidUtilities.dp(4.0f) : -AndroidUtilities.dp(4.0f)) * (1.0f - DialogsActivity.this.slideFragmentProgress), 0.0f);
                float measuredWidth = DialogsActivity.this.isDrawerTransition ? getMeasuredWidth() : 0.0f;
                if (((BaseFragment) DialogsActivity.this).actionBar.getOccupyStatusBar()) {
                    i = AndroidUtilities.statusBarHeight;
                }
                canvas.scale(f3, f3, measuredWidth, i + (ActionBar.getCurrentActionBarHeight() / 2.0f));
                z = super.drawChild(canvas, view, j);
                canvas.restore();
            }
            if (view == ((BaseFragment) DialogsActivity.this).actionBar && ((BaseFragment) DialogsActivity.this).parentLayout != null) {
                int y = (int) (((BaseFragment) DialogsActivity.this).actionBar.getY() + getActionBarFullHeight());
                ((BaseFragment) DialogsActivity.this).parentLayout.drawHeaderShadow(canvas, (int) ((1.0f - DialogsActivity.this.searchAnimationProgress) * 255.0f), y);
                if (DialogsActivity.this.searchAnimationProgress > 0.0f) {
                    if (DialogsActivity.this.searchAnimationProgress < 1.0f) {
                        int alpha = Theme.dividerPaint.getAlpha();
                        Theme.dividerPaint.setAlpha((int) (alpha * DialogsActivity.this.searchAnimationProgress));
                        float f4 = y;
                        canvas.drawLine(0.0f, f4, getMeasuredWidth(), f4, Theme.dividerPaint);
                        Theme.dividerPaint.setAlpha(alpha);
                    } else {
                        float f5 = y;
                        canvas.drawLine(0.0f, f5, getMeasuredWidth(), f5, Theme.dividerPaint);
                    }
                }
            }
            return z;
        }

        @Override
        public void dispatchDraw(Canvas canvas) {
            int i;
            Paint paint;
            Paint paint2;
            int i2;
            int actionBarFullHeight = getActionBarFullHeight();
            if (((BaseFragment) DialogsActivity.this).inPreviewMode) {
                i = AndroidUtilities.statusBarHeight;
            } else {
                i = (int) ((-getY()) + ((BaseFragment) DialogsActivity.this).actionBar.getY());
            }
            int i3 = i;
            String str = "actionBarDefault";
            if (DialogsActivity.this.whiteActionBar) {
                if (DialogsActivity.this.searchAnimationProgress == 1.0f) {
                    this.actionBarSearchPaint.setColor(Theme.getColor("windowBackgroundWhite"));
                    if (DialogsActivity.this.searchTabsView != null) {
                        DialogsActivity.this.searchTabsView.setTranslationY(0.0f);
                        DialogsActivity.this.searchTabsView.setAlpha(1.0f);
                        if (DialogsActivity.this.filtersView != null) {
                            DialogsActivity.this.filtersView.setTranslationY(0.0f);
                            DialogsActivity.this.filtersView.setAlpha(1.0f);
                        }
                    }
                } else if (DialogsActivity.this.searchAnimationProgress == 0.0f && DialogsActivity.this.filterTabsView != null && DialogsActivity.this.filterTabsView.getVisibility() == 0) {
                    DialogsActivity.this.filterTabsView.setTranslationY(((BaseFragment) DialogsActivity.this).actionBar.getTranslationY());
                }
                Rect rect = AndroidUtilities.rectTmp2;
                int i4 = i3 + actionBarFullHeight;
                rect.set(0, i3, getMeasuredWidth(), i4);
                if (DialogsActivity.this.searchAnimationProgress == 1.0f) {
                    paint2 = this.actionBarSearchPaint;
                } else {
                    paint2 = DialogsActivity.this.actionBarDefaultPaint;
                }
                drawBlurRect(canvas, 0.0f, rect, paint2, true);
                if (DialogsActivity.this.searchAnimationProgress > 0.0f && DialogsActivity.this.searchAnimationProgress < 1.0f) {
                    Paint paint3 = this.actionBarSearchPaint;
                    if (DialogsActivity.this.folderId != 0) {
                        str = "actionBarDefaultArchived";
                    }
                    paint3.setColor(ColorUtils.blendARGB(Theme.getColor(str), Theme.getColor("windowBackgroundWhite"), DialogsActivity.this.searchAnimationProgress));
                    if (DialogsActivity.this.searchIsShowed || !DialogsActivity.this.searchWasFullyShowed) {
                        canvas.save();
                        canvas.clipRect(0, i3, getMeasuredWidth(), i4);
                        drawBlurCircle(canvas, 0.0f, getMeasuredWidth() - AndroidUtilities.dp(24.0f), (((BaseFragment) DialogsActivity.this).actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ((((BaseFragment) DialogsActivity.this).actionBar.getMeasuredHeight() - i2) / 2.0f), getMeasuredWidth() * 1.3f * DialogsActivity.this.searchAnimationProgress, this.actionBarSearchPaint, true);
                        canvas.restore();
                    } else {
                        rect.set(0, i3, getMeasuredWidth(), i4);
                        drawBlurRect(canvas, 0.0f, rect, this.actionBarSearchPaint, true);
                    }
                    if (DialogsActivity.this.filterTabsView != null && DialogsActivity.this.filterTabsView.getVisibility() == 0) {
                        DialogsActivity.this.filterTabsView.setTranslationY(actionBarFullHeight - (((BaseFragment) DialogsActivity.this).actionBar.getHeight() + DialogsActivity.this.filterTabsView.getMeasuredHeight()));
                    }
                    if (DialogsActivity.this.searchTabsView != null) {
                        float height = actionBarFullHeight - (((BaseFragment) DialogsActivity.this).actionBar.getHeight() + DialogsActivity.this.searchTabsView.getMeasuredHeight());
                        float f = DialogsActivity.this.searchAnimationTabsDelayedCrossfade ? DialogsActivity.this.searchAnimationProgress < 0.5f ? 0.0f : (DialogsActivity.this.searchAnimationProgress - 0.5f) / 0.5f : DialogsActivity.this.searchAnimationProgress;
                        DialogsActivity.this.searchTabsView.setTranslationY(height);
                        DialogsActivity.this.searchTabsView.setAlpha(f);
                        if (DialogsActivity.this.filtersView != null) {
                            DialogsActivity.this.filtersView.setTranslationY(height);
                            DialogsActivity.this.filtersView.setAlpha(f);
                        }
                    }
                }
            } else if (!((BaseFragment) DialogsActivity.this).inPreviewMode) {
                if (DialogsActivity.this.progressToActionMode > 0.0f) {
                    Paint paint4 = this.actionBarSearchPaint;
                    if (DialogsActivity.this.folderId != 0) {
                        str = "actionBarDefaultArchived";
                    }
                    paint4.setColor(ColorUtils.blendARGB(Theme.getColor(str), Theme.getColor("windowBackgroundWhite"), DialogsActivity.this.progressToActionMode));
                    Rect rect2 = AndroidUtilities.rectTmp2;
                    rect2.set(0, i3, getMeasuredWidth(), i3 + actionBarFullHeight);
                    drawBlurRect(canvas, 0.0f, rect2, this.actionBarSearchPaint, true);
                } else {
                    Rect rect3 = AndroidUtilities.rectTmp2;
                    rect3.set(0, i3, getMeasuredWidth(), i3 + actionBarFullHeight);
                    drawBlurRect(canvas, 0.0f, rect3, DialogsActivity.this.actionBarDefaultPaint, true);
                }
            }
            DialogsActivity.this.tabsYOffset = 0.0f;
            if (DialogsActivity.this.filtersTabAnimator != null && DialogsActivity.this.filterTabsView != null && DialogsActivity.this.filterTabsView.getVisibility() == 0) {
                DialogsActivity dialogsActivity = DialogsActivity.this;
                dialogsActivity.tabsYOffset = (-(1.0f - dialogsActivity.filterTabsProgress)) * DialogsActivity.this.filterTabsView.getMeasuredHeight();
                DialogsActivity.this.filterTabsView.setTranslationY(((BaseFragment) DialogsActivity.this).actionBar.getTranslationY() + DialogsActivity.this.tabsYOffset);
                DialogsActivity.this.filterTabsView.setAlpha(DialogsActivity.this.filterTabsProgress);
                DialogsActivity.this.viewPages[0].setTranslationY((-(1.0f - DialogsActivity.this.filterTabsProgress)) * DialogsActivity.this.filterTabsMoveFrom);
            } else if (DialogsActivity.this.filterTabsView != null && DialogsActivity.this.filterTabsView.getVisibility() == 0) {
                DialogsActivity.this.filterTabsView.setTranslationY(((BaseFragment) DialogsActivity.this).actionBar.getTranslationY());
                DialogsActivity.this.filterTabsView.setAlpha(1.0f);
            }
            DialogsActivity.this.updateContextViewPosition();
            super.dispatchDraw(canvas);
            if (DialogsActivity.this.whiteActionBar && DialogsActivity.this.searchAnimationProgress > 0.0f && DialogsActivity.this.searchAnimationProgress < 1.0f && DialogsActivity.this.searchTabsView != null) {
                this.windowBackgroundPaint.setColor(Theme.getColor("windowBackgroundWhite"));
                this.windowBackgroundPaint.setAlpha((int) (paint.getAlpha() * DialogsActivity.this.searchAnimationProgress));
                canvas.drawRect(0.0f, actionBarFullHeight + i3, getMeasuredWidth(), i3 + ((BaseFragment) DialogsActivity.this).actionBar.getMeasuredHeight() + DialogsActivity.this.searchTabsView.getMeasuredHeight(), this.windowBackgroundPaint);
            }
            if (DialogsActivity.this.fragmentContextView != null && DialogsActivity.this.fragmentContextView.isCallStyle()) {
                canvas.save();
                canvas.translate(DialogsActivity.this.fragmentContextView.getX(), DialogsActivity.this.fragmentContextView.getY());
                DialogsActivity dialogsActivity2 = DialogsActivity.this;
                float f2 = dialogsActivity2.slideFragmentProgress;
                if (f2 != 1.0f) {
                    float f3 = 1.0f - ((1.0f - f2) * 0.05f);
                    canvas.translate((dialogsActivity2.isDrawerTransition ? AndroidUtilities.dp(4.0f) : -AndroidUtilities.dp(4.0f)) * (1.0f - DialogsActivity.this.slideFragmentProgress), 0.0f);
                    canvas.scale(f3, 1.0f, DialogsActivity.this.isDrawerTransition ? getMeasuredWidth() : 0.0f, DialogsActivity.this.fragmentContextView.getY());
                }
                DialogsActivity.this.fragmentContextView.setDrawOverlay(true);
                DialogsActivity.this.fragmentContextView.draw(canvas);
                DialogsActivity.this.fragmentContextView.setDrawOverlay(false);
                canvas.restore();
            }
            if (DialogsActivity.this.blurredView != null && DialogsActivity.this.blurredView.getVisibility() == 0) {
                if (DialogsActivity.this.blurredView.getAlpha() == 1.0f) {
                    DialogsActivity.this.blurredView.draw(canvas);
                } else if (DialogsActivity.this.blurredView.getAlpha() != 0.0f) {
                    canvas.saveLayerAlpha(DialogsActivity.this.blurredView.getLeft(), DialogsActivity.this.blurredView.getTop(), DialogsActivity.this.blurredView.getRight(), DialogsActivity.this.blurredView.getBottom(), (int) (DialogsActivity.this.blurredView.getAlpha() * 255.0f), 31);
                    canvas.translate(DialogsActivity.this.blurredView.getLeft(), DialogsActivity.this.blurredView.getTop());
                    DialogsActivity.this.blurredView.draw(canvas);
                    canvas.restore();
                }
            }
            if (DialogsActivity.this.scrimView != null) {
                canvas.drawRect(0.0f, 0.0f, getMeasuredWidth(), getMeasuredHeight(), DialogsActivity.this.scrimPaint);
                canvas.save();
                getLocationInWindow(this.pos);
                canvas.translate(DialogsActivity.this.scrimViewLocation[0] - this.pos[0], DialogsActivity.this.scrimViewLocation[1] - (Build.VERSION.SDK_INT < 21 ? AndroidUtilities.statusBarHeight : 0));
                DialogsActivity.this.scrimView.draw(canvas);
                if (DialogsActivity.this.scrimViewSelected) {
                    Drawable selectorDrawable = DialogsActivity.this.filterTabsView.getSelectorDrawable();
                    canvas.translate(-DialogsActivity.this.scrimViewLocation[0], (-selectorDrawable.getIntrinsicHeight()) - 1);
                    selectorDrawable.draw(canvas);
                }
                canvas.restore();
            }
        }

        @Override
        protected void onMeasure(int i, int i2) {
            int size = View.MeasureSpec.getSize(i);
            int size2 = View.MeasureSpec.getSize(i2);
            setMeasuredDimension(size, size2);
            int paddingTop = size2 - getPaddingTop();
            if (DialogsActivity.this.doneItem != null) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) DialogsActivity.this.doneItem.getLayoutParams();
                layoutParams.topMargin = ((BaseFragment) DialogsActivity.this).actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0;
                layoutParams.height = ActionBar.getCurrentActionBarHeight();
            }
            measureChildWithMargins(((BaseFragment) DialogsActivity.this).actionBar, i, 0, i2, 0);
            int measureKeyboardHeight = measureKeyboardHeight();
            int childCount = getChildCount();
            float f = 0.0f;
            if (DialogsActivity.this.commentView != null) {
                measureChildWithMargins(DialogsActivity.this.commentView, i, 0, i2, 0);
                Object tag = DialogsActivity.this.commentView.getTag();
                if (tag == null || !tag.equals(2)) {
                    this.inputFieldHeight = 0;
                } else {
                    if (measureKeyboardHeight <= AndroidUtilities.dp(20.0f) && !AndroidUtilities.isInMultiwindow) {
                        paddingTop -= DialogsActivity.this.commentView.getEmojiPadding();
                    }
                    this.inputFieldHeight = DialogsActivity.this.commentView.getMeasuredHeight();
                }
                if (SharedConfig.smoothKeyboard && DialogsActivity.this.commentView.isPopupShowing()) {
                    ((BaseFragment) DialogsActivity.this).fragmentView.setTranslationY(0.0f);
                    for (int i3 = 0; i3 < DialogsActivity.this.viewPages.length; i3++) {
                        if (DialogsActivity.this.viewPages[i3] != null) {
                            DialogsActivity.this.viewPages[i3].setTranslationY(0.0f);
                        }
                    }
                    if (!DialogsActivity.this.onlySelect) {
                        ((BaseFragment) DialogsActivity.this).actionBar.setTranslationY(0.0f);
                    }
                    DialogsActivity.this.searchViewPager.setTranslationY(0.0f);
                }
            }
            int i4 = 0;
            while (i4 < childCount) {
                View childAt = getChildAt(i4);
                if (!(childAt == null || childAt.getVisibility() == 8 || childAt == DialogsActivity.this.commentView || childAt == ((BaseFragment) DialogsActivity.this).actionBar)) {
                    if (childAt instanceof DatabaseMigrationHint) {
                        childAt.measure(View.MeasureSpec.makeMeasureSpec(size, 1073741824), View.MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10.0f), (((View.MeasureSpec.getSize(i2) + measureKeyboardHeight) - this.inputFieldHeight) + AndroidUtilities.dp(2.0f)) - ((BaseFragment) DialogsActivity.this).actionBar.getMeasuredHeight()), 1073741824));
                    } else if (childAt instanceof ViewPage) {
                        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, 1073741824);
                        int dp = (DialogsActivity.this.filterTabsView == null || DialogsActivity.this.filterTabsView.getVisibility() != 0) ? (((paddingTop - this.inputFieldHeight) + AndroidUtilities.dp(2.0f)) - (DialogsActivity.this.onlySelect ? 0 : ((BaseFragment) DialogsActivity.this).actionBar.getMeasuredHeight())) - DialogsActivity.this.topPadding : (((paddingTop - this.inputFieldHeight) + AndroidUtilities.dp(2.0f)) - AndroidUtilities.dp(44.0f)) - DialogsActivity.this.topPadding;
                        if (DialogsActivity.this.filtersTabAnimator == null || DialogsActivity.this.filterTabsView == null || DialogsActivity.this.filterTabsView.getVisibility() != 0) {
                            childAt.setTranslationY(f);
                        } else {
                            dp = (int) (dp + DialogsActivity.this.filterTabsMoveFrom);
                        }
                        DialogsActivity dialogsActivity = DialogsActivity.this;
                        int i5 = (dialogsActivity.isSlideBackTransition || dialogsActivity.isDrawerTransition) ? (int) (dp * 0.05f) : 0;
                        childAt.setPadding(childAt.getPaddingLeft(), childAt.getPaddingTop(), childAt.getPaddingRight(), i5);
                        childAt.measure(makeMeasureSpec, View.MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10.0f), dp + i5), 1073741824));
                        childAt.setPivotX(childAt.getMeasuredWidth() / 2);
                    } else {
                        if (childAt == DialogsActivity.this.searchViewPager) {
                            DialogsActivity.this.searchViewPager.setTranslationY(0.0f);
                            childAt.measure(View.MeasureSpec.makeMeasureSpec(size, 1073741824), View.MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10.0f), ((((View.MeasureSpec.getSize(i2) + measureKeyboardHeight) - this.inputFieldHeight) + AndroidUtilities.dp(2.0f)) - (DialogsActivity.this.onlySelect ? 0 : ((BaseFragment) DialogsActivity.this).actionBar.getMeasuredHeight())) - DialogsActivity.this.topPadding) - (DialogsActivity.this.searchTabsView == null ? 0 : AndroidUtilities.dp(44.0f)), 1073741824));
                            childAt.setPivotX(childAt.getMeasuredWidth() / 2);
                        } else if (DialogsActivity.this.commentView == null || !DialogsActivity.this.commentView.isPopupView(childAt)) {
                            measureChildWithMargins(childAt, i, 0, i2, 0);
                        } else if (!AndroidUtilities.isInMultiwindow) {
                            childAt.measure(View.MeasureSpec.makeMeasureSpec(size, 1073741824), View.MeasureSpec.makeMeasureSpec(childAt.getLayoutParams().height, 1073741824));
                        } else if (AndroidUtilities.isTablet()) {
                            childAt.measure(View.MeasureSpec.makeMeasureSpec(size, 1073741824), View.MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(320.0f), ((paddingTop - this.inputFieldHeight) - AndroidUtilities.statusBarHeight) + getPaddingTop()), 1073741824));
                        } else {
                            childAt.measure(View.MeasureSpec.makeMeasureSpec(size, 1073741824), View.MeasureSpec.makeMeasureSpec(((paddingTop - this.inputFieldHeight) - AndroidUtilities.statusBarHeight) + getPaddingTop(), 1073741824));
                        }
                        i4++;
                        f = 0.0f;
                    }
                }
                i4++;
                f = 0.0f;
            }
        }

        @Override
        public void onLayout(boolean r16, int r17, int r18, int r19, int r20) {
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.ContentView.onLayout(boolean, int, int, int, int):void");
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if ((actionMasked == 1 || actionMasked == 3) && ((BaseFragment) DialogsActivity.this).actionBar.isActionModeShowed()) {
                DialogsActivity.this.allowMoving = true;
            }
            if (!checkTabsAnimationInProgress()) {
                return (DialogsActivity.this.filterTabsView != null && DialogsActivity.this.filterTabsView.isAnimatingIndicator()) || onTouchEvent(motionEvent);
            }
            return true;
        }

        @Override
        public void requestDisallowInterceptTouchEvent(boolean z) {
            if (DialogsActivity.this.maybeStartTracking && !DialogsActivity.this.startedTracking) {
                onTouchEvent(null);
            }
            super.requestDisallowInterceptTouchEvent(z);
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            float f;
            float f2;
            float f3;
            int i;
            boolean z = false;
            if (((BaseFragment) DialogsActivity.this).parentLayout == null || DialogsActivity.this.filterTabsView == null || DialogsActivity.this.filterTabsView.isEditing() || DialogsActivity.this.searching || ((BaseFragment) DialogsActivity.this).parentLayout.checkTransitionAnimation() || ((BaseFragment) DialogsActivity.this).parentLayout.isInPreviewMode() || ((BaseFragment) DialogsActivity.this).parentLayout.isPreviewOpenAnimationInProgress() || ((BaseFragment) DialogsActivity.this).parentLayout.getDrawerLayoutContainer().isDrawerOpened() || ((motionEvent != null && !DialogsActivity.this.startedTracking && motionEvent.getY() <= ((BaseFragment) DialogsActivity.this).actionBar.getMeasuredHeight() + ((BaseFragment) DialogsActivity.this).actionBar.getTranslationY()) || SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) != 5)) {
                return false;
            }
            if (motionEvent != null) {
                if (this.velocityTracker == null) {
                    this.velocityTracker = VelocityTracker.obtain();
                }
                this.velocityTracker.addMovement(motionEvent);
            }
            if (motionEvent != null && motionEvent.getAction() == 0 && checkTabsAnimationInProgress()) {
                DialogsActivity.this.startedTracking = true;
                this.startedTrackingPointerId = motionEvent.getPointerId(0);
                this.startedTrackingX = (int) motionEvent.getX();
                ((BaseFragment) DialogsActivity.this).parentLayout.getDrawerLayoutContainer().setAllowOpenDrawerBySwipe(false);
                if (DialogsActivity.this.animatingForward) {
                    if (this.startedTrackingX < DialogsActivity.this.viewPages[0].getMeasuredWidth() + DialogsActivity.this.viewPages[0].getTranslationX()) {
                        DialogsActivity dialogsActivity = DialogsActivity.this;
                        dialogsActivity.additionalOffset = dialogsActivity.viewPages[0].getTranslationX();
                    } else {
                        ViewPage viewPage = DialogsActivity.this.viewPages[0];
                        DialogsActivity.this.viewPages[0] = DialogsActivity.this.viewPages[1];
                        DialogsActivity.this.viewPages[1] = viewPage;
                        DialogsActivity.this.animatingForward = false;
                        DialogsActivity dialogsActivity2 = DialogsActivity.this;
                        dialogsActivity2.additionalOffset = dialogsActivity2.viewPages[0].getTranslationX();
                        DialogsActivity.this.filterTabsView.selectTabWithId(DialogsActivity.this.viewPages[0].selectedType, 1.0f);
                        DialogsActivity.this.filterTabsView.selectTabWithId(DialogsActivity.this.viewPages[1].selectedType, DialogsActivity.this.additionalOffset / DialogsActivity.this.viewPages[0].getMeasuredWidth());
                        DialogsActivity.this.switchToCurrentSelectedMode(true);
                        DialogsActivity.this.viewPages[0].dialogsAdapter.resume();
                        DialogsActivity.this.viewPages[1].dialogsAdapter.pause();
                    }
                } else if (this.startedTrackingX < DialogsActivity.this.viewPages[1].getMeasuredWidth() + DialogsActivity.this.viewPages[1].getTranslationX()) {
                    ViewPage viewPage2 = DialogsActivity.this.viewPages[0];
                    DialogsActivity.this.viewPages[0] = DialogsActivity.this.viewPages[1];
                    DialogsActivity.this.viewPages[1] = viewPage2;
                    DialogsActivity.this.animatingForward = true;
                    DialogsActivity dialogsActivity3 = DialogsActivity.this;
                    dialogsActivity3.additionalOffset = dialogsActivity3.viewPages[0].getTranslationX();
                    DialogsActivity.this.filterTabsView.selectTabWithId(DialogsActivity.this.viewPages[0].selectedType, 1.0f);
                    DialogsActivity.this.filterTabsView.selectTabWithId(DialogsActivity.this.viewPages[1].selectedType, (-DialogsActivity.this.additionalOffset) / DialogsActivity.this.viewPages[0].getMeasuredWidth());
                    DialogsActivity.this.switchToCurrentSelectedMode(true);
                    DialogsActivity.this.viewPages[0].dialogsAdapter.resume();
                    DialogsActivity.this.viewPages[1].dialogsAdapter.pause();
                } else {
                    DialogsActivity dialogsActivity4 = DialogsActivity.this;
                    dialogsActivity4.additionalOffset = dialogsActivity4.viewPages[0].getTranslationX();
                }
                DialogsActivity.this.tabsAnimation.removeAllListeners();
                DialogsActivity.this.tabsAnimation.cancel();
                DialogsActivity.this.tabsAnimationInProgress = false;
            } else if (motionEvent != null && motionEvent.getAction() == 0) {
                DialogsActivity.this.additionalOffset = 0.0f;
            }
            if (motionEvent != null && motionEvent.getAction() == 0 && !DialogsActivity.this.startedTracking && !DialogsActivity.this.maybeStartTracking && DialogsActivity.this.filterTabsView.getVisibility() == 0) {
                this.startedTrackingPointerId = motionEvent.getPointerId(0);
                DialogsActivity.this.maybeStartTracking = true;
                this.startedTrackingX = (int) motionEvent.getX();
                this.startedTrackingY = (int) motionEvent.getY();
                this.velocityTracker.clear();
            } else if (motionEvent != null && motionEvent.getAction() == 2 && motionEvent.getPointerId(0) == this.startedTrackingPointerId) {
                int x = (int) ((motionEvent.getX() - this.startedTrackingX) + DialogsActivity.this.additionalOffset);
                int abs = Math.abs(((int) motionEvent.getY()) - this.startedTrackingY);
                if (DialogsActivity.this.startedTracking && ((DialogsActivity.this.animatingForward && x > 0) || (!DialogsActivity.this.animatingForward && x < 0))) {
                    if (!prepareForMoving(motionEvent, x < 0)) {
                        DialogsActivity.this.maybeStartTracking = true;
                        DialogsActivity.this.startedTracking = false;
                        DialogsActivity.this.viewPages[0].setTranslationX(0.0f);
                        DialogsActivity.this.viewPages[1].setTranslationX(DialogsActivity.this.animatingForward ? DialogsActivity.this.viewPages[0].getMeasuredWidth() : -DialogsActivity.this.viewPages[0].getMeasuredWidth());
                        DialogsActivity.this.filterTabsView.selectTabWithId(DialogsActivity.this.viewPages[1].selectedType, 0.0f);
                    }
                }
                if (DialogsActivity.this.maybeStartTracking && !DialogsActivity.this.startedTracking) {
                    float pixelsInCM = AndroidUtilities.getPixelsInCM(0.3f, true);
                    int x2 = (int) (motionEvent.getX() - this.startedTrackingX);
                    if (Math.abs(x2) >= pixelsInCM && Math.abs(x2) > abs) {
                        if (x < 0) {
                            z = true;
                        }
                        prepareForMoving(motionEvent, z);
                    }
                } else if (DialogsActivity.this.startedTracking) {
                    DialogsActivity.this.viewPages[0].setTranslationX(x);
                    if (DialogsActivity.this.animatingForward) {
                        DialogsActivity.this.viewPages[1].setTranslationX(DialogsActivity.this.viewPages[0].getMeasuredWidth() + x);
                    } else {
                        DialogsActivity.this.viewPages[1].setTranslationX(x - DialogsActivity.this.viewPages[0].getMeasuredWidth());
                    }
                    DialogsActivity.this.filterTabsView.selectTabWithId(DialogsActivity.this.viewPages[1].selectedType, Math.abs(x) / DialogsActivity.this.viewPages[0].getMeasuredWidth());
                }
            } else if (motionEvent == null || (motionEvent.getPointerId(0) == this.startedTrackingPointerId && (motionEvent.getAction() == 3 || motionEvent.getAction() == 1 || motionEvent.getAction() == 6))) {
                this.velocityTracker.computeCurrentVelocity(1000, DialogsActivity.this.maximumVelocity);
                if (motionEvent == null || motionEvent.getAction() == 3) {
                    f2 = 0.0f;
                    f = 0.0f;
                } else {
                    f2 = this.velocityTracker.getXVelocity();
                    f = this.velocityTracker.getYVelocity();
                    if (!DialogsActivity.this.startedTracking && Math.abs(f2) >= 3000.0f && Math.abs(f2) > Math.abs(f)) {
                        prepareForMoving(motionEvent, f2 < 0.0f);
                    }
                }
                if (DialogsActivity.this.startedTracking) {
                    float x3 = DialogsActivity.this.viewPages[0].getX();
                    DialogsActivity.this.tabsAnimation = new AnimatorSet();
                    if (DialogsActivity.this.additionalOffset == 0.0f) {
                        DialogsActivity.this.backAnimation = Math.abs(x3) < ((float) DialogsActivity.this.viewPages[0].getMeasuredWidth()) / 3.0f && (Math.abs(f2) < 3500.0f || Math.abs(f2) < Math.abs(f));
                    } else if (Math.abs(f2) > 1500.0f) {
                        DialogsActivity dialogsActivity5 = DialogsActivity.this;
                        dialogsActivity5.backAnimation = !dialogsActivity5.animatingForward ? f2 < 0.0f : f2 > 0.0f;
                    } else if (DialogsActivity.this.animatingForward) {
                        DialogsActivity dialogsActivity6 = DialogsActivity.this;
                        dialogsActivity6.backAnimation = dialogsActivity6.viewPages[1].getX() > ((float) (DialogsActivity.this.viewPages[0].getMeasuredWidth() >> 1));
                    } else {
                        DialogsActivity dialogsActivity7 = DialogsActivity.this;
                        dialogsActivity7.backAnimation = dialogsActivity7.viewPages[0].getX() < ((float) (DialogsActivity.this.viewPages[0].getMeasuredWidth() >> 1));
                    }
                    if (DialogsActivity.this.backAnimation) {
                        f3 = Math.abs(x3);
                        if (DialogsActivity.this.animatingForward) {
                            DialogsActivity.this.tabsAnimation.playTogether(ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[0], View.TRANSLATION_X, 0.0f), ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[1], View.TRANSLATION_X, DialogsActivity.this.viewPages[1].getMeasuredWidth()));
                        } else {
                            DialogsActivity.this.tabsAnimation.playTogether(ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[0], View.TRANSLATION_X, 0.0f), ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[1], View.TRANSLATION_X, -DialogsActivity.this.viewPages[1].getMeasuredWidth()));
                        }
                    } else {
                        f3 = DialogsActivity.this.viewPages[0].getMeasuredWidth() - Math.abs(x3);
                        if (DialogsActivity.this.animatingForward) {
                            DialogsActivity.this.tabsAnimation.playTogether(ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[0], View.TRANSLATION_X, -DialogsActivity.this.viewPages[0].getMeasuredWidth()), ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[1], View.TRANSLATION_X, 0.0f));
                        } else {
                            DialogsActivity.this.tabsAnimation.playTogether(ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[0], View.TRANSLATION_X, DialogsActivity.this.viewPages[0].getMeasuredWidth()), ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[1], View.TRANSLATION_X, 0.0f));
                        }
                    }
                    DialogsActivity.this.tabsAnimation.setInterpolator(DialogsActivity.interpolator);
                    int measuredWidth = getMeasuredWidth();
                    float f4 = measuredWidth / 2;
                    float distanceInfluenceForSnapDuration = f4 + (AndroidUtilities.distanceInfluenceForSnapDuration(Math.min(1.0f, (f3 * 1.0f) / measuredWidth)) * f4);
                    float abs2 = Math.abs(f2);
                    if (abs2 > 0.0f) {
                        i = Math.round(Math.abs(distanceInfluenceForSnapDuration / abs2) * 1000.0f) * 4;
                    } else {
                        i = (int) (((f3 / getMeasuredWidth()) + 1.0f) * 100.0f);
                    }
                    DialogsActivity.this.tabsAnimation.setDuration(Math.max((int) ImageReceiver.DEFAULT_CROSSFADE_DURATION, Math.min(i, 600)));
                    DialogsActivity.this.tabsAnimation.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            DialogsActivity.this.tabsAnimation = null;
                            if (!DialogsActivity.this.backAnimation) {
                                ViewPage viewPage3 = DialogsActivity.this.viewPages[0];
                                DialogsActivity.this.viewPages[0] = DialogsActivity.this.viewPages[1];
                                DialogsActivity.this.viewPages[1] = viewPage3;
                                DialogsActivity.this.filterTabsView.selectTabWithId(DialogsActivity.this.viewPages[0].selectedType, 1.0f);
                                DialogsActivity.this.updateCounters(false);
                                DialogsActivity.this.viewPages[0].dialogsAdapter.resume();
                                DialogsActivity.this.viewPages[1].dialogsAdapter.pause();
                            }
                            if (((BaseFragment) DialogsActivity.this).parentLayout != null) {
                                ((BaseFragment) DialogsActivity.this).parentLayout.getDrawerLayoutContainer().setAllowOpenDrawerBySwipe(DialogsActivity.this.viewPages[0].selectedType == DialogsActivity.this.filterTabsView.getFirstTabId() || DialogsActivity.this.searchIsShowed || SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) != 5);
                            }
                            DialogsActivity.this.viewPages[1].setVisibility(8);
                            DialogsActivity.this.showScrollbars(true);
                            DialogsActivity.this.tabsAnimationInProgress = false;
                            DialogsActivity.this.maybeStartTracking = false;
                            ((BaseFragment) DialogsActivity.this).actionBar.setEnabled(true);
                            DialogsActivity.this.filterTabsView.setEnabled(true);
                            DialogsActivity dialogsActivity8 = DialogsActivity.this;
                            dialogsActivity8.checkListLoad(dialogsActivity8.viewPages[0]);
                        }
                    });
                    DialogsActivity.this.tabsAnimation.start();
                    DialogsActivity.this.tabsAnimationInProgress = true;
                    DialogsActivity.this.startedTracking = false;
                } else {
                    ((BaseFragment) DialogsActivity.this).parentLayout.getDrawerLayoutContainer().setAllowOpenDrawerBySwipe(DialogsActivity.this.viewPages[0].selectedType == DialogsActivity.this.filterTabsView.getFirstTabId() || DialogsActivity.this.searchIsShowed || SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) != 5);
                    DialogsActivity.this.maybeStartTracking = false;
                    ((BaseFragment) DialogsActivity.this).actionBar.setEnabled(true);
                    DialogsActivity.this.filterTabsView.setEnabled(true);
                }
                VelocityTracker velocityTracker = this.velocityTracker;
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    this.velocityTracker = null;
                }
            }
            return DialogsActivity.this.startedTracking;
        }

        @Override
        public void drawList(Canvas canvas, boolean z) {
            if (!DialogsActivity.this.searchIsShowed) {
                for (int i = 0; i < DialogsActivity.this.viewPages.length; i++) {
                    if (DialogsActivity.this.viewPages[i] != null && DialogsActivity.this.viewPages[i].getVisibility() == 0) {
                        for (int i2 = 0; i2 < DialogsActivity.this.viewPages[i].listView.getChildCount(); i2++) {
                            View childAt = DialogsActivity.this.viewPages[i].listView.getChildAt(i2);
                            if (childAt.getY() < DialogsActivity.this.viewPages[i].listView.blurTopPadding + AndroidUtilities.dp(100.0f)) {
                                int save = canvas.save();
                                canvas.translate(DialogsActivity.this.viewPages[i].getX(), DialogsActivity.this.viewPages[i].getY() + DialogsActivity.this.viewPages[i].listView.getY() + childAt.getY());
                                if (childAt instanceof DialogCell) {
                                    DialogCell dialogCell = (DialogCell) childAt;
                                    dialogCell.drawingForBlur = true;
                                    dialogCell.draw(canvas);
                                    dialogCell.drawingForBlur = false;
                                } else {
                                    childAt.draw(canvas);
                                }
                                canvas.restoreToCount(save);
                            }
                        }
                    }
                }
            } else if (DialogsActivity.this.searchViewPager != null && DialogsActivity.this.searchViewPager.getVisibility() == 0) {
                DialogsActivity.this.searchViewPager.drawForBlur(canvas);
            }
        }
    }

    public class DialogsRecyclerView extends BlurredRecyclerView {
        private int appliedPaddingTop;
        private boolean ignoreLayout;
        private int lastListPadding;
        private final ViewPage parentPage;
        private boolean firstLayout = true;
        Paint paint = new Paint();
        RectF rectF = new RectF();

        @Override
        protected boolean updateEmptyViewAnimated() {
            return true;
        }

        public DialogsRecyclerView(Context context, ViewPage viewPage) {
            super(context);
            this.parentPage = viewPage;
            this.additionalClipBottom = AndroidUtilities.dp(200.0f);
        }

        public void setViewsOffset(float f) {
            View findViewByPosition;
            DialogsActivity.viewOffset = f;
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).setTranslationY(f);
            }
            if (!(this.selectorPosition == -1 || (findViewByPosition = getLayoutManager().findViewByPosition(this.selectorPosition)) == null)) {
                this.selectorRect.set(findViewByPosition.getLeft(), (int) (findViewByPosition.getTop() + f), findViewByPosition.getRight(), (int) (findViewByPosition.getBottom() + f));
                this.selectorDrawable.setBounds(this.selectorRect);
            }
            invalidate();
        }

        public float getViewOffset() {
            return DialogsActivity.viewOffset;
        }

        @Override
        public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
            super.addView(view, i, layoutParams);
            view.setTranslationY(DialogsActivity.viewOffset);
            view.setTranslationX(0.0f);
            view.setAlpha(1.0f);
        }

        @Override
        public void removeView(View view) {
            super.removeView(view);
            view.setTranslationY(0.0f);
            view.setTranslationX(0.0f);
            view.setAlpha(1.0f);
        }

        @Override
        public void onDraw(Canvas canvas) {
            if (!(this.parentPage.pullForegroundDrawable == null || DialogsActivity.viewOffset == 0.0f)) {
                int paddingTop = getPaddingTop();
                if (paddingTop != 0) {
                    canvas.save();
                    canvas.translate(0.0f, paddingTop);
                }
                this.parentPage.pullForegroundDrawable.drawOverScroll(canvas);
                if (paddingTop != 0) {
                    canvas.restore();
                }
            }
            super.onDraw(canvas);
        }

        @Override
        public void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            if (drawMovingViewsOverlayed()) {
                this.paint.setColor(Theme.getColor("windowBackgroundWhite"));
                for (int i = 0; i < getChildCount(); i++) {
                    View childAt = getChildAt(i);
                    if (((childAt instanceof DialogCell) && ((DialogCell) childAt).isMoving()) || ((childAt instanceof DialogsAdapter.LastEmptyView) && ((DialogsAdapter.LastEmptyView) childAt).moving)) {
                        if (childAt.getAlpha() != 1.0f) {
                            this.rectF.set(childAt.getX(), childAt.getY(), childAt.getX() + childAt.getMeasuredWidth(), childAt.getY() + childAt.getMeasuredHeight());
                            canvas.saveLayerAlpha(this.rectF, (int) (childAt.getAlpha() * 255.0f), 31);
                        } else {
                            canvas.save();
                        }
                        canvas.translate(childAt.getX(), childAt.getY());
                        canvas.drawRect(0.0f, 0.0f, childAt.getMeasuredWidth(), childAt.getMeasuredHeight(), this.paint);
                        childAt.draw(canvas);
                        canvas.restore();
                    }
                }
                invalidate();
            }
            if (!(DialogsActivity.this.slidingView == null || DialogsActivity.this.pacmanAnimation == null)) {
                DialogsActivity.this.pacmanAnimation.draw(canvas, DialogsActivity.this.slidingView.getTop() + (DialogsActivity.this.slidingView.getMeasuredHeight() / 2));
            }
        }

        private boolean drawMovingViewsOverlayed() {
            return (getItemAnimator() == null || !getItemAnimator().isRunning() || (DialogsActivity.this.dialogRemoveFinished == 0 && DialogsActivity.this.dialogInsertFinished == 0 && DialogsActivity.this.dialogChangeFinished == 0)) ? false : true;
        }

        @Override
        public boolean drawChild(Canvas canvas, View view, long j) {
            if (!drawMovingViewsOverlayed() || !(view instanceof DialogCell) || !((DialogCell) view).isMoving()) {
                return super.drawChild(canvas, view, j);
            }
            return true;
        }

        @Override
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
        }

        @Override
        public void setAdapter(RecyclerView.Adapter adapter) {
            super.setAdapter(adapter);
            this.firstLayout = true;
        }

        private void checkIfAdapterValid() {
            RecyclerView.Adapter adapter = getAdapter();
            if (this.parentPage.lastItemsCount != adapter.getItemCount() && !DialogsActivity.this.dialogsListFrozen) {
                this.ignoreLayout = true;
                adapter.notifyDataSetChanged();
                this.ignoreLayout = false;
            }
        }

        @Override
        public void onMeasure(int i, int i2) {
            int i3;
            RecyclerView.ViewHolder findViewHolderForAdapterPosition;
            if (DialogsActivity.this.onlySelect) {
                i3 = 0;
            } else if (DialogsActivity.this.filterTabsView == null || DialogsActivity.this.filterTabsView.getVisibility() != 0) {
                i3 = ((BaseFragment) DialogsActivity.this).actionBar.getMeasuredHeight();
            } else {
                i3 = AndroidUtilities.dp(44.0f);
            }
            int findFirstVisibleItemPosition = this.parentPage.layoutManager.findFirstVisibleItemPosition();
            if (findFirstVisibleItemPosition != -1 && !DialogsActivity.this.dialogsListFrozen && this.parentPage.itemTouchhelper.isIdle() && (findViewHolderForAdapterPosition = this.parentPage.listView.findViewHolderForAdapterPosition(findFirstVisibleItemPosition)) != null) {
                int top = findViewHolderForAdapterPosition.itemView.getTop();
                this.ignoreLayout = true;
                this.parentPage.layoutManager.scrollToPositionWithOffset(findFirstVisibleItemPosition, (int) ((top - this.lastListPadding) + DialogsActivity.this.scrollAdditionalOffset));
                this.ignoreLayout = false;
            }
            if (!DialogsActivity.this.onlySelect) {
                this.ignoreLayout = true;
                if (DialogsActivity.this.filterTabsView == null || DialogsActivity.this.filterTabsView.getVisibility() != 0) {
                    i3 = (!((BaseFragment) DialogsActivity.this).inPreviewMode || Build.VERSION.SDK_INT < 21) ? 0 : AndroidUtilities.statusBarHeight;
                } else {
                    i3 = ActionBar.getCurrentActionBarHeight() + (((BaseFragment) DialogsActivity.this).actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
                }
                setTopGlowOffset(i3);
                setPadding(0, i3, 0, 0);
                this.parentPage.progressView.setPaddingTop(i3);
                this.ignoreLayout = false;
            }
            if (this.firstLayout && DialogsActivity.this.getMessagesController().dialogsLoaded) {
                if (this.parentPage.dialogsType == 0 && DialogsActivity.this.hasHiddenArchive()) {
                    this.ignoreLayout = true;
                    ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(1, (int) ((BaseFragment) DialogsActivity.this).actionBar.getTranslationY());
                    this.ignoreLayout = false;
                }
                this.firstLayout = false;
            }
            checkIfAdapterValid();
            super.onMeasure(i, i2);
            if (!DialogsActivity.this.onlySelect && this.appliedPaddingTop != i3 && DialogsActivity.this.viewPages != null && DialogsActivity.this.viewPages.length > 1) {
                DialogsActivity.this.viewPages[1].setTranslationX(DialogsActivity.this.viewPages[0].getMeasuredWidth());
            }
        }

        @Override
        public void onLayout(boolean z, int i, int i2, int i3, int i4) {
            super.onLayout(z, i, i2, i3, i4);
            this.lastListPadding = getPaddingTop();
            DialogsActivity.this.scrollAdditionalOffset = 0.0f;
            if (!(DialogsActivity.this.dialogRemoveFinished == 0 && DialogsActivity.this.dialogInsertFinished == 0 && DialogsActivity.this.dialogChangeFinished == 0) && !this.parentPage.dialogsItemAnimator.isRunning()) {
                DialogsActivity.this.onDialogAnimationFinished();
            }
        }

        @Override
        public void requestLayout() {
            if (!this.ignoreLayout) {
                super.requestLayout();
            }
        }

        public void toggleArchiveHidden(boolean z, DialogCell dialogCell) {
            SharedConfig.toggleArchiveHidden();
            if (SharedConfig.archiveHidden) {
                if (dialogCell != null) {
                    DialogsActivity.this.disableActionBarScrolling = true;
                    DialogsActivity.this.waitingForScrollFinished = true;
                    smoothScrollBy(0, dialogCell.getMeasuredHeight() + (dialogCell.getTop() - getPaddingTop()), CubicBezierInterpolator.EASE_OUT);
                    if (z) {
                        DialogsActivity.this.updatePullAfterScroll = true;
                    } else {
                        updatePullState();
                    }
                }
                DialogsActivity.this.getUndoView().showWithAction(0L, 6, null, null);
                return;
            }
            DialogsActivity.this.getUndoView().showWithAction(0L, 7, null, null);
            updatePullState();
            if (z && dialogCell != null) {
                dialogCell.resetPinnedArchiveState();
                dialogCell.invalidate();
            }
        }

        public void updatePullState() {
            boolean z = false;
            this.parentPage.archivePullViewState = SharedConfig.archiveHidden ? 2 : 0;
            if (this.parentPage.pullForegroundDrawable != null) {
                PullForegroundDrawable pullForegroundDrawable = this.parentPage.pullForegroundDrawable;
                if (this.parentPage.archivePullViewState != 0) {
                    z = true;
                }
                pullForegroundDrawable.setWillDraw(z);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            LinearLayoutManager linearLayoutManager;
            int findFirstVisibleItemPosition;
            if (this.fastScrollAnimationRunning || DialogsActivity.this.waitingForScrollFinished || DialogsActivity.this.dialogRemoveFinished != 0 || DialogsActivity.this.dialogInsertFinished != 0 || DialogsActivity.this.dialogChangeFinished != 0) {
                return false;
            }
            int action = motionEvent.getAction();
            if (action == 0) {
                setOverScrollMode(0);
            }
            if ((action == 1 || action == 3) && !this.parentPage.itemTouchhelper.isIdle() && this.parentPage.swipeController.swipingFolder) {
                this.parentPage.swipeController.swipeFolderBack = true;
                if (!(this.parentPage.itemTouchhelper.checkHorizontalSwipe(null, 4) == 0 || this.parentPage.swipeController.currentItemViewHolder == null)) {
                    View view = this.parentPage.swipeController.currentItemViewHolder.itemView;
                    if (view instanceof DialogCell) {
                        DialogCell dialogCell = (DialogCell) view;
                        long dialogId = dialogCell.getDialogId();
                        if (DialogObject.isFolderDialogId(dialogId)) {
                            toggleArchiveHidden(false, dialogCell);
                        } else {
                            DialogsActivity dialogsActivity = DialogsActivity.this;
                            TLRPC$Dialog tLRPC$Dialog = dialogsActivity.getDialogsArray(((BaseFragment) dialogsActivity).currentAccount, this.parentPage.dialogsType, DialogsActivity.this.folderId, false).get(dialogCell.getDialogIndex());
                            if (SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) == 1) {
                                ArrayList arrayList = new ArrayList();
                                arrayList.add(Long.valueOf(dialogId));
                                DialogsActivity.this.canReadCount = (tLRPC$Dialog.unread_count > 0 || tLRPC$Dialog.unread_mark) ? 1 : 0;
                                DialogsActivity.this.performSelectedDialogsAction(arrayList, FileLoader.MEDIA_DIR_VIDEO_PUBLIC, true);
                            } else if (SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) == 3) {
                                if (!DialogsActivity.this.getMessagesController().isDialogMuted(dialogId)) {
                                    NotificationsController.getInstance(UserConfig.selectedAccount).setDialogNotificationsSettings(dialogId, 3);
                                    if (BulletinFactory.canShowBulletin(DialogsActivity.this)) {
                                        BulletinFactory.createMuteBulletin(DialogsActivity.this, 3).show();
                                    }
                                } else {
                                    ArrayList arrayList2 = new ArrayList();
                                    arrayList2.add(Long.valueOf(dialogId));
                                    DialogsActivity dialogsActivity2 = DialogsActivity.this;
                                    dialogsActivity2.canMuteCount = !MessagesController.getInstance(((BaseFragment) dialogsActivity2).currentAccount).isDialogMuted(dialogId);
                                    DialogsActivity dialogsActivity3 = DialogsActivity.this;
                                    dialogsActivity3.canUnmuteCount = dialogsActivity3.canMuteCount > 0 ? 0 : 1;
                                    DialogsActivity.this.performSelectedDialogsAction(arrayList2, 104, true);
                                }
                            } else if (SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) == 0) {
                                ArrayList arrayList3 = new ArrayList();
                                arrayList3.add(Long.valueOf(dialogId));
                                DialogsActivity.this.canPinCount = !DialogsActivity.this.isDialogPinned(tLRPC$Dialog) ? 1 : 0;
                                DialogsActivity.this.performSelectedDialogsAction(arrayList3, 100, true);
                            } else if (SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) == 4) {
                                ArrayList arrayList4 = new ArrayList();
                                arrayList4.add(Long.valueOf(dialogId));
                                DialogsActivity.this.performSelectedDialogsAction(arrayList4, 102, true);
                            }
                        }
                    }
                }
            }
            boolean onTouchEvent = super.onTouchEvent(motionEvent);
            if (this.parentPage.dialogsType == 0 && ((action == 1 || action == 3) && this.parentPage.archivePullViewState == 2 && DialogsActivity.this.hasHiddenArchive() && (findFirstVisibleItemPosition = (linearLayoutManager = (LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition()) == 0)) {
                int paddingTop = getPaddingTop();
                View findViewByPosition = linearLayoutManager.findViewByPosition(findFirstVisibleItemPosition);
                int dp = (int) (AndroidUtilities.dp(SharedConfig.useThreeLinesLayout ? 78.0f : 72.0f) * 0.85f);
                int top = (findViewByPosition.getTop() - paddingTop) + findViewByPosition.getMeasuredHeight();
                long currentTimeMillis = System.currentTimeMillis() - DialogsActivity.this.startArchivePullingTime;
                if (top < dp || currentTimeMillis < 200) {
                    DialogsActivity.this.disableActionBarScrolling = true;
                    smoothScrollBy(0, top, CubicBezierInterpolator.EASE_OUT_QUINT);
                    this.parentPage.archivePullViewState = 2;
                } else if (this.parentPage.archivePullViewState != 1) {
                    if (getViewOffset() == 0.0f) {
                        DialogsActivity.this.disableActionBarScrolling = true;
                        smoothScrollBy(0, findViewByPosition.getTop() - paddingTop, CubicBezierInterpolator.EASE_OUT_QUINT);
                    }
                    if (!DialogsActivity.this.canShowHiddenArchive) {
                        DialogsActivity.this.canShowHiddenArchive = true;
                        performHapticFeedback(3, 2);
                        if (this.parentPage.pullForegroundDrawable != null) {
                            this.parentPage.pullForegroundDrawable.colorize(true);
                        }
                    }
                    ((DialogCell) findViewByPosition).startOutAnimation();
                    this.parentPage.archivePullViewState = 1;
                }
                if (getViewOffset() != 0.0f) {
                    ValueAnimator ofFloat = ValueAnimator.ofFloat(getViewOffset(), 0.0f);
                    ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                            DialogsActivity.DialogsRecyclerView.this.lambda$onTouchEvent$0(valueAnimator);
                        }
                    });
                    ofFloat.setDuration(Math.max(100L, 350.0f - ((getViewOffset() / PullForegroundDrawable.getMaxOverscroll()) * 120.0f)));
                    ofFloat.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                    setScrollEnabled(false);
                    ofFloat.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            super.onAnimationEnd(animator);
                            DialogsRecyclerView.this.setScrollEnabled(true);
                        }
                    });
                    ofFloat.start();
                }
            }
            return onTouchEvent;
        }

        public void lambda$onTouchEvent$0(ValueAnimator valueAnimator) {
            setViewsOffset(((Float) valueAnimator.getAnimatedValue()).floatValue());
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
            if (this.fastScrollAnimationRunning || DialogsActivity.this.waitingForScrollFinished || DialogsActivity.this.dialogRemoveFinished != 0 || DialogsActivity.this.dialogInsertFinished != 0 || DialogsActivity.this.dialogChangeFinished != 0) {
                return false;
            }
            if (motionEvent.getAction() == 0) {
                DialogsActivity dialogsActivity = DialogsActivity.this;
                dialogsActivity.allowSwipeDuringCurrentTouch = !((BaseFragment) dialogsActivity).actionBar.isActionModeShowed();
                checkIfAdapterValid();
            }
            return super.onInterceptTouchEvent(motionEvent);
        }

        @Override
        public boolean allowSelectChildAtPosition(View view) {
            return !(view instanceof HeaderCell) || view.isClickable();
        }
    }

    public class SwipeController extends ItemTouchHelper.Callback {
        private RecyclerView.ViewHolder currentItemViewHolder;
        private ViewPage parentPage;
        private boolean swipeFolderBack;
        private boolean swipingFolder;

        @Override
        public float getSwipeEscapeVelocity(float f) {
            return 3500.0f;
        }

        @Override
        public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
            return 0.45f;
        }

        @Override
        public float getSwipeVelocityThreshold(float f) {
            return Float.MAX_VALUE;
        }

        public SwipeController(ViewPage viewPage) {
            this.parentPage = viewPage;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            TLRPC$Dialog tLRPC$Dialog;
            if (!DialogsActivity.this.waitingForDialogsAnimationEnd(this.parentPage) && (((BaseFragment) DialogsActivity.this).parentLayout == null || !((BaseFragment) DialogsActivity.this).parentLayout.isInPreviewMode())) {
                if (this.swipingFolder && this.swipeFolderBack) {
                    View view = viewHolder.itemView;
                    if (view instanceof DialogCell) {
                        ((DialogCell) view).swipeCanceled = true;
                    }
                    this.swipingFolder = false;
                    return 0;
                } else if (!DialogsActivity.this.onlySelect && this.parentPage.isDefaultDialogType() && DialogsActivity.this.slidingView == null) {
                    View view2 = viewHolder.itemView;
                    if (view2 instanceof DialogCell) {
                        DialogCell dialogCell = (DialogCell) view2;
                        long dialogId = dialogCell.getDialogId();
                        MessagesController.DialogFilter dialogFilter = null;
                        if (((BaseFragment) DialogsActivity.this).actionBar.isActionModeShowed(null)) {
                            TLRPC$Dialog tLRPC$Dialog2 = DialogsActivity.this.getMessagesController().dialogs_dict.get(dialogId);
                            if (!DialogsActivity.this.allowMoving || tLRPC$Dialog2 == null || !DialogsActivity.this.isDialogPinned(tLRPC$Dialog2) || DialogObject.isFolderDialogId(dialogId)) {
                                return 0;
                            }
                            DialogsActivity.this.movingView = (DialogCell) viewHolder.itemView;
                            DialogsActivity.this.movingView.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
                            this.swipeFolderBack = false;
                            return ItemTouchHelper.Callback.makeMovementFlags(3, 0);
                        } else if (!(DialogsActivity.this.filterTabsView != null && DialogsActivity.this.filterTabsView.getVisibility() == 0 && SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) == 5) && DialogsActivity.this.allowSwipeDuringCurrentTouch && (!((dialogId == DialogsActivity.this.getUserConfig().clientUserId || dialogId == 777000) && SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) == 2) && (!DialogsActivity.this.getMessagesController().isPromoDialog(dialogId, false) || DialogsActivity.this.getMessagesController().promoDialogType == MessagesController.PROMO_TYPE_PSA))) {
                            boolean z = DialogsActivity.this.folderId == 0 && (SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) == 3 || SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) == 1 || SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) == 0 || SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) == 4);
                            if (SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) == 1) {
                                if (DialogsActivity.this.viewPages[0].dialogsType == 7 || DialogsActivity.this.viewPages[0].dialogsType == 8) {
                                    dialogFilter = DialogsActivity.this.getMessagesController().selectedDialogFilter[DialogsActivity.this.viewPages[0].dialogsType == 8 ? (char) 1 : (char) 0];
                                }
                                if (!(dialogFilter == null || (dialogFilter.flags & MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_READ) == 0 || (tLRPC$Dialog = DialogsActivity.this.getMessagesController().dialogs_dict.get(dialogId)) == null || dialogFilter.alwaysShow(((BaseFragment) DialogsActivity.this).currentAccount, tLRPC$Dialog) || (tLRPC$Dialog.unread_count <= 0 && !tLRPC$Dialog.unread_mark))) {
                                    z = false;
                                }
                            }
                            this.swipeFolderBack = false;
                            this.swipingFolder = (z && !DialogObject.isFolderDialogId(dialogCell.getDialogId())) || (SharedConfig.archiveHidden && DialogObject.isFolderDialogId(dialogCell.getDialogId()));
                            dialogCell.setSliding(true);
                            return ItemTouchHelper.Callback.makeMovementFlags(0, 4);
                        }
                    }
                }
            }
            return 0;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            long dialogId;
            TLRPC$Dialog tLRPC$Dialog;
            View view = viewHolder2.itemView;
            char c = 0;
            if (!(view instanceof DialogCell) || (tLRPC$Dialog = DialogsActivity.this.getMessagesController().dialogs_dict.get((dialogId = ((DialogCell) view).getDialogId()))) == null || !DialogsActivity.this.isDialogPinned(tLRPC$Dialog) || DialogObject.isFolderDialogId(dialogId)) {
                return false;
            }
            this.parentPage.dialogsAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), viewHolder2.getAdapterPosition());
            DialogsActivity.this.updateDialogIndices();
            if (DialogsActivity.this.viewPages[0].dialogsType == 7 || DialogsActivity.this.viewPages[0].dialogsType == 8) {
                MessagesController.DialogFilter[] dialogFilterArr = DialogsActivity.this.getMessagesController().selectedDialogFilter;
                if (DialogsActivity.this.viewPages[0].dialogsType == 8) {
                    c = 1;
                }
                MessagesController.DialogFilter dialogFilter = dialogFilterArr[c];
                if (!DialogsActivity.this.movingDialogFilters.contains(dialogFilter)) {
                    DialogsActivity.this.movingDialogFilters.add(dialogFilter);
                }
            } else {
                DialogsActivity.this.movingWas = true;
            }
            return true;
        }

        @Override
        public int convertToAbsoluteDirection(int i, int i2) {
            if (this.swipeFolderBack) {
                return 0;
            }
            return super.convertToAbsoluteDirection(i, i2);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder != null) {
                DialogCell dialogCell = (DialogCell) viewHolder.itemView;
                long dialogId = dialogCell.getDialogId();
                int i2 = 0;
                if (DialogObject.isFolderDialogId(dialogId)) {
                    this.parentPage.listView.toggleArchiveHidden(false, dialogCell);
                    return;
                }
                final TLRPC$Dialog tLRPC$Dialog = DialogsActivity.this.getMessagesController().dialogs_dict.get(dialogId);
                if (tLRPC$Dialog != null) {
                    if (!DialogsActivity.this.getMessagesController().isPromoDialog(dialogId, false) && DialogsActivity.this.folderId == 0 && SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) == 1) {
                        ArrayList arrayList = new ArrayList();
                        arrayList.add(Long.valueOf(dialogId));
                        DialogsActivity dialogsActivity = DialogsActivity.this;
                        if (tLRPC$Dialog.unread_count > 0 || tLRPC$Dialog.unread_mark) {
                            i2 = 1;
                        }
                        dialogsActivity.canReadCount = i2;
                        DialogsActivity.this.performSelectedDialogsAction(arrayList, FileLoader.MEDIA_DIR_VIDEO_PUBLIC, true);
                        return;
                    }
                    DialogsActivity.this.slidingView = dialogCell;
                    final int adapterPosition = viewHolder.getAdapterPosition();
                    final int itemCount = this.parentPage.dialogsAdapter.getItemCount();
                    Runnable dialogsActivity$SwipeController$$ExternalSyntheticLambda2 = new Runnable() {
                        @Override
                        public final void run() {
                            DialogsActivity.SwipeController.this.lambda$onSwiped$1(tLRPC$Dialog, itemCount, adapterPosition);
                        }
                    };
                    DialogsActivity.this.setDialogsListFrozen(true);
                    if (Utilities.random.nextInt(1000) == 1) {
                        if (DialogsActivity.this.pacmanAnimation == null) {
                            DialogsActivity.this.pacmanAnimation = new PacmanAnimation(this.parentPage.listView);
                        }
                        DialogsActivity.this.pacmanAnimation.setFinishRunnable(dialogsActivity$SwipeController$$ExternalSyntheticLambda2);
                        DialogsActivity.this.pacmanAnimation.start();
                        return;
                    }
                    dialogsActivity$SwipeController$$ExternalSyntheticLambda2.run();
                    return;
                }
                return;
            }
            DialogsActivity.this.slidingView = null;
        }

        public void lambda$onSwiped$1(final TLRPC$Dialog tLRPC$Dialog, int i, int i2) {
            RecyclerView.ViewHolder findViewHolderForAdapterPosition;
            if (DialogsActivity.this.frozenDialogsList != null) {
                DialogsActivity.this.frozenDialogsList.remove(tLRPC$Dialog);
                final int i3 = tLRPC$Dialog.pinnedNum;
                DialogsActivity.this.slidingView = null;
                this.parentPage.listView.invalidate();
                int findLastVisibleItemPosition = this.parentPage.layoutManager.findLastVisibleItemPosition();
                if (findLastVisibleItemPosition == i - 1) {
                    this.parentPage.layoutManager.findViewByPosition(findLastVisibleItemPosition).requestLayout();
                }
                boolean z = false;
                if (DialogsActivity.this.getMessagesController().isPromoDialog(tLRPC$Dialog.id, false)) {
                    DialogsActivity.this.getMessagesController().hidePromoDialog();
                    this.parentPage.dialogsItemAnimator.prepareForRemove();
                    ViewPage.access$10810(this.parentPage);
                    this.parentPage.dialogsAdapter.notifyItemRemoved(i2);
                    DialogsActivity.this.dialogRemoveFinished = 2;
                    return;
                }
                int addDialogToFolder = DialogsActivity.this.getMessagesController().addDialogToFolder(tLRPC$Dialog.id, DialogsActivity.this.folderId == 0 ? 1 : 0, -1, 0L);
                if (!(addDialogToFolder == 2 && i2 == 0)) {
                    this.parentPage.dialogsItemAnimator.prepareForRemove();
                    ViewPage.access$10810(this.parentPage);
                    this.parentPage.dialogsAdapter.notifyItemRemoved(i2);
                    DialogsActivity.this.dialogRemoveFinished = 2;
                }
                if (DialogsActivity.this.folderId == 0) {
                    if (addDialogToFolder == 2) {
                        this.parentPage.dialogsItemAnimator.prepareForRemove();
                        if (i2 == 0) {
                            DialogsActivity.this.dialogChangeFinished = 2;
                            DialogsActivity.this.setDialogsListFrozen(true);
                            this.parentPage.dialogsAdapter.notifyItemChanged(0);
                        } else {
                            ViewPage.access$10808(this.parentPage);
                            this.parentPage.dialogsAdapter.notifyItemInserted(0);
                            if (!SharedConfig.archiveHidden && this.parentPage.layoutManager.findFirstVisibleItemPosition() == 0) {
                                DialogsActivity.this.disableActionBarScrolling = true;
                                this.parentPage.listView.smoothScrollBy(0, -AndroidUtilities.dp(SharedConfig.useThreeLinesLayout ? 78.0f : 72.0f));
                            }
                        }
                        DialogsActivity dialogsActivity = DialogsActivity.this;
                        DialogsActivity.this.frozenDialogsList.add(0, dialogsActivity.getDialogsArray(((BaseFragment) dialogsActivity).currentAccount, this.parentPage.dialogsType, DialogsActivity.this.folderId, false).get(0));
                    } else if (addDialogToFolder == 1 && (findViewHolderForAdapterPosition = this.parentPage.listView.findViewHolderForAdapterPosition(0)) != null) {
                        View view = findViewHolderForAdapterPosition.itemView;
                        if (view instanceof DialogCell) {
                            DialogCell dialogCell = (DialogCell) view;
                            dialogCell.checkCurrentDialogIndex(true);
                            dialogCell.animateArchiveAvatar();
                        }
                    }
                    SharedPreferences globalMainSettings = MessagesController.getGlobalMainSettings();
                    if (globalMainSettings.getBoolean("archivehint_l", false) || SharedConfig.archiveHidden) {
                        z = true;
                    }
                    if (!z) {
                        globalMainSettings.edit().putBoolean("archivehint_l", true).commit();
                    }
                    DialogsActivity.this.getUndoView().showWithAction(tLRPC$Dialog.id, z ? 2 : 3, null, new Runnable() {
                        @Override
                        public final void run() {
                            DialogsActivity.SwipeController.this.lambda$onSwiped$0(tLRPC$Dialog, i3);
                        }
                    });
                }
                if (DialogsActivity.this.folderId != 0 && DialogsActivity.this.frozenDialogsList.isEmpty()) {
                    this.parentPage.listView.setEmptyView(null);
                    this.parentPage.progressView.setVisibility(4);
                }
            }
        }

        public void lambda$onSwiped$0(TLRPC$Dialog tLRPC$Dialog, int i) {
            DialogsActivity.this.dialogsListFrozen = true;
            DialogsActivity.this.getMessagesController().addDialogToFolder(tLRPC$Dialog.id, 0, i, 0L);
            DialogsActivity.this.dialogsListFrozen = false;
            ArrayList<TLRPC$Dialog> dialogs = DialogsActivity.this.getMessagesController().getDialogs(0);
            int indexOf = dialogs.indexOf(tLRPC$Dialog);
            if (indexOf >= 0) {
                ArrayList<TLRPC$Dialog> dialogs2 = DialogsActivity.this.getMessagesController().getDialogs(1);
                if (!dialogs2.isEmpty() || indexOf != 1) {
                    DialogsActivity.this.dialogInsertFinished = 2;
                    DialogsActivity.this.setDialogsListFrozen(true);
                    this.parentPage.dialogsItemAnimator.prepareForRemove();
                    ViewPage.access$10808(this.parentPage);
                    this.parentPage.dialogsAdapter.notifyItemInserted(indexOf);
                }
                if (dialogs2.isEmpty()) {
                    dialogs.remove(0);
                    if (indexOf == 1) {
                        DialogsActivity.this.dialogChangeFinished = 2;
                        DialogsActivity.this.setDialogsListFrozen(true);
                        this.parentPage.dialogsAdapter.notifyItemChanged(0);
                        return;
                    }
                    DialogsActivity.this.frozenDialogsList.remove(0);
                    this.parentPage.dialogsItemAnimator.prepareForRemove();
                    ViewPage.access$10810(this.parentPage);
                    this.parentPage.dialogsAdapter.notifyItemRemoved(0);
                    return;
                }
                return;
            }
            this.parentPage.dialogsAdapter.notifyDataSetChanged();
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder != null) {
                this.parentPage.listView.hideSelector(false);
            }
            this.currentItemViewHolder = viewHolder;
            if (viewHolder != null) {
                View view = viewHolder.itemView;
                if (view instanceof DialogCell) {
                    ((DialogCell) view).swipeCanceled = false;
                }
            }
            super.onSelectedChanged(viewHolder, i);
        }

        @Override
        public long getAnimationDuration(RecyclerView recyclerView, int i, float f, float f2) {
            if (i == 4) {
                return 200L;
            }
            if (i == 8 && DialogsActivity.this.movingView != null) {
                final DialogCell dialogCell = DialogsActivity.this.movingView;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public final void run() {
                        dialogCell.setBackgroundDrawable(null);
                    }
                }, this.parentPage.dialogsItemAnimator.getMoveDuration());
                DialogsActivity.this.movingView = null;
            }
            return super.getAnimationDuration(recyclerView, i, f, f2);
        }
    }

    public DialogsActivity(Bundle bundle) {
        super(bundle);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        if (getArguments() != null) {
            this.onlySelect = this.arguments.getBoolean("onlySelect", false);
            this.cantSendToChannels = this.arguments.getBoolean("cantSendToChannels", false);
            this.initialDialogsType = this.arguments.getInt("dialogsType", 0);
            this.selectAlertString = this.arguments.getString("selectAlertString");
            this.selectAlertStringGroup = this.arguments.getString("selectAlertStringGroup");
            this.addToGroupAlertString = this.arguments.getString("addToGroupAlertString");
            this.allowSwitchAccount = this.arguments.getBoolean("allowSwitchAccount");
            this.checkCanWrite = this.arguments.getBoolean("checkCanWrite", true);
            this.afterSignup = this.arguments.getBoolean("afterSignup", false);
            this.folderId = this.arguments.getInt("folderId", 0);
            this.resetDelegate = this.arguments.getBoolean("resetDelegate", true);
            this.messagesCount = this.arguments.getInt("messagesCount", 0);
            this.hasPoll = this.arguments.getInt("hasPoll", 0);
            this.hasInvoice = this.arguments.getBoolean("hasInvoice", false);
            this.showSetPasswordConfirm = this.arguments.getBoolean("showSetPasswordConfirm", this.showSetPasswordConfirm);
            this.arguments.getInt("otherwiseRelogin");
            this.closeFragment = this.arguments.getBoolean("closeFragment", true);
        }
        if (this.initialDialogsType == 0) {
            this.askAboutContacts = MessagesController.getGlobalNotificationsSettings().getBoolean("askAboutContacts", true);
            SharedConfig.loadProxyList();
        }
        if (this.searchString == null) {
            this.currentConnectionState = getConnectionsManager().getConnectionState();
            getNotificationCenter().addObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);
            if (!this.onlySelect) {
                NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.closeSearchByActiveAction);
                NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.proxySettingsChanged);
                getNotificationCenter().addObserver(this, NotificationCenter.filterSettingsUpdated);
                getNotificationCenter().addObserver(this, NotificationCenter.dialogFiltersUpdated);
                getNotificationCenter().addObserver(this, NotificationCenter.dialogsUnreadCounterChanged);
            }
            getNotificationCenter().addObserver(this, NotificationCenter.updateInterfaces);
            getNotificationCenter().addObserver(this, NotificationCenter.encryptedChatUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.contactsDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.appDidLogout);
            getNotificationCenter().addObserver(this, NotificationCenter.openedChatChanged);
            getNotificationCenter().addObserver(this, NotificationCenter.notificationsSettingsUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.messageReceivedByAck);
            getNotificationCenter().addObserver(this, NotificationCenter.messageReceivedByServer);
            getNotificationCenter().addObserver(this, NotificationCenter.messageSendError);
            getNotificationCenter().addObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            getNotificationCenter().addObserver(this, NotificationCenter.replyMessagesDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.reloadHints);
            getNotificationCenter().addObserver(this, NotificationCenter.didUpdateConnectionState);
            getNotificationCenter().addObserver(this, NotificationCenter.onDownloadingFilesChanged);
            getNotificationCenter().addObserver(this, NotificationCenter.needDeleteDialog);
            getNotificationCenter().addObserver(this, NotificationCenter.folderBecomeEmpty);
            getNotificationCenter().addObserver(this, NotificationCenter.newSuggestionsAvailable);
            getNotificationCenter().addObserver(this, NotificationCenter.fileLoaded);
            getNotificationCenter().addObserver(this, NotificationCenter.fileLoadFailed);
            getNotificationCenter().addObserver(this, NotificationCenter.fileLoadProgressChanged);
            getNotificationCenter().addObserver(this, NotificationCenter.dialogsUnreadReactionsCounterChanged);
            getNotificationCenter().addObserver(this, NotificationCenter.forceImportContactsStart);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.appUpdateAvailable);
        }
        getNotificationCenter().addObserver(this, NotificationCenter.messagesDeleted);
        getNotificationCenter().addObserver(this, NotificationCenter.onDatabaseMigration);
        getNotificationCenter().addObserver(this, NotificationCenter.onDatabaseOpened);
        getNotificationCenter().addObserver(this, NotificationCenter.didClearDatabase);
        loadDialogs(getAccountInstance());
        getMessagesController().loadPinnedDialogs(this.folderId, 0L, null);
        if (this.databaseMigrationHint != null && !getMessagesStorage().isDatabaseMigrationInProgress()) {
            View view = this.databaseMigrationHint;
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
            this.databaseMigrationHint = null;
        }
        return true;
    }

    public static void loadDialogs(final AccountInstance accountInstance) {
        int currentAccount = accountInstance.getCurrentAccount();
        if (!dialogsLoaded[currentAccount]) {
            MessagesController messagesController = accountInstance.getMessagesController();
            messagesController.loadGlobalNotificationsSettings();
            messagesController.loadDialogs(0, 0, 100, true);
            messagesController.loadHintDialogs();
            messagesController.loadUserInfo(accountInstance.getUserConfig().getCurrentUser(), false, 0);
            accountInstance.getContactsController().checkInviteText();
            accountInstance.getMediaDataController().loadRecents(2, false, true, false);
            accountInstance.getMediaDataController().loadRecents(3, false, true, false);
            accountInstance.getMediaDataController().checkFeaturedStickers();
            accountInstance.getMediaDataController().checkReactions();
            accountInstance.getMediaDataController().checkMenuBots();
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public final void run() {
                    DialogsActivity.lambda$loadDialogs$1(AccountInstance.this);
                }
            }, 200L);
            Iterator<String> it = messagesController.diceEmojies.iterator();
            while (it.hasNext()) {
                accountInstance.getMediaDataController().loadStickersByEmojiOrName(it.next(), true, true);
            }
            dialogsLoaded[currentAccount] = true;
        }
    }

    public static void lambda$loadDialogs$1(AccountInstance accountInstance) {
        accountInstance.getDownloadController().loadDownloadingFiles();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (this.searchString == null) {
            getNotificationCenter().removeObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiLoaded);
            if (!this.onlySelect) {
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.closeSearchByActiveAction);
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.proxySettingsChanged);
                getNotificationCenter().removeObserver(this, NotificationCenter.filterSettingsUpdated);
                getNotificationCenter().removeObserver(this, NotificationCenter.dialogFiltersUpdated);
                getNotificationCenter().removeObserver(this, NotificationCenter.dialogsUnreadCounterChanged);
            }
            getNotificationCenter().removeObserver(this, NotificationCenter.updateInterfaces);
            getNotificationCenter().removeObserver(this, NotificationCenter.encryptedChatUpdated);
            getNotificationCenter().removeObserver(this, NotificationCenter.contactsDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.appDidLogout);
            getNotificationCenter().removeObserver(this, NotificationCenter.openedChatChanged);
            getNotificationCenter().removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
            getNotificationCenter().removeObserver(this, NotificationCenter.messageReceivedByAck);
            getNotificationCenter().removeObserver(this, NotificationCenter.messageReceivedByServer);
            getNotificationCenter().removeObserver(this, NotificationCenter.messageSendError);
            getNotificationCenter().removeObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            getNotificationCenter().removeObserver(this, NotificationCenter.replyMessagesDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.reloadHints);
            getNotificationCenter().removeObserver(this, NotificationCenter.didUpdateConnectionState);
            getNotificationCenter().removeObserver(this, NotificationCenter.onDownloadingFilesChanged);
            getNotificationCenter().removeObserver(this, NotificationCenter.needDeleteDialog);
            getNotificationCenter().removeObserver(this, NotificationCenter.folderBecomeEmpty);
            getNotificationCenter().removeObserver(this, NotificationCenter.newSuggestionsAvailable);
            getNotificationCenter().removeObserver(this, NotificationCenter.fileLoaded);
            getNotificationCenter().removeObserver(this, NotificationCenter.fileLoadFailed);
            getNotificationCenter().removeObserver(this, NotificationCenter.fileLoadProgressChanged);
            getNotificationCenter().removeObserver(this, NotificationCenter.dialogsUnreadReactionsCounterChanged);
            getNotificationCenter().removeObserver(this, NotificationCenter.forceImportContactsStart);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.appUpdateAvailable);
        }
        getNotificationCenter().removeObserver(this, NotificationCenter.messagesDeleted);
        getNotificationCenter().removeObserver(this, NotificationCenter.onDatabaseMigration);
        getNotificationCenter().removeObserver(this, NotificationCenter.onDatabaseOpened);
        getNotificationCenter().removeObserver(this, NotificationCenter.didClearDatabase);
        ChatActivityEnterView chatActivityEnterView = this.commentView;
        if (chatActivityEnterView != null) {
            chatActivityEnterView.onDestroy();
        }
        UndoView[] undoViewArr = this.undoView;
        if (undoViewArr[0] != null) {
            undoViewArr[0].hide(true, 0);
        }
        getNotificationCenter().onAnimationFinish(this.animationIndex);
        this.delegate = null;
        SuggestClearDatabaseBottomSheet.dismissDialog();
    }

    @Override
    public ActionBar createActionBar(Context context) {
        ActionBar actionBar = new ActionBar(context) {
            @Override
            public void setTranslationY(float f) {
                if (!(f == getTranslationY() || ((BaseFragment) DialogsActivity.this).fragmentView == null)) {
                    ((BaseFragment) DialogsActivity.this).fragmentView.invalidate();
                }
                super.setTranslationY(f);
            }

            @Override
            public boolean shouldClipChild(View view) {
                return super.shouldClipChild(view) || view == DialogsActivity.this.doneItem;
            }

            @Override
            public boolean drawChild(Canvas canvas, View view, long j) {
                if (!((BaseFragment) DialogsActivity.this).inPreviewMode || DialogsActivity.this.avatarContainer == null || view == DialogsActivity.this.avatarContainer) {
                    return super.drawChild(canvas, view, j);
                }
                return false;
            }
        };
        actionBar.setItemsBackgroundColor(Theme.getColor("actionBarDefaultSelector"), false);
        actionBar.setItemsBackgroundColor(Theme.getColor("actionBarActionModeDefaultSelector"), true);
        actionBar.setItemsColor(Theme.getColor("actionBarDefaultIcon"), false);
        actionBar.setItemsColor(Theme.getColor("actionBarActionModeDefaultIcon"), true);
        if (this.inPreviewMode || (AndroidUtilities.isTablet() && this.folderId != 0)) {
            actionBar.setOccupyStatusBar(false);
        }
        return actionBar;
    }

    @Override
    public android.view.View createView(final android.content.Context r44) {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.createView(android.content.Context):android.view.View");
    }

    public void lambda$createView$3(View view) {
        this.filterTabsView.setIsEditing(false);
        showDoneItem(false);
    }

    public void lambda$createView$4() {
        if (this.initialDialogsType != 10) {
            hideFloatingButton(false);
        }
        scrollToTop();
    }

    public class AnonymousClass6 implements FilterTabsView.FilterTabsViewDelegate {
        public static void lambda$showDeleteAlert$0() {
        }

        AnonymousClass6() {
        }

        private void showDeleteAlert(final MessagesController.DialogFilter dialogFilter) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
            builder.setTitle(LocaleController.getString("FilterDelete", R.string.FilterDelete));
            builder.setMessage(LocaleController.getString("FilterDeleteAlert", R.string.FilterDeleteAlert));
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            builder.setPositiveButton(LocaleController.getString("Delete", R.string.Delete), new DialogInterface.OnClickListener() {
                @Override
                public final void onClick(DialogInterface dialogInterface, int i) {
                    DialogsActivity.AnonymousClass6.this.lambda$showDeleteAlert$2(dialogFilter, dialogInterface, i);
                }
            });
            AlertDialog create = builder.create();
            DialogsActivity.this.showDialog(create);
            TextView textView = (TextView) create.getButton(-1);
            if (textView != null) {
                textView.setTextColor(Theme.getColor("dialogTextRed2"));
            }
        }

        public void lambda$showDeleteAlert$2(MessagesController.DialogFilter dialogFilter, DialogInterface dialogInterface, int i) {
            TLRPC$TL_messages_updateDialogFilter tLRPC$TL_messages_updateDialogFilter = new TLRPC$TL_messages_updateDialogFilter();
            tLRPC$TL_messages_updateDialogFilter.id = dialogFilter.id;
            DialogsActivity.this.getConnectionsManager().sendRequest(tLRPC$TL_messages_updateDialogFilter, DialogsActivity$6$$ExternalSyntheticLambda3.INSTANCE);
            DialogsActivity.this.getMessagesController().removeFilter(dialogFilter);
            DialogsActivity.this.getMessagesStorage().deleteDialogFilter(dialogFilter);
        }

        public static void lambda$showDeleteAlert$1(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
            AndroidUtilities.runOnUIThread(DialogsActivity$6$$ExternalSyntheticLambda2.INSTANCE);
        }

        @Override
        public void onSamePageSelected() {
            DialogsActivity.this.scrollToTop();
        }

        @Override
        public void onPageReorder(int i, int i2) {
            for (int i3 = 0; i3 < DialogsActivity.this.viewPages.length; i3++) {
                if (DialogsActivity.this.viewPages[i3].selectedType == i) {
                    DialogsActivity.this.viewPages[i3].selectedType = i2;
                } else if (DialogsActivity.this.viewPages[i3].selectedType == i2) {
                    DialogsActivity.this.viewPages[i3].selectedType = i;
                }
            }
        }

        @Override
        public void onPageSelected(int i, boolean z) {
            if (DialogsActivity.this.viewPages[0].selectedType != i) {
                ArrayList<MessagesController.DialogFilter> arrayList = DialogsActivity.this.getMessagesController().dialogFilters;
                if (i == Integer.MAX_VALUE || (i >= 0 && i < arrayList.size())) {
                    if (((BaseFragment) DialogsActivity.this).parentLayout != null) {
                        ((BaseFragment) DialogsActivity.this).parentLayout.getDrawerLayoutContainer().setAllowOpenDrawerBySwipe(i == DialogsActivity.this.filterTabsView.getFirstTabId() || SharedConfig.getChatSwipeAction(((BaseFragment) DialogsActivity.this).currentAccount) != 5);
                    }
                    DialogsActivity.this.viewPages[1].selectedType = i;
                    DialogsActivity.this.viewPages[1].setVisibility(0);
                    DialogsActivity.this.viewPages[1].setTranslationX(DialogsActivity.this.viewPages[0].getMeasuredWidth());
                    DialogsActivity.this.showScrollbars(false);
                    DialogsActivity.this.switchToCurrentSelectedMode(true);
                    DialogsActivity.this.animatingForward = z;
                }
            }
        }

        @Override
        public boolean canPerformActions() {
            return !DialogsActivity.this.searching;
        }

        @Override
        public void onPageScrolled(float f) {
            if (f != 1.0f || DialogsActivity.this.viewPages[1].getVisibility() == 0 || DialogsActivity.this.searching) {
                if (DialogsActivity.this.animatingForward) {
                    DialogsActivity.this.viewPages[0].setTranslationX((-f) * DialogsActivity.this.viewPages[0].getMeasuredWidth());
                    DialogsActivity.this.viewPages[1].setTranslationX(DialogsActivity.this.viewPages[0].getMeasuredWidth() - (DialogsActivity.this.viewPages[0].getMeasuredWidth() * f));
                } else {
                    DialogsActivity.this.viewPages[0].setTranslationX(DialogsActivity.this.viewPages[0].getMeasuredWidth() * f);
                    DialogsActivity.this.viewPages[1].setTranslationX((DialogsActivity.this.viewPages[0].getMeasuredWidth() * f) - DialogsActivity.this.viewPages[0].getMeasuredWidth());
                }
                if (f == 1.0f) {
                    ViewPage viewPage = DialogsActivity.this.viewPages[0];
                    DialogsActivity.this.viewPages[0] = DialogsActivity.this.viewPages[1];
                    DialogsActivity.this.viewPages[1] = viewPage;
                    DialogsActivity.this.viewPages[1].setVisibility(8);
                    DialogsActivity.this.showScrollbars(true);
                    DialogsActivity.this.updateCounters(false);
                    DialogsActivity dialogsActivity = DialogsActivity.this;
                    dialogsActivity.checkListLoad(dialogsActivity.viewPages[0]);
                    DialogsActivity.this.viewPages[0].dialogsAdapter.resume();
                    DialogsActivity.this.viewPages[1].dialogsAdapter.pause();
                }
            }
        }

        @Override
        public int getTabCounter(int i) {
            if (i == Integer.MAX_VALUE) {
                return DialogsActivity.this.getMessagesStorage().getMainUnreadCount();
            }
            ArrayList<MessagesController.DialogFilter> arrayList = DialogsActivity.this.getMessagesController().dialogFilters;
            if (i < 0 || i >= arrayList.size()) {
                return 0;
            }
            return DialogsActivity.this.getMessagesController().dialogFilters.get(i).unreadCount;
        }

        @Override
        public boolean didSelectTab(FilterTabsView.TabView tabView, boolean z) {
            ScrollView scrollView;
            if (((BaseFragment) DialogsActivity.this).actionBar.isActionModeShowed()) {
                return false;
            }
            MessagesController.DialogFilter dialogFilter = null;
            if (DialogsActivity.this.scrimPopupWindow != null) {
                DialogsActivity.this.scrimPopupWindow.dismiss();
                DialogsActivity.this.scrimPopupWindow = null;
                DialogsActivity.this.scrimPopupWindowItems = null;
                return false;
            }
            final Rect rect = new Rect();
            if (tabView.getId() != Integer.MAX_VALUE) {
                dialogFilter = DialogsActivity.this.getMessagesController().dialogFilters.get(tabView.getId());
            }
            final MessagesController.DialogFilter dialogFilter2 = dialogFilter;
            ActionBarPopupWindow.ActionBarPopupWindowLayout actionBarPopupWindowLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(DialogsActivity.this.getParentActivity());
            actionBarPopupWindowLayout.setOnTouchListener(new View.OnTouchListener() {
                private int[] pos = new int[2];

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getActionMasked() == 0) {
                        if (DialogsActivity.this.scrimPopupWindow != null && DialogsActivity.this.scrimPopupWindow.isShowing()) {
                            View contentView = DialogsActivity.this.scrimPopupWindow.getContentView();
                            contentView.getLocationInWindow(this.pos);
                            Rect rect2 = rect;
                            int[] iArr = this.pos;
                            rect2.set(iArr[0], iArr[1], iArr[0] + contentView.getMeasuredWidth(), this.pos[1] + contentView.getMeasuredHeight());
                            if (!rect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
                                DialogsActivity.this.scrimPopupWindow.dismiss();
                            }
                        }
                    } else if (motionEvent.getActionMasked() == 4 && DialogsActivity.this.scrimPopupWindow != null && DialogsActivity.this.scrimPopupWindow.isShowing()) {
                        DialogsActivity.this.scrimPopupWindow.dismiss();
                    }
                    return false;
                }
            });
            actionBarPopupWindowLayout.setDispatchKeyEventListener(new ActionBarPopupWindow.OnDispatchKeyEventListener() {
                @Override
                public final void onDispatchKeyEvent(KeyEvent keyEvent) {
                    DialogsActivity.AnonymousClass6.this.lambda$didSelectTab$3(keyEvent);
                }
            });
            Rect rect2 = new Rect();
            Drawable mutate = DialogsActivity.this.getParentActivity().getResources().getDrawable(R.drawable.popup_fixed_alert).mutate();
            mutate.getPadding(rect2);
            actionBarPopupWindowLayout.setBackgroundDrawable(mutate);
            actionBarPopupWindowLayout.setBackgroundColor(Theme.getColor("actionBarDefaultSubmenuBackground"));
            final LinearLayout linearLayout = new LinearLayout(DialogsActivity.this.getParentActivity());
            if (Build.VERSION.SDK_INT >= 21) {
                scrollView = new ScrollView(this, DialogsActivity.this.getParentActivity(), null, 0, R.style.scrollbarShapeStyle) {
                    @Override
                    protected void onMeasure(int i, int i2) {
                        super.onMeasure(i, i2);
                        setMeasuredDimension(linearLayout.getMeasuredWidth(), getMeasuredHeight());
                    }
                };
            } else {
                scrollView = new ScrollView(DialogsActivity.this.getParentActivity());
            }
            scrollView.setClipToPadding(false);
            actionBarPopupWindowLayout.addView(scrollView, LayoutHelper.createFrame(-2, -2.0f));
            linearLayout.setMinimumWidth(AndroidUtilities.dp(200.0f));
            linearLayout.setOrientation(1);
            final int i = 3;
            DialogsActivity.this.scrimPopupWindowItems = new ActionBarMenuSubItem[3];
            if (tabView.getId() == Integer.MAX_VALUE) {
                i = 2;
            }
            final int i2 = 0;
            while (i2 < i) {
                ActionBarMenuSubItem actionBarMenuSubItem = new ActionBarMenuSubItem(DialogsActivity.this.getParentActivity(), i2 == 0, i2 == i + (-1));
                if (i2 == 0) {
                    if (DialogsActivity.this.getMessagesController().dialogFilters.size() <= 1) {
                        i2++;
                    } else {
                        actionBarMenuSubItem.setTextAndIcon(LocaleController.getString("FilterReorder", R.string.FilterReorder), R.drawable.tabs_reorder);
                    }
                } else if (i2 != 1) {
                    actionBarMenuSubItem.setTextAndIcon(LocaleController.getString("FilterDeleteItem", R.string.FilterDeleteItem), R.drawable.msg_delete);
                } else if (i == 2) {
                    actionBarMenuSubItem.setTextAndIcon(LocaleController.getString("FilterEditAll", R.string.FilterEditAll), R.drawable.msg_edit);
                } else {
                    actionBarMenuSubItem.setTextAndIcon(LocaleController.getString("FilterEdit", R.string.FilterEdit), R.drawable.msg_edit);
                }
                DialogsActivity.this.scrimPopupWindowItems[i2] = actionBarMenuSubItem;
                linearLayout.addView(actionBarMenuSubItem);
                actionBarMenuSubItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public final void onClick(View view) {
                        DialogsActivity.AnonymousClass6.this.lambda$didSelectTab$4(i2, i, dialogFilter2, view);
                    }
                });
                i2++;
            }
            scrollView.addView(linearLayout, LayoutHelper.createScroll(-2, -2, 51));
            DialogsActivity.this.scrimPopupWindow = new ActionBarPopupWindow(actionBarPopupWindowLayout, -2, -2) {
                @Override
                public void dismiss() {
                    super.dismiss();
                    if (DialogsActivity.this.scrimPopupWindow == this) {
                        DialogsActivity.this.scrimPopupWindow = null;
                        DialogsActivity.this.scrimPopupWindowItems = null;
                        if (DialogsActivity.this.scrimAnimatorSet != null) {
                            DialogsActivity.this.scrimAnimatorSet.cancel();
                            DialogsActivity.this.scrimAnimatorSet = null;
                        }
                        DialogsActivity.this.scrimAnimatorSet = new AnimatorSet();
                        ArrayList arrayList = new ArrayList();
                        arrayList.add(ObjectAnimator.ofInt(DialogsActivity.this.scrimPaint, AnimationProperties.PAINT_ALPHA, 0));
                        DialogsActivity.this.scrimAnimatorSet.playTogether(arrayList);
                        DialogsActivity.this.scrimAnimatorSet.setDuration(220L);
                        DialogsActivity.this.scrimAnimatorSet.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animator) {
                                if (DialogsActivity.this.scrimView != null) {
                                    DialogsActivity.this.scrimView.setBackground(null);
                                    DialogsActivity.this.scrimView = null;
                                }
                                if (((BaseFragment) DialogsActivity.this).fragmentView != null) {
                                    ((BaseFragment) DialogsActivity.this).fragmentView.invalidate();
                                }
                            }
                        });
                        DialogsActivity.this.scrimAnimatorSet.start();
                        if (Build.VERSION.SDK_INT >= 19) {
                            DialogsActivity.this.getParentActivity().getWindow().getDecorView().setImportantForAccessibility(0);
                        }
                    }
                }
            };
            tabView.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(6.0f), 0, Theme.getColor("actionBarDefault")));
            DialogsActivity.this.scrimPopupWindow.setDismissAnimationDuration(220);
            DialogsActivity.this.scrimPopupWindow.setOutsideTouchable(true);
            DialogsActivity.this.scrimPopupWindow.setClippingEnabled(true);
            DialogsActivity.this.scrimPopupWindow.setAnimationStyle(R.style.PopupContextAnimation);
            DialogsActivity.this.scrimPopupWindow.setFocusable(true);
            actionBarPopupWindowLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000.0f), Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000.0f), Integer.MIN_VALUE));
            DialogsActivity.this.scrimPopupWindow.setInputMethodMode(2);
            DialogsActivity.this.scrimPopupWindow.setSoftInputMode(0);
            DialogsActivity.this.scrimPopupWindow.getContentView().setFocusableInTouchMode(true);
            tabView.getLocationInWindow(DialogsActivity.this.scrimViewLocation);
            int dp = (DialogsActivity.this.scrimViewLocation[0] + rect2.left) - AndroidUtilities.dp(16.0f);
            if (dp < AndroidUtilities.dp(6.0f)) {
                dp = AndroidUtilities.dp(6.0f);
            } else if (dp > (((BaseFragment) DialogsActivity.this).fragmentView.getMeasuredWidth() - AndroidUtilities.dp(6.0f)) - actionBarPopupWindowLayout.getMeasuredWidth()) {
                dp = (((BaseFragment) DialogsActivity.this).fragmentView.getMeasuredWidth() - AndroidUtilities.dp(6.0f)) - actionBarPopupWindowLayout.getMeasuredWidth();
            }
            DialogsActivity.this.scrimPopupWindow.showAtLocation(((BaseFragment) DialogsActivity.this).fragmentView, 51, dp, (DialogsActivity.this.scrimViewLocation[1] + tabView.getMeasuredHeight()) - AndroidUtilities.dp(12.0f));
            DialogsActivity.this.scrimView = tabView;
            DialogsActivity.this.scrimViewSelected = z;
            ((BaseFragment) DialogsActivity.this).fragmentView.invalidate();
            if (DialogsActivity.this.scrimAnimatorSet != null) {
                DialogsActivity.this.scrimAnimatorSet.cancel();
            }
            DialogsActivity.this.scrimAnimatorSet = new AnimatorSet();
            ArrayList arrayList = new ArrayList();
            arrayList.add(ObjectAnimator.ofInt(DialogsActivity.this.scrimPaint, AnimationProperties.PAINT_ALPHA, 0, 50));
            DialogsActivity.this.scrimAnimatorSet.playTogether(arrayList);
            DialogsActivity.this.scrimAnimatorSet.setDuration(150L);
            DialogsActivity.this.scrimAnimatorSet.start();
            return true;
        }

        public void lambda$didSelectTab$3(KeyEvent keyEvent) {
            if (keyEvent.getKeyCode() == 4 && keyEvent.getRepeatCount() == 0 && DialogsActivity.this.scrimPopupWindow != null && DialogsActivity.this.scrimPopupWindow.isShowing()) {
                DialogsActivity.this.scrimPopupWindow.dismiss();
            }
        }

        public void lambda$didSelectTab$4(int i, int i2, MessagesController.DialogFilter dialogFilter, View view) {
            if (i == 0) {
                DialogsActivity.this.resetScroll();
                DialogsActivity.this.filterTabsView.setIsEditing(true);
                DialogsActivity.this.showDoneItem(true);
            } else if (i == 1) {
                if (i2 == 2) {
                    DialogsActivity.this.presentFragment(new FiltersSetupActivity());
                } else {
                    DialogsActivity.this.presentFragment(new FilterCreateActivity(dialogFilter));
                }
            } else if (i == 2) {
                showDeleteAlert(dialogFilter);
            }
            if (DialogsActivity.this.scrimPopupWindow != null) {
                DialogsActivity.this.scrimPopupWindow.dismiss();
            }
        }

        @Override
        public boolean isTabMenuVisible() {
            return DialogsActivity.this.scrimPopupWindow != null && DialogsActivity.this.scrimPopupWindow.isShowing();
        }

        @Override
        public void onDeletePressed(int i) {
            showDeleteAlert(DialogsActivity.this.getMessagesController().dialogFilters.get(i));
        }
    }

    class AnonymousClass9 extends LinearLayoutManager {
        private boolean fixOffset;
        final ViewPage val$viewPage;

        AnonymousClass9(Context context, ViewPage viewPage) {
            super(context);
            this.val$viewPage = viewPage;
        }

        @Override
        public void scrollToPositionWithOffset(int i, int i2) {
            if (this.fixOffset) {
                i2 -= this.val$viewPage.listView.getPaddingTop();
            }
            super.scrollToPositionWithOffset(i, i2);
        }

        @Override
        public void prepareForDrop(View view, View view2, int i, int i2) {
            this.fixOffset = true;
            super.prepareForDrop(view, view2, i, i2);
            this.fixOffset = false;
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int i) {
            if (!DialogsActivity.this.hasHiddenArchive() || i != 1) {
                LinearSmoothScrollerCustom linearSmoothScrollerCustom = new LinearSmoothScrollerCustom(recyclerView.getContext(), 0);
                linearSmoothScrollerCustom.setTargetPosition(i);
                startSmoothScroll(linearSmoothScrollerCustom);
                return;
            }
            super.smoothScrollToPosition(recyclerView, state, i);
        }

        @Override
        public int scrollVerticallyBy(int r18, androidx.recyclerview.widget.RecyclerView.Recycler r19, androidx.recyclerview.widget.RecyclerView.State r20) {
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.AnonymousClass9.scrollVerticallyBy(int, androidx.recyclerview.widget.RecyclerView$Recycler, androidx.recyclerview.widget.RecyclerView$State):int");
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            if (BuildVars.DEBUG_PRIVATE_VERSION) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException unused) {
                    throw new RuntimeException("Inconsistency detected. dialogsListIsFrozen=" + DialogsActivity.this.dialogsListFrozen + " lastUpdateAction=" + DialogsActivity.this.debugLastUpdateAction);
                }
            } else {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    FileLog.e(e);
                    final ViewPage viewPage = this.val$viewPage;
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public final void run() {
                            DialogsActivity.AnonymousClass9.lambda$onLayoutChildren$0(DialogsActivity.ViewPage.this);
                        }
                    });
                }
            }
        }

        public static void lambda$onLayoutChildren$0(ViewPage viewPage) {
            viewPage.dialogsAdapter.notifyDataSetChanged();
        }
    }

    public void lambda$createView$5(ViewPage viewPage, View view, int i) {
        int i2 = this.initialDialogsType;
        if (i2 == 10) {
            onItemLongClick(view, i, 0.0f, 0.0f, viewPage.dialogsType, viewPage.dialogsAdapter);
        } else if ((i2 == 11 || i2 == 13) && i == 1) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("forImport", true);
            bundle.putLongArray("result", new long[]{getUserConfig().getClientUserId()});
            bundle.putInt("chatType", 4);
            String string = this.arguments.getString("importTitle");
            if (string != null) {
                bundle.putString("title", string);
            }
            GroupCreateFinalActivity groupCreateFinalActivity = new GroupCreateFinalActivity(bundle);
            groupCreateFinalActivity.setDelegate(new GroupCreateFinalActivity.GroupCreateFinalActivityDelegate() {
                @Override
                public void didFailChatCreation() {
                }

                @Override
                public void didStartChatCreation() {
                }

                @Override
                public void didFinishChatCreation(GroupCreateFinalActivity groupCreateFinalActivity2, long j) {
                    ArrayList<Long> arrayList = new ArrayList<>();
                    arrayList.add(Long.valueOf(-j));
                    DialogsActivityDelegate dialogsActivityDelegate = DialogsActivity.this.delegate;
                    if (DialogsActivity.this.closeFragment) {
                        DialogsActivity.this.removeSelfFromStack();
                    }
                    dialogsActivityDelegate.didSelectDialogs(DialogsActivity.this, arrayList, null, true);
                }
            });
            presentFragment(groupCreateFinalActivity);
        } else {
            onItemClick(view, i, viewPage.dialogsAdapter);
        }
    }

    public class AnonymousClass16 implements DialogsSearchAdapter.DialogsSearchAdapterDelegate {
        AnonymousClass16() {
        }

        @Override
        public void searchStateChanged(boolean z, boolean z2) {
            if (DialogsActivity.this.searchViewPager.emptyView.getVisibility() == 0) {
                z2 = true;
            }
            if (DialogsActivity.this.searching && DialogsActivity.this.searchWas && DialogsActivity.this.searchViewPager.emptyView != null) {
                if (z || DialogsActivity.this.searchViewPager.dialogsSearchAdapter.getItemCount() != 0) {
                    DialogsActivity.this.searchViewPager.emptyView.showProgress(true, z2);
                } else {
                    DialogsActivity.this.searchViewPager.emptyView.showProgress(false, z2);
                }
            }
            if (z && DialogsActivity.this.searchViewPager.dialogsSearchAdapter.getItemCount() == 0) {
                DialogsActivity.this.searchViewPager.cancelEnterAnimation();
            }
        }

        @Override
        public void didPressedOnSubDialog(long j) {
            if (!DialogsActivity.this.onlySelect) {
                Bundle bundle = new Bundle();
                if (DialogObject.isUserDialog(j)) {
                    bundle.putLong("user_id", j);
                } else {
                    bundle.putLong("chat_id", -j);
                }
                DialogsActivity.this.closeSearch();
                if (AndroidUtilities.isTablet() && DialogsActivity.this.viewPages != null) {
                    for (int i = 0; i < DialogsActivity.this.viewPages.length; i++) {
                        DialogsActivity.this.viewPages[i].dialogsAdapter.setOpenedDialogId(DialogsActivity.this.openedDialogId = j);
                    }
                    DialogsActivity.this.updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                }
                if (DialogsActivity.this.searchString != null) {
                    if (DialogsActivity.this.getMessagesController().checkCanOpenChat(bundle, DialogsActivity.this)) {
                        DialogsActivity.this.getNotificationCenter().postNotificationName(NotificationCenter.closeChats, new Object[0]);
                        DialogsActivity.this.presentFragment(new ChatActivity(bundle));
                    }
                } else if (DialogsActivity.this.getMessagesController().checkCanOpenChat(bundle, DialogsActivity.this)) {
                    DialogsActivity.this.presentFragment(new ChatActivity(bundle));
                }
            } else if (DialogsActivity.this.validateSlowModeDialog(j)) {
                if (!DialogsActivity.this.selectedDialogs.isEmpty()) {
                    DialogsActivity.this.findAndUpdateCheckBox(j, DialogsActivity.this.addOrRemoveSelectedDialog(j, null));
                    DialogsActivity.this.updateSelectedCount();
                    ((BaseFragment) DialogsActivity.this).actionBar.closeSearchField();
                    return;
                }
                DialogsActivity.this.didSelectResult(j, true, false);
            }
        }

        @Override
        public void needRemoveHint(final long j) {
            TLRPC$User user;
            if (DialogsActivity.this.getParentActivity() != null && (user = DialogsActivity.this.getMessagesController().getUser(Long.valueOf(j))) != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
                builder.setTitle(LocaleController.getString("ChatHintsDeleteAlertTitle", R.string.ChatHintsDeleteAlertTitle));
                builder.setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("ChatHintsDeleteAlert", R.string.ChatHintsDeleteAlert, ContactsController.formatName(user.first_name, user.last_name))));
                builder.setPositiveButton(LocaleController.getString("StickersRemove", R.string.StickersRemove), new DialogInterface.OnClickListener() {
                    @Override
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        DialogsActivity.AnonymousClass16.this.lambda$needRemoveHint$0(j, dialogInterface, i);
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                AlertDialog create = builder.create();
                DialogsActivity.this.showDialog(create);
                TextView textView = (TextView) create.getButton(-1);
                if (textView != null) {
                    textView.setTextColor(Theme.getColor("dialogTextRed2"));
                }
            }
        }

        public void lambda$needRemoveHint$0(long j, DialogInterface dialogInterface, int i) {
            DialogsActivity.this.getMediaDataController().removePeer(j);
        }

        @Override
        public void needClearList() {
            AlertDialog.Builder builder = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
            builder.setTitle(LocaleController.getString("ClearSearchAlertTitle", R.string.ClearSearchAlertTitle));
            builder.setMessage(LocaleController.getString("ClearSearchAlert", R.string.ClearSearchAlert));
            builder.setPositiveButton(LocaleController.getString("ClearButton", R.string.ClearButton).toUpperCase(), new DialogInterface.OnClickListener() {
                @Override
                public final void onClick(DialogInterface dialogInterface, int i) {
                    DialogsActivity.AnonymousClass16.this.lambda$needClearList$1(dialogInterface, i);
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            AlertDialog create = builder.create();
            DialogsActivity.this.showDialog(create);
            TextView textView = (TextView) create.getButton(-1);
            if (textView != null) {
                textView.setTextColor(Theme.getColor("dialogTextRed2"));
            }
        }

        public void lambda$needClearList$1(DialogInterface dialogInterface, int i) {
            if (DialogsActivity.this.searchViewPager.dialogsSearchAdapter.isRecentSearchDisplayed()) {
                DialogsActivity.this.searchViewPager.dialogsSearchAdapter.clearRecentSearch();
            } else {
                DialogsActivity.this.searchViewPager.dialogsSearchAdapter.clearRecentHashtags();
            }
        }

        @Override
        public void runResultsEnterAnimation() {
            if (DialogsActivity.this.searchViewPager != null) {
                DialogsActivity.this.searchViewPager.runResultsEnterAnimation();
            }
        }

        @Override
        public boolean isSelected(long j) {
            return DialogsActivity.this.selectedDialogs.contains(Long.valueOf(j));
        }
    }

    public void lambda$createView$6(View view, int i) {
        if (this.initialDialogsType == 10) {
            onItemLongClick(view, i, 0.0f, 0.0f, -1, this.searchViewPager.dialogsSearchAdapter);
        } else {
            onItemClick(view, i, this.searchViewPager.dialogsSearchAdapter);
        }
    }

    public void lambda$createView$7(boolean z, ArrayList arrayList, ArrayList arrayList2, boolean z2) {
        updateFiltersView(z, arrayList, arrayList2, z2, true);
    }

    public void lambda$createView$8(View view, int i) {
        this.filtersView.cancelClickRunnables(true);
        addSearchFilter(this.filtersView.getFilterAt(i));
    }

    public void lambda$createView$9(View view) {
        if (this.initialDialogsType == 10) {
            if (this.delegate != null && !this.selectedDialogs.isEmpty()) {
                this.delegate.didSelectDialogs(this, this.selectedDialogs, null, false);
            }
        } else if (this.floatingButton.getVisibility() == 0) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("destroyAfterSelect", true);
            presentFragment(new ContactsActivity(bundle));
        }
    }

    public void lambda$createView$10(View view) {
        if (this.delegate != null && !this.selectedDialogs.isEmpty()) {
            this.delegate.didSelectDialogs(this, this.selectedDialogs, this.commentView.getFieldText(), false);
        }
    }

    public boolean lambda$createView$11(FrameLayout frameLayout, View view) {
        if (this.isNextButton) {
            return false;
        }
        onSendLongClick(frameLayout);
        return true;
    }

    public void lambda$createView$12(View view) {
        if (SharedConfig.isAppUpdateAvailable()) {
            AndroidUtilities.openForView(SharedConfig.pendingAppUpdate.document, true, getParentActivity());
        }
    }

    public class AnonymousClass26 extends UndoView {
        AnonymousClass26(Context context) {
            super(context);
        }

        @Override
        public void setTranslationY(float f) {
            super.setTranslationY(f);
            if (this == DialogsActivity.this.undoView[0] && DialogsActivity.this.undoView[1].getVisibility() != 0) {
                DialogsActivity.this.additionalFloatingTranslation = (getMeasuredHeight() + AndroidUtilities.dp(8.0f)) - f;
                if (DialogsActivity.this.additionalFloatingTranslation < 0.0f) {
                    DialogsActivity.this.additionalFloatingTranslation = 0.0f;
                }
                if (!DialogsActivity.this.floatingHidden) {
                    DialogsActivity.this.updateFloatingButtonOffset();
                }
            }
        }

        @Override
        protected boolean canUndo() {
            for (int i = 0; i < DialogsActivity.this.viewPages.length; i++) {
                if (DialogsActivity.this.viewPages[i].dialogsItemAnimator.isRunning()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onRemoveDialogAction(long j, int i) {
            if (i == 1 || i == 27) {
                DialogsActivity.this.debugLastUpdateAction = 1;
                DialogsActivity.this.setDialogsListFrozen(true);
                if (DialogsActivity.this.frozenDialogsList != null) {
                    final int i2 = -1;
                    int i3 = 0;
                    while (true) {
                        if (i3 >= DialogsActivity.this.frozenDialogsList.size()) {
                            break;
                        } else if (((TLRPC$Dialog) DialogsActivity.this.frozenDialogsList.get(i3)).id == j) {
                            i2 = i3;
                            break;
                        } else {
                            i3++;
                        }
                    }
                    if (i2 >= 0) {
                        final TLRPC$Dialog tLRPC$Dialog = (TLRPC$Dialog) DialogsActivity.this.frozenDialogsList.remove(i2);
                        DialogsActivity.this.viewPages[0].dialogsAdapter.notifyDataSetChanged();
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public final void run() {
                                DialogsActivity.AnonymousClass26.this.lambda$onRemoveDialogAction$0(i2, tLRPC$Dialog);
                            }
                        });
                        return;
                    }
                    DialogsActivity.this.setDialogsListFrozen(false);
                }
            }
        }

        public void lambda$onRemoveDialogAction$0(int i, TLRPC$Dialog tLRPC$Dialog) {
            if (DialogsActivity.this.frozenDialogsList != null) {
                DialogsActivity.this.frozenDialogsList.add(i, tLRPC$Dialog);
                DialogsActivity.this.viewPages[0].dialogsAdapter.notifyItemInserted(i);
                DialogsActivity.this.dialogInsertFinished = 2;
            }
        }
    }

    private void updateAppUpdateViews(boolean z) {
        boolean z2;
        if (this.updateLayout != null) {
            if (SharedConfig.isAppUpdateAvailable()) {
                FileLoader.getAttachFileName(SharedConfig.pendingAppUpdate.document);
                z2 = getFileLoader().getPathToAttach(SharedConfig.pendingAppUpdate.document, true).exists();
            } else {
                z2 = false;
            }
            if (z2) {
                if (this.updateLayout.getTag() == null) {
                    AnimatorSet animatorSet = this.updateLayoutAnimator;
                    if (animatorSet != null) {
                        animatorSet.cancel();
                    }
                    this.updateLayout.setVisibility(0);
                    this.updateLayout.setTag(1);
                    if (z) {
                        AnimatorSet animatorSet2 = new AnimatorSet();
                        this.updateLayoutAnimator = animatorSet2;
                        animatorSet2.setDuration(180L);
                        this.updateLayoutAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT);
                        this.updateLayoutAnimator.playTogether(ObjectAnimator.ofFloat(this.updateLayout, View.TRANSLATION_Y, 0.0f));
                        this.updateLayoutAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animator) {
                                DialogsActivity.this.updateLayoutAnimator = null;
                            }
                        });
                        this.updateLayoutAnimator.start();
                        return;
                    }
                    this.updateLayout.setTranslationY(0.0f);
                }
            } else if (this.updateLayout.getTag() != null) {
                this.updateLayout.setTag(null);
                if (z) {
                    AnimatorSet animatorSet3 = new AnimatorSet();
                    this.updateLayoutAnimator = animatorSet3;
                    animatorSet3.setDuration(180L);
                    this.updateLayoutAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT);
                    this.updateLayoutAnimator.playTogether(ObjectAnimator.ofFloat(this.updateLayout, View.TRANSLATION_Y, AndroidUtilities.dp(48.0f)));
                    this.updateLayoutAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            if (DialogsActivity.this.updateLayout.getTag() == null) {
                                DialogsActivity.this.updateLayout.setVisibility(4);
                            }
                            DialogsActivity.this.updateLayoutAnimator = null;
                        }
                    });
                    this.updateLayoutAnimator.start();
                    return;
                }
                this.updateLayout.setTranslationY(AndroidUtilities.dp(48.0f));
                this.updateLayout.setVisibility(4);
            }
        }
    }

    public void updateContextViewPosition() {
        FilterTabsView filterTabsView = this.filterTabsView;
        float f = 0.0f;
        float measuredHeight = (filterTabsView == null || filterTabsView.getVisibility() == 8) ? 0.0f : this.filterTabsView.getMeasuredHeight();
        ViewPagerFixed.TabsView tabsView = this.searchTabsView;
        float measuredHeight2 = (tabsView == null || tabsView.getVisibility() == 8) ? 0.0f : this.searchTabsView.getMeasuredHeight();
        if (this.fragmentContextView != null) {
            FragmentContextView fragmentContextView = this.fragmentLocationContextView;
            float dp = (fragmentContextView == null || fragmentContextView.getVisibility() != 0) ? 0.0f : AndroidUtilities.dp(36.0f) + 0.0f;
            FragmentContextView fragmentContextView2 = this.fragmentContextView;
            float topPadding = dp + fragmentContextView2.getTopPadding() + this.actionBar.getTranslationY();
            float f2 = this.searchAnimationProgress;
            fragmentContextView2.setTranslationY(topPadding + ((1.0f - f2) * measuredHeight) + (f2 * measuredHeight2) + this.tabsYOffset);
        }
        if (this.fragmentLocationContextView != null) {
            FragmentContextView fragmentContextView3 = this.fragmentContextView;
            if (fragmentContextView3 != null && fragmentContextView3.getVisibility() == 0) {
                f = 0.0f + AndroidUtilities.dp(this.fragmentContextView.getStyleHeight()) + this.fragmentContextView.getTopPadding();
            }
            FragmentContextView fragmentContextView4 = this.fragmentLocationContextView;
            float topPadding2 = f + fragmentContextView4.getTopPadding() + this.actionBar.getTranslationY();
            float f3 = this.searchAnimationProgress;
            fragmentContextView4.setTranslationY(topPadding2 + (measuredHeight * (1.0f - f3)) + (measuredHeight2 * f3) + this.tabsYOffset);
        }
    }

    public void updateFiltersView(boolean r11, java.util.ArrayList<java.lang.Object> r12, java.util.ArrayList<org.telegram.ui.Adapters.FiltersView.DateData> r13, boolean r14, boolean r15) {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.updateFiltersView(boolean, java.util.ArrayList, java.util.ArrayList, boolean, boolean):void");
    }

    private void addSearchFilter(FiltersView.MediaFilterData mediaFilterData) {
        if (this.searchIsShowed) {
            ArrayList<FiltersView.MediaFilterData> currentSearchFilters = this.searchViewPager.getCurrentSearchFilters();
            if (!currentSearchFilters.isEmpty()) {
                for (int i = 0; i < currentSearchFilters.size(); i++) {
                    if (mediaFilterData.isSameType(currentSearchFilters.get(i))) {
                        return;
                    }
                }
            }
            currentSearchFilters.add(mediaFilterData);
            this.actionBar.setSearchFilter(mediaFilterData);
            this.actionBar.setSearchFieldText("");
            updateFiltersView(true, null, null, false, true);
        }
    }

    private void createActionMode(String str) {
        if (!this.actionBar.actionModeIsExist(str)) {
            ActionBarMenu createActionMode = this.actionBar.createActionMode(false, str);
            createActionMode.setBackgroundColor(0);
            createActionMode.drawBlur = false;
            NumberTextView numberTextView = new NumberTextView(createActionMode.getContext());
            this.selectedDialogsCountTextView = numberTextView;
            numberTextView.setTextSize(18);
            this.selectedDialogsCountTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            this.selectedDialogsCountTextView.setTextColor(Theme.getColor("actionBarActionModeDefaultIcon"));
            createActionMode.addView(this.selectedDialogsCountTextView, LayoutHelper.createLinear(0, -1, 1.0f, 72, 0, 0, 0));
            this.selectedDialogsCountTextView.setOnTouchListener(DialogsActivity$$ExternalSyntheticLambda24.INSTANCE);
            this.pinItem = createActionMode.addItemWithWidth(100, R.drawable.msg_pin, AndroidUtilities.dp(54.0f));
            this.muteItem = createActionMode.addItemWithWidth(104, R.drawable.msg_mute, AndroidUtilities.dp(54.0f));
            this.archive2Item = createActionMode.addItemWithWidth(107, R.drawable.msg_archive, AndroidUtilities.dp(54.0f));
            this.deleteItem = createActionMode.addItemWithWidth(102, R.drawable.msg_delete, AndroidUtilities.dp(54.0f), LocaleController.getString("Delete", R.string.Delete));
            ActionBarMenuItem addItemWithWidth = createActionMode.addItemWithWidth(0, R.drawable.ic_ab_other, AndroidUtilities.dp(54.0f), LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));
            this.archiveItem = addItemWithWidth.addSubItem(105, R.drawable.msg_archive, LocaleController.getString("Archive", R.string.Archive));
            this.pin2Item = addItemWithWidth.addSubItem(108, R.drawable.msg_pin, LocaleController.getString("DialogPin", R.string.DialogPin));
            this.addToFolderItem = addItemWithWidth.addSubItem(109, R.drawable.msg_addfolder, LocaleController.getString("FilterAddTo", R.string.FilterAddTo));
            this.removeFromFolderItem = addItemWithWidth.addSubItem(110, R.drawable.msg_removefolder, LocaleController.getString("FilterRemoveFrom", R.string.FilterRemoveFrom));
            this.readItem = addItemWithWidth.addSubItem(FileLoader.MEDIA_DIR_VIDEO_PUBLIC, R.drawable.msg_markread, LocaleController.getString("MarkAsRead", R.string.MarkAsRead));
            this.clearItem = addItemWithWidth.addSubItem(103, R.drawable.msg_clear, LocaleController.getString("ClearHistory", R.string.ClearHistory));
            this.blockItem = addItemWithWidth.addSubItem(106, R.drawable.msg_block, LocaleController.getString("BlockUser", R.string.BlockUser));
            this.actionModeViews.add(this.pinItem);
            this.actionModeViews.add(this.archive2Item);
            this.actionModeViews.add(this.muteItem);
            this.actionModeViews.add(this.deleteItem);
            this.actionModeViews.add(addItemWithWidth);
            if (str == null) {
                this.actionBar.setActionBarMenuOnItemClick(new AnonymousClass30());
            }
        }
    }

    public class AnonymousClass30 extends ActionBar.ActionBarMenuOnItemClick {
        AnonymousClass30() {
        }

        @Override
        public void onItemClick(int i) {
            if ((i == 201 || i == 200 || i == 202) && DialogsActivity.this.searchViewPager != null) {
                DialogsActivity.this.searchViewPager.onActionBarItemClick(i);
            } else if (i == -1) {
                if (DialogsActivity.this.filterTabsView != null && DialogsActivity.this.filterTabsView.isEditing()) {
                    DialogsActivity.this.filterTabsView.setIsEditing(false);
                    DialogsActivity.this.showDoneItem(false);
                } else if (((BaseFragment) DialogsActivity.this).actionBar.isActionModeShowed()) {
                    if (DialogsActivity.this.searchViewPager == null || DialogsActivity.this.searchViewPager.getVisibility() != 0 || !DialogsActivity.this.searchViewPager.actionModeShowing()) {
                        DialogsActivity.this.hideActionMode(true);
                    } else {
                        DialogsActivity.this.searchViewPager.hideActionMode();
                    }
                } else if (DialogsActivity.this.onlySelect || DialogsActivity.this.folderId != 0) {
                    DialogsActivity.this.finishFragment();
                } else if (((BaseFragment) DialogsActivity.this).parentLayout != null) {
                    ((BaseFragment) DialogsActivity.this).parentLayout.getDrawerLayoutContainer().openDrawer(false);
                }
            } else if (i == 1) {
                if (DialogsActivity.this.getParentActivity() != null) {
                    SharedConfig.appLocked = true;
                    SharedConfig.saveConfig();
                    int[] iArr = new int[2];
                    DialogsActivity.this.passcodeItem.getLocationInWindow(iArr);
                    ((LaunchActivity) DialogsActivity.this.getParentActivity()).showPasscodeActivity(false, true, iArr[0] + (DialogsActivity.this.passcodeItem.getMeasuredWidth() / 2), iArr[1] + (DialogsActivity.this.passcodeItem.getMeasuredHeight() / 2), new Runnable() {
                        @Override
                        public final void run() {
                            DialogsActivity.AnonymousClass30.this.lambda$onItemClick$0();
                        }
                    }, new Runnable() {
                        @Override
                        public final void run() {
                            DialogsActivity.AnonymousClass30.this.lambda$onItemClick$1();
                        }
                    });
                    DialogsActivity.this.updatePasscodeButton();
                }
            } else if (i == 2) {
                DialogsActivity.this.presentFragment(new ProxyListActivity());
            } else if (i == 3) {
                DialogsActivity.this.showSearch(true, true, true);
                ((BaseFragment) DialogsActivity.this).actionBar.openSearchField(true);
            } else if (i < 10 || i >= 13) {
                if (i == 109) {
                    DialogsActivity dialogsActivity = DialogsActivity.this;
                    FiltersListBottomSheet filtersListBottomSheet = new FiltersListBottomSheet(dialogsActivity, dialogsActivity.selectedDialogs);
                    filtersListBottomSheet.setDelegate(new FiltersListBottomSheet.FiltersListBottomSheetDelegate() {
                        @Override
                        public final void didSelectFilter(MessagesController.DialogFilter dialogFilter) {
                            DialogsActivity.AnonymousClass30.this.lambda$onItemClick$2(dialogFilter);
                        }
                    });
                    DialogsActivity.this.showDialog(filtersListBottomSheet);
                } else if (i == 110) {
                    MessagesController.DialogFilter dialogFilter = DialogsActivity.this.getMessagesController().dialogFilters.get(DialogsActivity.this.viewPages[0].selectedType);
                    DialogsActivity dialogsActivity2 = DialogsActivity.this;
                    ArrayList<Long> dialogsCount = FiltersListBottomSheet.getDialogsCount(dialogsActivity2, dialogFilter, dialogsActivity2.selectedDialogs, false, false);
                    if ((dialogFilter != null ? dialogFilter.neverShow.size() : 0) + dialogsCount.size() > 100) {
                        DialogsActivity dialogsActivity3 = DialogsActivity.this;
                        dialogsActivity3.showDialog(AlertsCreator.createSimpleAlert(dialogsActivity3.getParentActivity(), LocaleController.getString("FilterAddToAlertFullTitle", R.string.FilterAddToAlertFullTitle), LocaleController.getString("FilterAddToAlertFullText", R.string.FilterAddToAlertFullText)).create());
                        return;
                    }
                    if (!dialogsCount.isEmpty()) {
                        dialogFilter.neverShow.addAll(dialogsCount);
                        for (int i2 = 0; i2 < dialogsCount.size(); i2++) {
                            Long l = dialogsCount.get(i2);
                            dialogFilter.alwaysShow.remove(l);
                            dialogFilter.pinnedDialogs.delete(l.longValue());
                        }
                        FilterCreateActivity.saveFilterToServer(dialogFilter, dialogFilter.flags, dialogFilter.name, dialogFilter.alwaysShow, dialogFilter.neverShow, dialogFilter.pinnedDialogs, false, false, true, false, false, DialogsActivity.this, null);
                    }
                    DialogsActivity.this.getUndoView().showWithAction(dialogsCount.size() == 1 ? dialogsCount.get(0).longValue() : 0L, 21, Integer.valueOf(dialogsCount.size()), dialogFilter, (Runnable) null, (Runnable) null);
                    DialogsActivity.this.hideActionMode(false);
                } else if (i == 100 || i == 101 || i == 102 || i == 103 || i == 104 || i == 105 || i == 106 || i == 107 || i == 108) {
                    DialogsActivity dialogsActivity4 = DialogsActivity.this;
                    dialogsActivity4.performSelectedDialogsAction(dialogsActivity4.selectedDialogs, i, true);
                }
            } else if (DialogsActivity.this.getParentActivity() != null) {
                DialogsActivityDelegate dialogsActivityDelegate = DialogsActivity.this.delegate;
                LaunchActivity launchActivity = (LaunchActivity) DialogsActivity.this.getParentActivity();
                launchActivity.switchToAccount(i - 10, true);
                DialogsActivity dialogsActivity5 = new DialogsActivity(((BaseFragment) DialogsActivity.this).arguments);
                dialogsActivity5.setDelegate(dialogsActivityDelegate);
                launchActivity.presentFragment(dialogsActivity5, false, true);
            }
        }

        public void lambda$onItemClick$0() {
            DialogsActivity.this.passcodeItem.setAlpha(1.0f);
        }

        public void lambda$onItemClick$1() {
            DialogsActivity.this.passcodeItem.setAlpha(0.0f);
        }

        public void lambda$onItemClick$2(MessagesController.DialogFilter dialogFilter) {
            ArrayList<Long> arrayList;
            long j;
            ArrayList<Long> arrayList2;
            DialogsActivity dialogsActivity = DialogsActivity.this;
            ArrayList<Long> dialogsCount = FiltersListBottomSheet.getDialogsCount(dialogsActivity, dialogFilter, dialogsActivity.selectedDialogs, true, false);
            if ((dialogFilter != null ? dialogFilter.alwaysShow.size() : 0) + dialogsCount.size() > 100) {
                DialogsActivity dialogsActivity2 = DialogsActivity.this;
                dialogsActivity2.showDialog(AlertsCreator.createSimpleAlert(dialogsActivity2.getParentActivity(), LocaleController.getString("FilterAddToAlertFullTitle", R.string.FilterAddToAlertFullTitle), LocaleController.getString("FilterRemoveFromAlertFullText", R.string.FilterRemoveFromAlertFullText)).create());
                return;
            }
            if (dialogFilter != null) {
                if (!dialogsCount.isEmpty()) {
                    for (int i = 0; i < dialogsCount.size(); i++) {
                        dialogFilter.neverShow.remove(dialogsCount.get(i));
                    }
                    dialogFilter.alwaysShow.addAll(dialogsCount);
                    arrayList = dialogsCount;
                    FilterCreateActivity.saveFilterToServer(dialogFilter, dialogFilter.flags, dialogFilter.name, dialogFilter.alwaysShow, dialogFilter.neverShow, dialogFilter.pinnedDialogs, false, false, true, true, false, DialogsActivity.this, null);
                } else {
                    arrayList = dialogsCount;
                }
                if (arrayList.size() == 1) {
                    arrayList2 = arrayList;
                    j = arrayList2.get(0).longValue();
                } else {
                    arrayList2 = arrayList;
                    j = 0;
                }
                DialogsActivity.this.getUndoView().showWithAction(j, 20, Integer.valueOf(arrayList2.size()), dialogFilter, (Runnable) null, (Runnable) null);
            } else {
                DialogsActivity.this.presentFragment(new FilterCreateActivity(null, dialogsCount));
            }
            DialogsActivity.this.hideActionMode(true);
        }
    }

    public void switchToCurrentSelectedMode(boolean z) {
        ViewPage[] viewPageArr;
        int i = 0;
        int i2 = 0;
        while (true) {
            viewPageArr = this.viewPages;
            if (i2 >= viewPageArr.length) {
                break;
            }
            viewPageArr[i2].listView.stopScroll();
            i2++;
        }
        viewPageArr[z ? 1 : 0].listView.getAdapter();
        if (this.viewPages[z].selectedType == Integer.MAX_VALUE) {
            this.viewPages[z].dialogsType = 0;
            this.viewPages[z].listView.updatePullState();
        } else {
            MessagesController.DialogFilter dialogFilter = getMessagesController().dialogFilters.get(this.viewPages[z].selectedType);
            if (this.viewPages[!z ? 1 : 0].dialogsType == 7) {
                this.viewPages[z].dialogsType = 8;
            } else {
                this.viewPages[z].dialogsType = 7;
            }
            this.viewPages[z].listView.setScrollEnabled(true);
            getMessagesController().selectDialogFilter(dialogFilter, this.viewPages[z].dialogsType == 8 ? 1 : 0);
        }
        this.viewPages[z].dialogsAdapter.setDialogsType(this.viewPages[z].dialogsType);
        LinearLayoutManager linearLayoutManager = this.viewPages[z].layoutManager;
        if (this.viewPages[z].dialogsType == 0 && hasHiddenArchive()) {
            i = 1;
        }
        linearLayoutManager.scrollToPositionWithOffset(i, (int) this.actionBar.getTranslationY());
        checkListLoad(this.viewPages[z]);
    }

    public void showScrollbars(boolean z) {
        if (this.viewPages != null && this.scrollBarVisible != z) {
            this.scrollBarVisible = z;
            int i = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i < viewPageArr.length) {
                    if (z) {
                        viewPageArr[i].listView.setScrollbarFadingEnabled(false);
                    }
                    this.viewPages[i].listView.setVerticalScrollBarEnabled(z);
                    if (z) {
                        this.viewPages[i].listView.setScrollbarFadingEnabled(true);
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    private void updateFilterTabs(boolean z, boolean z2) {
        int findFirstVisibleItemPosition;
        boolean z3;
        if (!(this.filterTabsView == null || this.inPreviewMode || this.searchIsShowed)) {
            ActionBarPopupWindow actionBarPopupWindow = this.scrimPopupWindow;
            if (actionBarPopupWindow != null) {
                actionBarPopupWindow.dismiss();
                this.scrimPopupWindow = null;
            }
            ArrayList<MessagesController.DialogFilter> arrayList = getMessagesController().dialogFilters;
            MessagesController.getMainSettings(this.currentAccount);
            boolean z4 = true;
            if (arrayList.isEmpty()) {
                if (this.filterTabsView.getVisibility() != 8) {
                    this.filterTabsView.setIsEditing(false);
                    showDoneItem(false);
                    this.maybeStartTracking = false;
                    if (this.startedTracking) {
                        this.startedTracking = false;
                        this.viewPages[0].setTranslationX(0.0f);
                        ViewPage[] viewPageArr = this.viewPages;
                        viewPageArr[1].setTranslationX(viewPageArr[0].getMeasuredWidth());
                    }
                    if (this.viewPages[0].selectedType != Integer.MAX_VALUE) {
                        this.viewPages[0].selectedType = ConnectionsManager.DEFAULT_DATACENTER_ID;
                        this.viewPages[0].dialogsAdapter.setDialogsType(0);
                        this.viewPages[0].dialogsType = 0;
                        this.viewPages[0].dialogsAdapter.notifyDataSetChanged();
                    }
                    this.viewPages[1].setVisibility(8);
                    this.viewPages[1].selectedType = ConnectionsManager.DEFAULT_DATACENTER_ID;
                    this.viewPages[1].dialogsAdapter.setDialogsType(0);
                    this.viewPages[1].dialogsType = 0;
                    this.viewPages[1].dialogsAdapter.notifyDataSetChanged();
                    this.canShowFilterTabsView = false;
                    updateFilterTabsVisibility(z2);
                    int i = 0;
                    while (true) {
                        ViewPage[] viewPageArr2 = this.viewPages;
                        if (i >= viewPageArr2.length) {
                            break;
                        }
                        if (viewPageArr2[i].dialogsType == 0 && this.viewPages[i].archivePullViewState == 2 && hasHiddenArchive() && ((findFirstVisibleItemPosition = this.viewPages[i].layoutManager.findFirstVisibleItemPosition()) == 0 || findFirstVisibleItemPosition == 1)) {
                            this.viewPages[i].layoutManager.scrollToPositionWithOffset(1, 0);
                        }
                        this.viewPages[i].listView.setScrollingTouchSlop(0);
                        this.viewPages[i].listView.requestLayout();
                        this.viewPages[i].requestLayout();
                        i++;
                    }
                }
                ActionBarLayout actionBarLayout = this.parentLayout;
                if (actionBarLayout != null) {
                    actionBarLayout.getDrawerLayoutContainer().setAllowOpenDrawerBySwipe(true);
                }
            } else if (z || this.filterTabsView.getVisibility() != 0) {
                boolean z5 = this.filterTabsView.getVisibility() != 0 ? false : z2;
                this.canShowFilterTabsView = true;
                updateFilterTabsVisibility(z2);
                int currentTabId = this.filterTabsView.getCurrentTabId();
                if (currentTabId != Integer.MAX_VALUE && currentTabId >= arrayList.size()) {
                    this.filterTabsView.resetTabId();
                }
                this.filterTabsView.removeTabs();
                this.filterTabsView.addTab(ConnectionsManager.DEFAULT_DATACENTER_ID, 0, LocaleController.getString("FilterAllChats", R.string.FilterAllChats));
                int size = arrayList.size();
                for (int i2 = 0; i2 < size; i2++) {
                    this.filterTabsView.addTab(i2, arrayList.get(i2).localId, arrayList.get(i2).name);
                }
                int currentTabId2 = this.filterTabsView.getCurrentTabId();
                if (currentTabId2 < 0 || this.viewPages[0].selectedType == currentTabId2) {
                    z3 = false;
                } else {
                    this.viewPages[0].selectedType = currentTabId2;
                    z3 = true;
                }
                int i3 = 0;
                while (true) {
                    ViewPage[] viewPageArr3 = this.viewPages;
                    if (i3 >= viewPageArr3.length) {
                        break;
                    }
                    if (viewPageArr3[i3].selectedType != Integer.MAX_VALUE && this.viewPages[i3].selectedType >= arrayList.size()) {
                        this.viewPages[i3].selectedType = arrayList.size() - 1;
                    }
                    this.viewPages[i3].listView.setScrollingTouchSlop(1);
                    i3++;
                }
                this.filterTabsView.finishAddingTabs(z5);
                if (z3) {
                    switchToCurrentSelectedMode(false);
                }
                ActionBarLayout actionBarLayout2 = this.parentLayout;
                if (actionBarLayout2 != null) {
                    DrawerLayoutContainer drawerLayoutContainer = actionBarLayout2.getDrawerLayoutContainer();
                    if (currentTabId2 != this.filterTabsView.getFirstTabId() && SharedConfig.getChatSwipeAction(this.currentAccount) == 5) {
                        z4 = false;
                    }
                    drawerLayoutContainer.setAllowOpenDrawerBySwipe(z4);
                }
            }
            updateCounters(false);
        }
    }

    @Override
    public void finishFragment() {
        super.finishFragment();
        ActionBarPopupWindow actionBarPopupWindow = this.scrimPopupWindow;
        if (actionBarPopupWindow != null) {
            actionBarPopupWindow.dismiss();
        }
    }

    @Override
    public void onResume() {
        int i;
        View view;
        super.onResume();
        if (!this.parentLayout.isInPreviewMode() && (view = this.blurredView) != null && view.getVisibility() == 0) {
            this.blurredView.setVisibility(8);
            this.blurredView.setBackground(null);
        }
        FilterTabsView filterTabsView = this.filterTabsView;
        if (filterTabsView != null && filterTabsView.getVisibility() == 0) {
            this.parentLayout.getDrawerLayoutContainer().setAllowOpenDrawerBySwipe(this.viewPages[0].selectedType == this.filterTabsView.getFirstTabId() || this.searchIsShowed || SharedConfig.getChatSwipeAction(this.currentAccount) != 5);
        }
        if (this.viewPages != null) {
            int i2 = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i2 >= viewPageArr.length) {
                    break;
                }
                viewPageArr[i2].dialogsAdapter.notifyDataSetChanged();
                i2++;
            }
        }
        ChatActivityEnterView chatActivityEnterView = this.commentView;
        if (chatActivityEnterView != null) {
            chatActivityEnterView.onResume();
        }
        if (!this.onlySelect && this.folderId == 0) {
            getMediaDataController().checkStickers(4);
        }
        SearchViewPager searchViewPager = this.searchViewPager;
        if (searchViewPager != null) {
            searchViewPager.onResume();
        }
        if ((this.afterSignup || getUserConfig().unacceptedTermsOfService == null) && this.checkPermission && !this.onlySelect && (i = Build.VERSION.SDK_INT) >= 23) {
            final Activity parentActivity = getParentActivity();
            if (parentActivity != null) {
                this.checkPermission = false;
                final boolean z = parentActivity.checkSelfPermission("android.permission.READ_CONTACTS") != 0;
                final boolean z2 = (i <= 28 || BuildVars.NO_SCOPED_STORAGE) && parentActivity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public final void run() {
                        DialogsActivity.this.lambda$onResume$16(z, z2, parentActivity);
                    }
                }, (!this.afterSignup || !z) ? 0L : 4000L);
            }
        } else if (!this.onlySelect && XiaomiUtilities.isMIUI() && Build.VERSION.SDK_INT >= 19 && !XiaomiUtilities.isCustomPermissionGranted(XiaomiUtilities.OP_SHOW_WHEN_LOCKED)) {
            if (getParentActivity() != null && !MessagesController.getGlobalNotificationsSettings().getBoolean("askedAboutMiuiLockscreen", false)) {
                showDialog(new AlertDialog.Builder(getParentActivity()).setTopAnimation(R.raw.permission_request_apk, 72, false, Theme.getColor("dialogTopBackground")).setMessage(LocaleController.getString("PermissionXiaomiLockscreen", R.string.PermissionXiaomiLockscreen)).setPositiveButton(LocaleController.getString("PermissionOpenSettings", R.string.PermissionOpenSettings), new DialogInterface.OnClickListener() {
                    @Override
                    public final void onClick(DialogInterface dialogInterface, int i3) {
                        DialogsActivity.this.lambda$onResume$17(dialogInterface, i3);
                    }
                }).setNegativeButton(LocaleController.getString("ContactsPermissionAlertNotNow", R.string.ContactsPermissionAlertNotNow), DialogsActivity$$ExternalSyntheticLambda15.INSTANCE).create());
            } else {
                return;
            }
        }
        showFiltersHint();
        if (this.viewPages != null) {
            int i3 = 0;
            while (true) {
                ViewPage[] viewPageArr2 = this.viewPages;
                if (i3 >= viewPageArr2.length) {
                    break;
                }
                if (viewPageArr2[i3].dialogsType == 0 && this.viewPages[i3].archivePullViewState == 2 && this.viewPages[i3].layoutManager.findFirstVisibleItemPosition() == 0 && hasHiddenArchive()) {
                    this.viewPages[i3].layoutManager.scrollToPositionWithOffset(1, 0);
                }
                if (i3 == 0) {
                    this.viewPages[i3].dialogsAdapter.resume();
                } else {
                    this.viewPages[i3].dialogsAdapter.pause();
                }
                i3++;
            }
        }
        showNextSupportedSuggestion();
        Bulletin.addDelegate(this, new Bulletin.Delegate() {
            @Override
            public int getBottomOffset(int i4) {
                return Bulletin.Delegate.CC.$default$getBottomOffset(this, i4);
            }

            @Override
            public void onHide(Bulletin bulletin) {
                Bulletin.Delegate.CC.$default$onHide(this, bulletin);
            }

            @Override
            public void onOffsetChange(float f) {
                if (DialogsActivity.this.undoView[0] == null || DialogsActivity.this.undoView[0].getVisibility() != 0) {
                    DialogsActivity.this.additionalFloatingTranslation = f;
                    if (DialogsActivity.this.additionalFloatingTranslation < 0.0f) {
                        DialogsActivity.this.additionalFloatingTranslation = 0.0f;
                    }
                    if (!DialogsActivity.this.floatingHidden) {
                        DialogsActivity.this.updateFloatingButtonOffset();
                    }
                }
            }

            @Override
            public void onShow(Bulletin bulletin) {
                if (DialogsActivity.this.undoView[0] != null && DialogsActivity.this.undoView[0].getVisibility() == 0) {
                    DialogsActivity.this.undoView[0].hide(true, 2);
                }
            }
        });
        if (this.searchIsShowed) {
            AndroidUtilities.requestAdjustResize(getParentActivity(), this.classGuid);
        }
        updateVisibleRows(0, false);
        updateProxyButton(false, true);
        checkSuggestClearDatabase();
    }

    public void lambda$onResume$16(boolean z, boolean z2, Activity activity) {
        this.afterSignup = false;
        if (z || z2) {
            this.askingForPermissions = true;
            if (z && this.askAboutContacts && getUserConfig().syncContacts && activity.shouldShowRequestPermissionRationale("android.permission.READ_CONTACTS")) {
                AlertDialog create = AlertsCreator.createContactsPermissionDialog(activity, new MessagesStorage.IntCallback() {
                    @Override
                    public final void run(int i) {
                        DialogsActivity.this.lambda$onResume$15(i);
                    }
                }).create();
                this.permissionDialog = create;
                showDialog(create);
            } else if (!z2 || !activity.shouldShowRequestPermissionRationale("android.permission.WRITE_EXTERNAL_STORAGE")) {
                askForPermissons(true);
            } else if (activity instanceof BasePermissionsActivity) {
                AlertDialog createPermissionErrorAlert = ((BasePermissionsActivity) activity).createPermissionErrorAlert(R.raw.permission_request_folder, LocaleController.getString((int) R.string.PermissionStorageWithHint));
                this.permissionDialog = createPermissionErrorAlert;
                showDialog(createPermissionErrorAlert);
            }
        }
    }

    public void lambda$onResume$15(int i) {
        this.askAboutContacts = i != 0;
        MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askAboutContacts", this.askAboutContacts).apply();
        askForPermissons(false);
    }

    public void lambda$onResume$17(DialogInterface dialogInterface, int i) {
        Intent permissionManagerIntent = XiaomiUtilities.getPermissionManagerIntent();
        if (permissionManagerIntent != null) {
            try {
                try {
                    getParentActivity().startActivity(permissionManagerIntent);
                } catch (Exception unused) {
                    Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
                    getParentActivity().startActivity(intent);
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public static void lambda$onResume$18(DialogInterface dialogInterface, int i) {
        MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askedAboutMiuiLockscreen", true).commit();
    }

    @Override
    public boolean presentFragment(BaseFragment baseFragment) {
        boolean presentFragment = super.presentFragment(baseFragment);
        if (presentFragment && this.viewPages != null) {
            int i = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i >= viewPageArr.length) {
                    break;
                }
                viewPageArr[i].dialogsAdapter.pause();
                i++;
            }
        }
        return presentFragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        ActionBarPopupWindow actionBarPopupWindow = this.scrimPopupWindow;
        if (actionBarPopupWindow != null) {
            actionBarPopupWindow.dismiss();
        }
        ChatActivityEnterView chatActivityEnterView = this.commentView;
        if (chatActivityEnterView != null) {
            chatActivityEnterView.onResume();
        }
        UndoView[] undoViewArr = this.undoView;
        int i = 0;
        if (undoViewArr[0] != null) {
            undoViewArr[0].hide(true, 0);
        }
        Bulletin.removeDelegate(this);
        if (this.viewPages != null) {
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i < viewPageArr.length) {
                    viewPageArr[i].dialogsAdapter.pause();
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        ActionBarPopupWindow actionBarPopupWindow = this.scrimPopupWindow;
        if (actionBarPopupWindow != null) {
            actionBarPopupWindow.dismiss();
            return false;
        }
        FilterTabsView filterTabsView = this.filterTabsView;
        if (filterTabsView == null || !filterTabsView.isEditing()) {
            ActionBar actionBar = this.actionBar;
            if (actionBar == null || !actionBar.isActionModeShowed()) {
                FilterTabsView filterTabsView2 = this.filterTabsView;
                if (filterTabsView2 == null || filterTabsView2.getVisibility() != 0 || this.tabsAnimationInProgress || this.filterTabsView.isAnimatingIndicator() || this.filterTabsView.getCurrentTabId() == Integer.MAX_VALUE || this.startedTracking) {
                    ChatActivityEnterView chatActivityEnterView = this.commentView;
                    if (chatActivityEnterView == null || !chatActivityEnterView.isPopupShowing()) {
                        return super.onBackPressed();
                    }
                    this.commentView.hidePopup(true);
                    return false;
                }
                this.filterTabsView.selectFirstTab();
                return false;
            }
            if (this.searchViewPager.getVisibility() == 0) {
                this.searchViewPager.hideActionMode();
                hideActionMode(true);
            } else {
                hideActionMode(true);
            }
            return false;
        }
        this.filterTabsView.setIsEditing(false);
        showDoneItem(false);
        return false;
    }

    @Override
    public void onBecomeFullyHidden() {
        if (this.closeSearchFieldOnHide) {
            ActionBar actionBar = this.actionBar;
            if (actionBar != null) {
                actionBar.closeSearchField();
            }
            TLObject tLObject = this.searchObject;
            if (tLObject != null) {
                this.searchViewPager.dialogsSearchAdapter.putRecentSearch(this.searchDialogId, tLObject);
                this.searchObject = null;
            }
            this.closeSearchFieldOnHide = false;
        }
        FilterTabsView filterTabsView = this.filterTabsView;
        if (filterTabsView != null && filterTabsView.getVisibility() == 0 && this.filterTabsViewIsVisible) {
            int i = (int) (-this.actionBar.getTranslationY());
            int currentActionBarHeight = ActionBar.getCurrentActionBarHeight();
            if (!(i == 0 || i == currentActionBarHeight)) {
                if (i < currentActionBarHeight / 2) {
                    setScrollY(0.0f);
                } else if (this.viewPages[0].listView.canScrollVertically(1)) {
                    setScrollY(-currentActionBarHeight);
                }
            }
        }
        UndoView[] undoViewArr = this.undoView;
        if (undoViewArr[0] != null) {
            undoViewArr[0].hide(true, 0);
        }
    }

    @Override
    public void setInPreviewMode(boolean z) {
        super.setInPreviewMode(z);
        if (!z && this.avatarContainer != null) {
            this.actionBar.setBackground(null);
            ((ViewGroup.MarginLayoutParams) this.actionBar.getLayoutParams()).topMargin = 0;
            this.actionBar.removeView(this.avatarContainer);
            this.avatarContainer = null;
            updateFilterTabs(false, false);
            this.floatingButton.setVisibility(0);
            ContentView contentView = (ContentView) this.fragmentView;
            FragmentContextView fragmentContextView = this.fragmentContextView;
            if (fragmentContextView != null) {
                contentView.addView(fragmentContextView);
            }
            FragmentContextView fragmentContextView2 = this.fragmentLocationContextView;
            if (fragmentContextView2 != null) {
                contentView.addView(fragmentContextView2);
            }
        }
    }

    public boolean addOrRemoveSelectedDialog(long j, View view) {
        if (this.selectedDialogs.contains(Long.valueOf(j))) {
            this.selectedDialogs.remove(Long.valueOf(j));
            if (view instanceof DialogCell) {
                ((DialogCell) view).setChecked(false, true);
            } else if (view instanceof ProfileSearchCell) {
                ((ProfileSearchCell) view).setChecked(false, true);
            }
            return false;
        }
        this.selectedDialogs.add(Long.valueOf(j));
        if (view instanceof DialogCell) {
            ((DialogCell) view).setChecked(true, true);
        } else if (view instanceof ProfileSearchCell) {
            ((ProfileSearchCell) view).setChecked(true, true);
        }
        return true;
    }

    public void search(String str, boolean z) {
        showSearch(true, false, z);
        this.actionBar.openSearchField(str, false);
    }

    public void showSearch(final boolean z, boolean z2, boolean z3) {
        FilterTabsView filterTabsView;
        ActionBarLayout actionBarLayout;
        int i;
        int i2 = this.initialDialogsType;
        int i3 = 0;
        boolean z4 = false;
        boolean z5 = (i2 == 0 || i2 == 3) ? z3 : false;
        AnimatorSet animatorSet = this.searchAnimator;
        if (animatorSet != null) {
            animatorSet.cancel();
            this.searchAnimator = null;
        }
        Animator animator = this.tabsAlphaAnimator;
        if (animator != null) {
            animator.cancel();
            this.tabsAlphaAnimator = null;
        }
        this.searchIsShowed = z;
        ((SizeNotifierFrameLayout) this.fragmentView).invalidateBlur();
        if (z) {
            boolean onlyDialogsAdapter = this.searchFiltersWasShowed ? false : onlyDialogsAdapter();
            this.searchViewPager.showOnlyDialogsAdapter(onlyDialogsAdapter);
            boolean z6 = !onlyDialogsAdapter;
            this.whiteActionBar = z6;
            if (z6) {
                this.searchFiltersWasShowed = true;
            }
            ContentView contentView = (ContentView) this.fragmentView;
            ViewPagerFixed.TabsView tabsView = this.searchTabsView;
            if (tabsView == null && !onlyDialogsAdapter) {
                this.searchTabsView = this.searchViewPager.createTabsView();
                if (this.filtersView != null) {
                    i = 0;
                    while (i < contentView.getChildCount()) {
                        if (contentView.getChildAt(i) == this.filtersView) {
                            break;
                        }
                        i++;
                    }
                }
                i = -1;
                if (i > 0) {
                    contentView.addView(this.searchTabsView, i, LayoutHelper.createFrame(-1, 44.0f));
                } else {
                    contentView.addView(this.searchTabsView, LayoutHelper.createFrame(-1, 44.0f));
                }
            } else if (tabsView != null && onlyDialogsAdapter) {
                ViewParent parent = tabsView.getParent();
                if (parent instanceof ViewGroup) {
                    ((ViewGroup) parent).removeView(this.searchTabsView);
                }
                this.searchTabsView = null;
            }
            EditTextBoldCursor searchField = this.searchItem.getSearchField();
            if (this.whiteActionBar) {
                searchField.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
                searchField.setHintTextColor(Theme.getColor("player_time"));
                searchField.setCursorColor(Theme.getColor("chat_messagePanelCursor"));
            } else {
                searchField.setCursorColor(Theme.getColor("actionBarDefaultSearch"));
                searchField.setHintTextColor(Theme.getColor("actionBarDefaultSearchPlaceholder"));
                searchField.setTextColor(Theme.getColor("actionBarDefaultSearch"));
            }
            this.searchViewPager.setKeyboardHeight(((ContentView) this.fragmentView).getKeyboardHeight());
            this.parentLayout.getDrawerLayoutContainer().setAllowOpenDrawerBySwipe(true);
            this.searchViewPager.clear();
            if (this.folderId != 0) {
                addSearchFilter(new FiltersView.MediaFilterData(R.drawable.chats_archive, R.drawable.chats_archive, LocaleController.getString("ArchiveSearchFilter", R.string.ArchiveSearchFilter), null, 7));
            }
        } else if (!(this.filterTabsView == null || (actionBarLayout = this.parentLayout) == null)) {
            actionBarLayout.getDrawerLayoutContainer().setAllowOpenDrawerBySwipe(this.viewPages[0].selectedType == this.filterTabsView.getFirstTabId() || SharedConfig.getChatSwipeAction(this.currentAccount) != 5);
        }
        if (!z5 || !this.searchViewPager.dialogsSearchAdapter.hasRecentSearch()) {
            AndroidUtilities.requestAdjustResize(getParentActivity(), this.classGuid);
        } else {
            AndroidUtilities.setAdjustResizeToNothing(getParentActivity(), this.classGuid);
        }
        if (!z && (filterTabsView = this.filterTabsView) != null && this.canShowFilterTabsView) {
            filterTabsView.setVisibility(0);
        }
        float f = 0.9f;
        float f2 = 0.0f;
        if (z5) {
            if (z) {
                this.searchViewPager.setVisibility(0);
                this.searchViewPager.reset();
                updateFiltersView(true, null, null, false, false);
                ViewPagerFixed.TabsView tabsView2 = this.searchTabsView;
                if (tabsView2 != null) {
                    tabsView2.hide(false, false);
                    this.searchTabsView.setVisibility(0);
                }
            } else {
                this.viewPages[0].listView.setVisibility(0);
                this.viewPages[0].setVisibility(0);
            }
            setDialogsListFrozen(true);
            this.viewPages[0].listView.setVerticalScrollBarEnabled(false);
            this.searchViewPager.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
            this.searchAnimator = new AnimatorSet();
            ArrayList arrayList = new ArrayList();
            ViewPage viewPage = this.viewPages[0];
            Property property = View.ALPHA;
            float[] fArr = new float[1];
            fArr[0] = z ? 0.0f : 1.0f;
            arrayList.add(ObjectAnimator.ofFloat(viewPage, property, fArr));
            ViewPage viewPage2 = this.viewPages[0];
            Property property2 = View.SCALE_X;
            float[] fArr2 = new float[1];
            fArr2[0] = z ? 0.9f : 1.0f;
            arrayList.add(ObjectAnimator.ofFloat(viewPage2, property2, fArr2));
            ViewPage viewPage3 = this.viewPages[0];
            Property property3 = View.SCALE_Y;
            float[] fArr3 = new float[1];
            if (!z) {
                f = 1.0f;
            }
            fArr3[0] = f;
            arrayList.add(ObjectAnimator.ofFloat(viewPage3, property3, fArr3));
            SearchViewPager searchViewPager = this.searchViewPager;
            Property property4 = View.ALPHA;
            float[] fArr4 = new float[1];
            fArr4[0] = z ? 1.0f : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(searchViewPager, property4, fArr4));
            SearchViewPager searchViewPager2 = this.searchViewPager;
            Property property5 = View.SCALE_X;
            float[] fArr5 = new float[1];
            float f3 = 1.05f;
            fArr5[0] = z ? 1.0f : 1.05f;
            arrayList.add(ObjectAnimator.ofFloat(searchViewPager2, property5, fArr5));
            SearchViewPager searchViewPager3 = this.searchViewPager;
            Property property6 = View.SCALE_Y;
            float[] fArr6 = new float[1];
            if (z) {
                f3 = 1.0f;
            }
            fArr6[0] = f3;
            arrayList.add(ObjectAnimator.ofFloat(searchViewPager3, property6, fArr6));
            ActionBarMenuItem actionBarMenuItem = this.passcodeItem;
            if (actionBarMenuItem != null) {
                RLottieImageView iconView = actionBarMenuItem.getIconView();
                Property property7 = View.ALPHA;
                float[] fArr7 = new float[1];
                fArr7[0] = z ? 0.0f : 1.0f;
                arrayList.add(ObjectAnimator.ofFloat(iconView, property7, fArr7));
            }
            ActionBarMenuItem actionBarMenuItem2 = this.downloadsItem;
            if (actionBarMenuItem2 != null) {
                if (z) {
                    actionBarMenuItem2.setAlpha(0.0f);
                } else {
                    arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem2, View.ALPHA, 1.0f));
                }
                updateProxyButton(false, false);
            }
            FilterTabsView filterTabsView2 = this.filterTabsView;
            if (filterTabsView2 != null && filterTabsView2.getVisibility() == 0) {
                RecyclerListView tabsContainer = this.filterTabsView.getTabsContainer();
                Property property8 = View.ALPHA;
                float[] fArr8 = new float[1];
                fArr8[0] = z ? 0.0f : 1.0f;
                ObjectAnimator duration = ObjectAnimator.ofFloat(tabsContainer, property8, fArr8).setDuration(100L);
                this.tabsAlphaAnimator = duration;
                duration.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator2) {
                        DialogsActivity.this.tabsAlphaAnimator = null;
                    }
                });
            }
            float[] fArr9 = new float[2];
            fArr9[0] = this.searchAnimationProgress;
            if (z) {
                f2 = 1.0f;
            }
            fArr9[1] = f2;
            ValueAnimator ofFloat = ValueAnimator.ofFloat(fArr9);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    DialogsActivity.this.lambda$showSearch$19(valueAnimator);
                }
            });
            arrayList.add(ofFloat);
            this.searchAnimator.playTogether(arrayList);
            this.searchAnimator.setDuration(z ? 200L : 180L);
            this.searchAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT);
            if (this.filterTabsViewIsVisible) {
                int color = Theme.getColor(this.folderId == 0 ? "actionBarDefault" : "actionBarDefaultArchived");
                int color2 = Theme.getColor("windowBackgroundWhite");
                if (((Math.abs(Color.red(color) - Color.red(color2)) + Math.abs(Color.green(color) - Color.green(color2))) + Math.abs(Color.blue(color) - Color.blue(color2))) / 255.0f > 0.3f) {
                    z4 = true;
                }
                this.searchAnimationTabsDelayedCrossfade = z4;
            } else {
                this.searchAnimationTabsDelayedCrossfade = true;
            }
            if (!z) {
                this.searchAnimator.setStartDelay(20L);
                Animator animator2 = this.tabsAlphaAnimator;
                if (animator2 != null) {
                    if (this.searchAnimationTabsDelayedCrossfade) {
                        animator2.setStartDelay(80L);
                        this.tabsAlphaAnimator.setDuration(100L);
                    } else {
                        animator2.setDuration(z ? 200L : 180L);
                    }
                }
            }
            this.searchAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator3) {
                    DialogsActivity.this.getNotificationCenter().onAnimationFinish(DialogsActivity.this.animationIndex);
                    if (DialogsActivity.this.searchAnimator == animator3) {
                        DialogsActivity.this.setDialogsListFrozen(false);
                        if (z) {
                            DialogsActivity.this.viewPages[0].listView.hide();
                            if (DialogsActivity.this.filterTabsView != null) {
                                DialogsActivity.this.filterTabsView.setVisibility(8);
                            }
                            DialogsActivity.this.searchWasFullyShowed = true;
                            AndroidUtilities.requestAdjustResize(DialogsActivity.this.getParentActivity(), ((BaseFragment) DialogsActivity.this).classGuid);
                            DialogsActivity.this.searchItem.setVisibility(8);
                        } else {
                            DialogsActivity.this.searchItem.collapseSearchFilters();
                            DialogsActivity.this.whiteActionBar = false;
                            DialogsActivity.this.searchViewPager.setVisibility(8);
                            if (DialogsActivity.this.searchTabsView != null) {
                                DialogsActivity.this.searchTabsView.setVisibility(8);
                            }
                            DialogsActivity.this.searchItem.clearSearchFilters();
                            DialogsActivity.this.searchViewPager.clear();
                            DialogsActivity.this.filtersView.setVisibility(8);
                            DialogsActivity.this.viewPages[0].listView.show();
                            if (!DialogsActivity.this.onlySelect) {
                                DialogsActivity.this.hideFloatingButton(false);
                            }
                            DialogsActivity.this.searchWasFullyShowed = false;
                        }
                        if (((BaseFragment) DialogsActivity.this).fragmentView != null) {
                            ((BaseFragment) DialogsActivity.this).fragmentView.requestLayout();
                        }
                        float f4 = 1.0f;
                        DialogsActivity.this.setSearchAnimationProgress(z ? 1.0f : 0.0f);
                        DialogsActivity.this.viewPages[0].listView.setVerticalScrollBarEnabled(true);
                        DialogsActivity.this.searchViewPager.setBackground(null);
                        DialogsActivity.this.searchAnimator = null;
                        if (DialogsActivity.this.downloadsItem != null) {
                            ActionBarMenuItem actionBarMenuItem3 = DialogsActivity.this.downloadsItem;
                            if (z) {
                                f4 = 0.0f;
                            }
                            actionBarMenuItem3.setAlpha(f4);
                        }
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator3) {
                    DialogsActivity.this.getNotificationCenter().onAnimationFinish(DialogsActivity.this.animationIndex);
                    if (DialogsActivity.this.searchAnimator == animator3) {
                        if (z) {
                            DialogsActivity.this.viewPages[0].listView.hide();
                        } else {
                            DialogsActivity.this.viewPages[0].listView.show();
                        }
                        DialogsActivity.this.searchAnimator = null;
                    }
                }
            });
            this.animationIndex = getNotificationCenter().setAnimationInProgress(this.animationIndex, null);
            this.searchAnimator.start();
            Animator animator3 = this.tabsAlphaAnimator;
            if (animator3 != null) {
                animator3.start();
            }
        } else {
            setDialogsListFrozen(false);
            if (z) {
                this.viewPages[0].listView.hide();
            } else {
                this.viewPages[0].listView.show();
            }
            this.viewPages[0].setAlpha(z ? 0.0f : 1.0f);
            this.viewPages[0].setScaleX(z ? 0.9f : 1.0f);
            ViewPage viewPage4 = this.viewPages[0];
            if (!z) {
                f = 1.0f;
            }
            viewPage4.setScaleY(f);
            this.searchViewPager.setAlpha(z ? 1.0f : 0.0f);
            this.filtersView.setAlpha(z ? 1.0f : 0.0f);
            float f4 = 1.1f;
            this.searchViewPager.setScaleX(z ? 1.0f : 1.1f);
            SearchViewPager searchViewPager4 = this.searchViewPager;
            if (z) {
                f4 = 1.0f;
            }
            searchViewPager4.setScaleY(f4);
            FilterTabsView filterTabsView3 = this.filterTabsView;
            if (filterTabsView3 != null && filterTabsView3.getVisibility() == 0) {
                this.filterTabsView.setTranslationY(z ? -AndroidUtilities.dp(44.0f) : 0.0f);
                this.filterTabsView.getTabsContainer().setAlpha(z ? 0.0f : 1.0f);
            }
            FilterTabsView filterTabsView4 = this.filterTabsView;
            if (filterTabsView4 != null) {
                if (!this.canShowFilterTabsView || z) {
                    filterTabsView4.setVisibility(8);
                } else {
                    filterTabsView4.setVisibility(0);
                }
            }
            SearchViewPager searchViewPager5 = this.searchViewPager;
            if (!z) {
                i3 = 8;
            }
            searchViewPager5.setVisibility(i3);
            setSearchAnimationProgress(z ? 1.0f : 0.0f);
            this.fragmentView.invalidate();
            ActionBarMenuItem actionBarMenuItem3 = this.downloadsItem;
            if (actionBarMenuItem3 != null) {
                if (!z) {
                    f2 = 1.0f;
                }
                actionBarMenuItem3.setAlpha(f2);
            }
        }
        int i4 = this.initialSearchType;
        if (i4 >= 0) {
            SearchViewPager searchViewPager6 = this.searchViewPager;
            searchViewPager6.setPosition(searchViewPager6.getPositionForType(i4));
        }
        if (!z) {
            this.initialSearchType = -1;
        }
        if (z && z2) {
            this.searchViewPager.showDownloads();
        }
    }

    public void lambda$showSearch$19(ValueAnimator valueAnimator) {
        setSearchAnimationProgress(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    public boolean onlyDialogsAdapter() {
        return this.onlySelect || !this.searchViewPager.dialogsSearchAdapter.hasRecentSearch() || getMessagesController().getTotalDialogsCount() <= 10;
    }

    private void updateFilterTabsVisibility(boolean z) {
        int i = 0;
        if (this.isPaused || this.databaseMigrationHint != null) {
            z = false;
        }
        float f = 1.0f;
        if (this.searchIsShowed) {
            ValueAnimator valueAnimator = this.filtersTabAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            boolean z2 = this.canShowFilterTabsView;
            this.filterTabsViewIsVisible = z2;
            if (!z2) {
                f = 0.0f;
            }
            this.filterTabsProgress = f;
            return;
        }
        final boolean z3 = this.canShowFilterTabsView;
        if (this.filterTabsViewIsVisible != z3) {
            ValueAnimator valueAnimator2 = this.filtersTabAnimator;
            if (valueAnimator2 != null) {
                valueAnimator2.cancel();
            }
            this.filterTabsViewIsVisible = z3;
            if (z) {
                if (z3) {
                    if (this.filterTabsView.getVisibility() != 0) {
                        this.filterTabsView.setVisibility(0);
                    }
                    this.filtersTabAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
                    this.filterTabsMoveFrom = AndroidUtilities.dp(44.0f);
                } else {
                    this.filtersTabAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
                    this.filterTabsMoveFrom = Math.max(0.0f, AndroidUtilities.dp(44.0f) + this.actionBar.getTranslationY());
                }
                final float translationY = this.actionBar.getTranslationY();
                this.filtersTabAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        DialogsActivity.this.filtersTabAnimator = null;
                        DialogsActivity.this.scrollAdditionalOffset = AndroidUtilities.dp(44.0f) - DialogsActivity.this.filterTabsMoveFrom;
                        if (!z3) {
                            DialogsActivity.this.filterTabsView.setVisibility(8);
                        }
                        if (((BaseFragment) DialogsActivity.this).fragmentView != null) {
                            ((BaseFragment) DialogsActivity.this).fragmentView.requestLayout();
                        }
                        DialogsActivity.this.getNotificationCenter().onAnimationFinish(DialogsActivity.this.animationIndex);
                    }
                });
                this.filtersTabAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public final void onAnimationUpdate(ValueAnimator valueAnimator3) {
                        DialogsActivity.this.lambda$updateFilterTabsVisibility$20(z3, translationY, valueAnimator3);
                    }
                });
                this.filtersTabAnimator.setDuration(220L);
                this.filtersTabAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
                this.animationIndex = getNotificationCenter().setAnimationInProgress(this.animationIndex, null);
                this.filtersTabAnimator.start();
                this.fragmentView.requestLayout();
                return;
            }
            if (!z3) {
                f = 0.0f;
            }
            this.filterTabsProgress = f;
            FilterTabsView filterTabsView = this.filterTabsView;
            if (!z3) {
                i = 8;
            }
            filterTabsView.setVisibility(i);
            View view = this.fragmentView;
            if (view != null) {
                view.invalidate();
            }
        }
    }

    public void lambda$updateFilterTabsVisibility$20(boolean z, float f, ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.filterTabsProgress = floatValue;
        if (!z) {
            setScrollY(f * floatValue);
        }
        View view = this.fragmentView;
        if (view != null) {
            view.invalidate();
        }
    }

    public void setSearchAnimationProgress(float f) {
        this.searchAnimationProgress = f;
        if (this.whiteActionBar) {
            this.actionBar.setItemsColor(ColorUtils.blendARGB(Theme.getColor(this.folderId != 0 ? "actionBarDefaultArchivedIcon" : "actionBarDefaultIcon"), Theme.getColor("windowBackgroundWhiteGrayText2"), this.searchAnimationProgress), false);
            this.actionBar.setItemsColor(ColorUtils.blendARGB(Theme.getColor("actionBarActionModeDefaultIcon"), Theme.getColor("windowBackgroundWhiteGrayText2"), this.searchAnimationProgress), true);
            this.actionBar.setItemsBackgroundColor(ColorUtils.blendARGB(Theme.getColor(this.folderId != 0 ? "actionBarDefaultArchivedSelector" : "actionBarDefaultSelector"), Theme.getColor("actionBarActionModeDefaultSelector"), this.searchAnimationProgress), false);
        }
        View view = this.fragmentView;
        if (view != null) {
            view.invalidate();
        }
        updateContextViewPosition();
    }

    public void findAndUpdateCheckBox(long j, boolean z) {
        if (this.viewPages != null) {
            int i = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i < viewPageArr.length) {
                    int childCount = viewPageArr[i].listView.getChildCount();
                    int i2 = 0;
                    while (true) {
                        if (i2 < childCount) {
                            View childAt = this.viewPages[i].listView.getChildAt(i2);
                            if (childAt instanceof DialogCell) {
                                DialogCell dialogCell = (DialogCell) childAt;
                                if (dialogCell.getDialogId() == j) {
                                    dialogCell.setChecked(z, true);
                                    break;
                                }
                            }
                            i2++;
                        }
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    public void checkListLoad(ViewPage viewPage) {
        final boolean z;
        final boolean z2;
        final boolean z3;
        final boolean z4;
        if (!this.tabsAnimationInProgress && !this.startedTracking) {
            FilterTabsView filterTabsView = this.filterTabsView;
            if (filterTabsView == null || filterTabsView.getVisibility() != 0 || !this.filterTabsView.isAnimatingIndicator()) {
                int findFirstVisibleItemPosition = viewPage.layoutManager.findFirstVisibleItemPosition();
                int findLastVisibleItemPosition = viewPage.layoutManager.findLastVisibleItemPosition();
                int abs = Math.abs(viewPage.layoutManager.findLastVisibleItemPosition() - findFirstVisibleItemPosition) + 1;
                if (findLastVisibleItemPosition != -1) {
                    RecyclerView.ViewHolder findViewHolderForAdapterPosition = viewPage.listView.findViewHolderForAdapterPosition(findLastVisibleItemPosition);
                    boolean z5 = findViewHolderForAdapterPosition != null && findViewHolderForAdapterPosition.getItemViewType() == 11;
                    this.floatingForceVisible = z5;
                    if (z5) {
                        hideFloatingButton(false);
                    }
                } else {
                    this.floatingForceVisible = false;
                }
                if (viewPage.dialogsType == 7 || viewPage.dialogsType == 8) {
                    ArrayList<MessagesController.DialogFilter> arrayList = getMessagesController().dialogFilters;
                    if (viewPage.selectedType >= 0 && viewPage.selectedType < arrayList.size() && (getMessagesController().dialogFilters.get(viewPage.selectedType).flags & MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_ARCHIVED) == 0 && ((abs > 0 && findLastVisibleItemPosition >= getDialogsArray(this.currentAccount, viewPage.dialogsType, 1, this.dialogsListFrozen).size() - 10) || (abs == 0 && !getMessagesController().isDialogsEndReached(1)))) {
                        boolean z6 = !getMessagesController().isDialogsEndReached(1);
                        if (z6 || !getMessagesController().isServerDialogsEndReached(1)) {
                            z = z6;
                            z2 = true;
                        } else {
                            z = z6;
                            z2 = false;
                        }
                        if ((abs > 0 || findLastVisibleItemPosition < getDialogsArray(this.currentAccount, viewPage.dialogsType, this.folderId, this.dialogsListFrozen).size() - 10) && (abs != 0 || (!(viewPage.dialogsType == 7 || viewPage.dialogsType == 8) || getMessagesController().isDialogsEndReached(this.folderId)))) {
                            z4 = false;
                            z3 = false;
                        } else {
                            boolean z7 = !getMessagesController().isDialogsEndReached(this.folderId);
                            if (z7 || !getMessagesController().isServerDialogsEndReached(this.folderId)) {
                                z3 = z7;
                                z4 = true;
                            } else {
                                z3 = z7;
                                z4 = false;
                            }
                        }
                        if (!z4 || z2) {
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public final void run() {
                                    DialogsActivity.this.lambda$checkListLoad$21(z4, z3, z2, z);
                                }
                            });
                        }
                        return;
                    }
                }
                z2 = false;
                z = false;
                if (abs > 0) {
                }
                z4 = false;
                z3 = false;
                if (!z4) {
                }
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public final void run() {
                        DialogsActivity.this.lambda$checkListLoad$21(z4, z3, z2, z);
                    }
                });
            }
        }
    }

    public void lambda$checkListLoad$21(boolean z, boolean z2, boolean z3, boolean z4) {
        if (z) {
            getMessagesController().loadDialogs(this.folderId, -1, 100, z2);
        }
        if (z3) {
            getMessagesController().loadDialogs(1, -1, 100, z4);
        }
    }

    private void onItemClick(android.view.View r17, int r18, androidx.recyclerview.widget.RecyclerView.Adapter r19) {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.onItemClick(android.view.View, int, androidx.recyclerview.widget.RecyclerView$Adapter):void");
    }

    public boolean onItemLongClick(View view, int i, float f, float f2, int i2, RecyclerView.Adapter adapter) {
        TLRPC$Dialog tLRPC$Dialog;
        String str;
        int i3;
        final long j;
        if (getParentActivity() == null) {
            return false;
        }
        if (!this.actionBar.isActionModeShowed() && !AndroidUtilities.isTablet() && !this.onlySelect && (view instanceof DialogCell)) {
            DialogCell dialogCell = (DialogCell) view;
            if (dialogCell.isPointInsideAvatar(f, f2)) {
                return showChatPreview(dialogCell);
            }
        }
        DialogsSearchAdapter dialogsSearchAdapter = this.searchViewPager.dialogsSearchAdapter;
        String str2 = null;
        if (adapter == dialogsSearchAdapter) {
            Object item = dialogsSearchAdapter.getItem(i);
            if (this.searchViewPager.dialogsSearchAdapter.isRecentSearchDisplayed()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("ClearSearchSingleAlertTitle", R.string.ClearSearchSingleAlertTitle));
                if (item instanceof TLRPC$Chat) {
                    TLRPC$Chat tLRPC$Chat = (TLRPC$Chat) item;
                    builder.setMessage(LocaleController.formatString("ClearSearchSingleChatAlertText", R.string.ClearSearchSingleChatAlertText, tLRPC$Chat.title));
                    j = -tLRPC$Chat.id;
                } else if (item instanceof TLRPC$User) {
                    TLRPC$User tLRPC$User = (TLRPC$User) item;
                    if (tLRPC$User.id == getUserConfig().clientUserId) {
                        builder.setMessage(LocaleController.formatString("ClearSearchSingleChatAlertText", R.string.ClearSearchSingleChatAlertText, LocaleController.getString("SavedMessages", R.string.SavedMessages)));
                    } else {
                        builder.setMessage(LocaleController.formatString("ClearSearchSingleUserAlertText", R.string.ClearSearchSingleUserAlertText, ContactsController.formatName(tLRPC$User.first_name, tLRPC$User.last_name)));
                    }
                    j = tLRPC$User.id;
                } else if (!(item instanceof TLRPC$EncryptedChat)) {
                    return false;
                } else {
                    TLRPC$EncryptedChat tLRPC$EncryptedChat = (TLRPC$EncryptedChat) item;
                    TLRPC$User user = getMessagesController().getUser(Long.valueOf(tLRPC$EncryptedChat.user_id));
                    builder.setMessage(LocaleController.formatString("ClearSearchSingleUserAlertText", R.string.ClearSearchSingleUserAlertText, ContactsController.formatName(user.first_name, user.last_name)));
                    j = DialogObject.makeEncryptedDialogId(tLRPC$EncryptedChat.id);
                }
                builder.setPositiveButton(LocaleController.getString("ClearSearchRemove", R.string.ClearSearchRemove).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public final void onClick(DialogInterface dialogInterface, int i4) {
                        DialogsActivity.this.lambda$onItemLongClick$22(j, dialogInterface, i4);
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                AlertDialog create = builder.create();
                showDialog(create);
                TextView textView = (TextView) create.getButton(-1);
                if (textView != null) {
                    textView.setTextColor(Theme.getColor("dialogTextRed2"));
                }
                return true;
            }
        }
        DialogsSearchAdapter dialogsSearchAdapter2 = this.searchViewPager.dialogsSearchAdapter;
        if (adapter != dialogsSearchAdapter2) {
            ArrayList<TLRPC$Dialog> dialogsArray = getDialogsArray(this.currentAccount, i2, this.folderId, this.dialogsListFrozen);
            int fixPosition = ((DialogsAdapter) adapter).fixPosition(i);
            if (fixPosition < 0 || fixPosition >= dialogsArray.size() || (tLRPC$Dialog = dialogsArray.get(fixPosition)) == null) {
                return false;
            }
            if (this.onlySelect) {
                int i4 = this.initialDialogsType;
                if ((i4 != 3 && i4 != 10) || !validateSlowModeDialog(tLRPC$Dialog.id)) {
                    return false;
                }
                addOrRemoveSelectedDialog(tLRPC$Dialog.id, view);
                updateSelectedCount();
                return true;
            } else if (tLRPC$Dialog instanceof TLRPC$TL_dialogFolder) {
                view.performHapticFeedback(0, 2);
                BottomSheet.Builder builder2 = new BottomSheet.Builder(getParentActivity());
                boolean z = getMessagesStorage().getArchiveUnreadCount() != 0;
                int[] iArr = new int[2];
                iArr[0] = z ? R.drawable.menu_read : 0;
                iArr[1] = SharedConfig.archiveHidden ? R.drawable.chats_pin : R.drawable.chats_unpin;
                CharSequence[] charSequenceArr = new CharSequence[2];
                if (z) {
                    str2 = LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead);
                }
                charSequenceArr[0] = str2;
                if (SharedConfig.archiveHidden) {
                    i3 = R.string.PinInTheList;
                    str = "PinInTheList";
                } else {
                    i3 = R.string.HideAboveTheList;
                    str = "HideAboveTheList";
                }
                charSequenceArr[1] = LocaleController.getString(str, i3);
                builder2.setItems(charSequenceArr, iArr, new DialogInterface.OnClickListener() {
                    @Override
                    public final void onClick(DialogInterface dialogInterface, int i5) {
                        DialogsActivity.this.lambda$onItemLongClick$23(dialogInterface, i5);
                    }
                });
                showDialog(builder2.create());
                return false;
            } else if (this.actionBar.isActionModeShowed() && isDialogPinned(tLRPC$Dialog)) {
                return false;
            } else {
                showOrUpdateActionMode(tLRPC$Dialog.id, view);
                return true;
            }
        } else if (this.onlySelect) {
            onItemClick(view, i, adapter);
            return false;
        } else {
            long dialogId = (!(view instanceof ProfileSearchCell) || dialogsSearchAdapter2.isGlobalSearch(i)) ? 0L : ((ProfileSearchCell) view).getDialogId();
            if (dialogId == 0) {
                return false;
            }
            showOrUpdateActionMode(dialogId, view);
            return true;
        }
    }

    public void lambda$onItemLongClick$22(long j, DialogInterface dialogInterface, int i) {
        this.searchViewPager.dialogsSearchAdapter.removeRecentSearch(j);
    }

    public void lambda$onItemLongClick$23(DialogInterface dialogInterface, int i) {
        if (i == 0) {
            getMessagesStorage().readAllDialogs(1);
        } else if (i == 1 && this.viewPages != null) {
            int i2 = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i2 < viewPageArr.length) {
                    if (viewPageArr[i2].dialogsType == 0 && this.viewPages[i2].getVisibility() == 0) {
                        View childAt = this.viewPages[i2].listView.getChildAt(0);
                        DialogCell dialogCell = null;
                        if (childAt instanceof DialogCell) {
                            DialogCell dialogCell2 = (DialogCell) childAt;
                            if (dialogCell2.isFolderCell()) {
                                dialogCell = dialogCell2;
                            }
                        }
                        this.viewPages[i2].listView.toggleArchiveHidden(true, dialogCell);
                    }
                    i2++;
                } else {
                    return;
                }
            }
        }
    }

    public boolean showChatPreview(DialogCell dialogCell) {
        TLRPC$Chat chat;
        long dialogId = dialogCell.getDialogId();
        Bundle bundle = new Bundle();
        int messageId = dialogCell.getMessageId();
        if (DialogObject.isEncryptedDialog(dialogId)) {
            return false;
        }
        if (DialogObject.isUserDialog(dialogId)) {
            bundle.putLong("user_id", dialogId);
        } else {
            if (!(messageId == 0 || (chat = getMessagesController().getChat(Long.valueOf(-dialogId))) == null || chat.migrated_to == null)) {
                bundle.putLong("migrated_to", dialogId);
                dialogId = -chat.migrated_to.channel_id;
            }
            bundle.putLong("chat_id", -dialogId);
        }
        if (messageId != 0) {
            bundle.putInt("message_id", messageId);
        }
        if (this.searchString != null) {
            if (!getMessagesController().checkCanOpenChat(bundle, this)) {
                return true;
            }
            getNotificationCenter().postNotificationName(NotificationCenter.closeChats, new Object[0]);
            prepareBlurBitmap();
            presentFragmentAsPreview(new ChatActivity(bundle));
            return true;
        } else if (!getMessagesController().checkCanOpenChat(bundle, this)) {
            return true;
        } else {
            prepareBlurBitmap();
            presentFragmentAsPreview(new ChatActivity(bundle));
            return true;
        }
    }

    public void updateFloatingButtonOffset() {
        this.floatingButtonContainer.setTranslationY(this.floatingButtonTranslation - (Math.max(this.additionalFloatingTranslation, this.additionalFloatingTranslation2) * (1.0f - this.floatingButtonHideProgress)));
    }

    public boolean hasHiddenArchive() {
        return !this.onlySelect && this.initialDialogsType == 0 && this.folderId == 0 && getMessagesController().hasHiddenArchive();
    }

    public boolean waitingForDialogsAnimationEnd(ViewPage viewPage) {
        return (!viewPage.dialogsItemAnimator.isRunning() && this.dialogRemoveFinished == 0 && this.dialogInsertFinished == 0 && this.dialogChangeFinished == 0) ? false : true;
    }

    public void onDialogAnimationFinished() {
        this.dialogRemoveFinished = 0;
        this.dialogInsertFinished = 0;
        this.dialogChangeFinished = 0;
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public final void run() {
                DialogsActivity.this.lambda$onDialogAnimationFinished$24();
            }
        });
    }

    public void lambda$onDialogAnimationFinished$24() {
        ArrayList<TLRPC$Dialog> arrayList;
        if (!(this.viewPages == null || this.folderId == 0 || ((arrayList = this.frozenDialogsList) != null && !arrayList.isEmpty()))) {
            int i = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i >= viewPageArr.length) {
                    break;
                }
                viewPageArr[i].listView.setEmptyView(null);
                this.viewPages[i].progressView.setVisibility(4);
                i++;
            }
            finishFragment();
        }
        setDialogsListFrozen(false);
        updateDialogIndices();
    }

    public void setScrollY(float f) {
        View view = this.scrimView;
        if (view != null) {
            view.getLocationInWindow(this.scrimViewLocation);
        }
        this.actionBar.setTranslationY(f);
        FilterTabsView filterTabsView = this.filterTabsView;
        if (filterTabsView != null) {
            filterTabsView.setTranslationY(f);
        }
        updateContextViewPosition();
        if (this.viewPages != null) {
            int i = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i >= viewPageArr.length) {
                    break;
                }
                viewPageArr[i].listView.setTopGlowOffset(this.viewPages[i].listView.getPaddingTop() + ((int) f));
                i++;
            }
        }
        this.fragmentView.invalidate();
    }

    private void prepareBlurBitmap() {
        if (this.blurredView != null) {
            int measuredWidth = (int) (this.fragmentView.getMeasuredWidth() / 6.0f);
            int measuredHeight = (int) (this.fragmentView.getMeasuredHeight() / 6.0f);
            Bitmap createBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(createBitmap);
            canvas.scale(0.16666667f, 0.16666667f);
            this.fragmentView.draw(canvas);
            Utilities.stackBlurBitmap(createBitmap, Math.max(7, Math.max(measuredWidth, measuredHeight) / 180));
            this.blurredView.setBackground(new BitmapDrawable(createBitmap));
            this.blurredView.setAlpha(0.0f);
            this.blurredView.setVisibility(0);
        }
    }

    @Override
    public void onTransitionAnimationProgress(boolean z, float f) {
        View view = this.blurredView;
        if (view != null && view.getVisibility() == 0) {
            if (z) {
                this.blurredView.setAlpha(1.0f - f);
            } else {
                this.blurredView.setAlpha(f);
            }
        }
    }

    @Override
    public void onTransitionAnimationEnd(boolean z, boolean z2) {
        View view;
        if (z && (view = this.blurredView) != null && view.getVisibility() == 0) {
            this.blurredView.setVisibility(8);
            this.blurredView.setBackground(null);
        }
        if (z && this.afterSignup) {
            try {
                this.fragmentView.performHapticFeedback(3, 2);
            } catch (Exception unused) {
            }
            if (getParentActivity() instanceof LaunchActivity) {
                ((LaunchActivity) getParentActivity()).getFireworksOverlay().start();
            }
        }
    }

    public void resetScroll() {
        if (this.actionBar.getTranslationY() != 0.0f) {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(ObjectAnimator.ofFloat(this, this.SCROLL_Y, 0.0f));
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.setDuration(180L);
            animatorSet.start();
        }
    }

    public void hideActionMode(boolean z) {
        this.actionBar.hideActionMode();
        if (this.menuDrawable != null) {
            this.actionBar.setBackButtonContentDescription(LocaleController.getString("AccDescrOpenMenu", R.string.AccDescrOpenMenu));
        }
        this.selectedDialogs.clear();
        MenuDrawable menuDrawable = this.menuDrawable;
        if (menuDrawable != null) {
            menuDrawable.setRotation(0.0f, true);
        } else {
            BackDrawable backDrawable = this.backDrawable;
            if (backDrawable != null) {
                backDrawable.setRotation(0.0f, true);
            }
        }
        FilterTabsView filterTabsView = this.filterTabsView;
        if (filterTabsView != null) {
            filterTabsView.animateColorsTo("actionBarTabLine", "actionBarTabActiveText", "actionBarTabUnactiveText", "actionBarTabSelector", "actionBarDefault");
        }
        ValueAnimator valueAnimator = this.actionBarColorAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        int i = 0;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.progressToActionMode, 0.0f);
        this.actionBarColorAnimator = ofFloat;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                DialogsActivity.this.lambda$hideActionMode$25(valueAnimator2);
            }
        });
        this.actionBarColorAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
        this.actionBarColorAnimator.setDuration(200L);
        this.actionBarColorAnimator.start();
        this.allowMoving = false;
        if (!this.movingDialogFilters.isEmpty()) {
            int size = this.movingDialogFilters.size();
            for (int i2 = 0; i2 < size; i2++) {
                MessagesController.DialogFilter dialogFilter = this.movingDialogFilters.get(i2);
                size = size;
                FilterCreateActivity.saveFilterToServer(dialogFilter, dialogFilter.flags, dialogFilter.name, dialogFilter.alwaysShow, dialogFilter.neverShow, dialogFilter.pinnedDialogs, false, false, true, true, false, this, null);
            }
            this.movingDialogFilters.clear();
        }
        if (this.movingWas) {
            getMessagesController().reorderPinnedDialogs(this.folderId, null, 0L);
            this.movingWas = false;
        }
        updateCounters(true);
        if (this.viewPages != null) {
            int i3 = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i3 >= viewPageArr.length) {
                    break;
                }
                viewPageArr[i3].dialogsAdapter.onReorderStateChanged(false);
                i3++;
            }
        }
        int i4 = MessagesController.UPDATE_MASK_REORDER | MessagesController.UPDATE_MASK_CHECK;
        if (z) {
            i = MessagesController.UPDATE_MASK_CHAT;
        }
        updateVisibleRows(i4 | i);
    }

    public void lambda$hideActionMode$25(ValueAnimator valueAnimator) {
        this.progressToActionMode = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        for (int i = 0; i < this.actionBar.getChildCount(); i++) {
            if (!(this.actionBar.getChildAt(i).getVisibility() != 0 || this.actionBar.getChildAt(i) == this.actionBar.getActionMode() || this.actionBar.getChildAt(i) == this.actionBar.getBackButton())) {
                this.actionBar.getChildAt(i).setAlpha(1.0f - this.progressToActionMode);
            }
        }
        View view = this.fragmentView;
        if (view != null) {
            view.invalidate();
        }
    }

    private int getPinnedCount() {
        ArrayList<TLRPC$Dialog> arrayList;
        if ((this.viewPages[0].dialogsType == 7 || this.viewPages[0].dialogsType == 8) && (!this.actionBar.isActionModeShowed() || this.actionBar.isActionModeShowed(null))) {
            arrayList = getDialogsArray(this.currentAccount, this.viewPages[0].dialogsType, this.folderId, this.dialogsListFrozen);
        } else {
            arrayList = getMessagesController().getDialogs(this.folderId);
        }
        int size = arrayList.size();
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            TLRPC$Dialog tLRPC$Dialog = arrayList.get(i2);
            if (!(tLRPC$Dialog instanceof TLRPC$TL_dialogFolder)) {
                if (isDialogPinned(tLRPC$Dialog)) {
                    i++;
                } else if (!getMessagesController().isPromoDialog(tLRPC$Dialog.id, false)) {
                    break;
                }
            }
        }
        return i;
    }

    public boolean isDialogPinned(TLRPC$Dialog tLRPC$Dialog) {
        MessagesController.DialogFilter dialogFilter = null;
        if ((this.viewPages[0].dialogsType == 7 || this.viewPages[0].dialogsType == 8) && (!this.actionBar.isActionModeShowed() || this.actionBar.isActionModeShowed(null))) {
            dialogFilter = getMessagesController().selectedDialogFilter[this.viewPages[0].dialogsType == 8 ? (char) 1 : (char) 0];
        }
        if (dialogFilter != null) {
            return dialogFilter.pinnedDialogs.indexOfKey(tLRPC$Dialog.id) >= 0;
        }
        return tLRPC$Dialog.pinned;
    }

    public void performSelectedDialogsAction(final java.util.ArrayList<java.lang.Long> r40, final int r41, boolean r42) {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.performSelectedDialogsAction(java.util.ArrayList, int, boolean):void");
    }

    public void lambda$performSelectedDialogsAction$26(ArrayList arrayList) {
        getMessagesController().addDialogToFolder(arrayList, this.folderId == 0 ? 0 : 1, -1, null, 0L);
    }

    public void lambda$performSelectedDialogsAction$27(DialogInterface dialogInterface, int i) {
        presentFragment(new FiltersSetupActivity());
    }

    public void lambda$performSelectedDialogsAction$29(ArrayList arrayList, final int i, DialogInterface dialogInterface, int i2) {
        if (!arrayList.isEmpty()) {
            final ArrayList<Long> arrayList2 = new ArrayList<>(arrayList);
            getUndoView().showWithAction(arrayList2, i == 102 ? 27 : 26, (Object) null, (Object) null, new Runnable() {
                @Override
                public final void run() {
                    DialogsActivity.this.lambda$performSelectedDialogsAction$28(i, arrayList2);
                }
            }, (Runnable) null);
            hideActionMode(i == 103);
        }
    }

    public void lambda$performSelectedDialogsAction$28(int i, ArrayList arrayList) {
        if (i == 102) {
            getMessagesController().setDialogsInTransaction(true);
            performSelectedDialogsAction(arrayList, i, false);
            getMessagesController().setDialogsInTransaction(false);
            getMessagesController().checkIfFolderEmpty(this.folderId);
            if (this.folderId != 0 && getDialogsArray(this.currentAccount, this.viewPages[0].dialogsType, this.folderId, false).size() == 0) {
                this.viewPages[0].listView.setEmptyView(null);
                this.viewPages[0].progressView.setVisibility(4);
                finishFragment();
                return;
            }
            return;
        }
        performSelectedDialogsAction(arrayList, i, false);
    }

    public void lambda$performSelectedDialogsAction$30(ArrayList arrayList, boolean z, boolean z2) {
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            long longValue = ((Long) arrayList.get(i)).longValue();
            if (z) {
                getMessagesController().reportSpam(longValue, getMessagesController().getUser(Long.valueOf(longValue)), null, null, false);
            }
            if (z2) {
                getMessagesController().deleteDialog(longValue, 0, true);
            }
            getMessagesController().blockPeer(longValue);
        }
        hideActionMode(false);
    }

    public void lambda$performSelectedDialogsAction$31(DialogInterface dialogInterface, int i) {
        getMessagesController().hidePromoDialog();
        hideActionMode(false);
    }

    public void lambda$performSelectedDialogsAction$33(final int i, final TLRPC$Chat tLRPC$Chat, final long j, final boolean z, final boolean z2) {
        int i2;
        int i3;
        int i4;
        ArrayList<TLRPC$Dialog> arrayList;
        hideActionMode(false);
        if (i != 103 || !ChatObject.isChannel(tLRPC$Chat) || (tLRPC$Chat.megagroup && TextUtils.isEmpty(tLRPC$Chat.username))) {
            if (i == 102 && this.folderId != 0 && getDialogsArray(this.currentAccount, this.viewPages[0].dialogsType, this.folderId, false).size() == 1) {
                this.viewPages[0].progressView.setVisibility(4);
            }
            this.debugLastUpdateAction = 3;
            if (i == 102) {
                setDialogsListFrozen(true);
                if (this.frozenDialogsList != null) {
                    for (int i5 = 0; i5 < this.frozenDialogsList.size(); i5++) {
                        if (this.frozenDialogsList.get(i5).id == j) {
                            i2 = i5;
                            break;
                        }
                    }
                }
            }
            i2 = -1;
            int i6 = i2;
            getUndoView().showWithAction(j, i == 103 ? 0 : 1, new Runnable() {
                @Override
                public final void run() {
                    DialogsActivity.this.lambda$performSelectedDialogsAction$32(i, j, tLRPC$Chat, z, z2);
                }
            });
            ArrayList arrayList2 = new ArrayList(getDialogsArray(this.currentAccount, this.viewPages[0].dialogsType, this.folderId, false));
            int i7 = 0;
            while (true) {
                if (i7 >= arrayList2.size()) {
                    i3 = 102;
                    i4 = -1;
                    break;
                } else if (((TLRPC$Dialog) arrayList2.get(i7)).id == j) {
                    i4 = i7;
                    i3 = 102;
                    break;
                } else {
                    i7++;
                }
            }
            if (i != i3) {
                return;
            }
            if (i6 < 0 || i4 >= 0 || (arrayList = this.frozenDialogsList) == null) {
                setDialogsListFrozen(false);
                return;
            }
            arrayList.remove(i6);
            this.viewPages[0].dialogsItemAnimator.prepareForRemove();
            this.viewPages[0].dialogsAdapter.notifyItemRemoved(i6);
            this.dialogRemoveFinished = 2;
            return;
        }
        getMessagesController().deleteDialog(j, 2, z2);
    }

    public void lambda$performSelectedDialogsAction$34(DialogInterface dialogInterface) {
        hideActionMode(true);
    }

    public void lambda$performSelectedDialogsAction$32(int i, long j, TLRPC$Chat tLRPC$Chat, boolean z, boolean z2) {
        if (i == 103) {
            getMessagesController().deleteDialog(j, 1, z2);
            return;
        }
        if (tLRPC$Chat == null) {
            getMessagesController().deleteDialog(j, 0, z2);
            if (z) {
                getMessagesController().blockPeer((int) j);
            }
        } else if (ChatObject.isNotInChat(tLRPC$Chat)) {
            getMessagesController().deleteDialog(j, 0, z2);
        } else {
            getMessagesController().deleteParticipantFromChat((int) (-j), getMessagesController().getUser(Long.valueOf(getUserConfig().getClientUserId())), null, null, z2, false);
        }
        if (AndroidUtilities.isTablet()) {
            getNotificationCenter().postNotificationName(NotificationCenter.closeChats, Long.valueOf(j));
        }
        getMessagesController().checkIfFolderEmpty(this.folderId);
    }

    private void pinDialog(long r16, boolean r18, org.telegram.messenger.MessagesController.DialogFilter r19, int r20, boolean r21) {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.pinDialog(long, boolean, org.telegram.messenger.MessagesController$DialogFilter, int, boolean):void");
    }

    public void lambda$pinDialog$35() {
        setDialogsListFrozen(false);
    }

    public void scrollToTop() {
        int findFirstVisibleItemPosition = this.viewPages[0].layoutManager.findFirstVisibleItemPosition() * AndroidUtilities.dp(SharedConfig.useThreeLinesLayout ? 78.0f : 72.0f);
        int i = (this.viewPages[0].dialogsType != 0 || !hasHiddenArchive()) ? 0 : 1;
        this.viewPages[0].listView.getItemAnimator();
        if (findFirstVisibleItemPosition >= this.viewPages[0].listView.getMeasuredHeight() * 1.2f) {
            this.viewPages[0].scrollHelper.setScrollDirection(1);
            this.viewPages[0].scrollHelper.scrollToPosition(i, 0, false, true);
            resetScroll();
            return;
        }
        this.viewPages[0].listView.smoothScrollToPosition(i);
    }

    public void updateCounters(boolean z) {
        int i;
        int i2;
        int i3;
        int i4;
        TLRPC$User tLRPC$User;
        this.canDeletePsaSelected = false;
        this.canUnarchiveCount = 0;
        this.canUnmuteCount = 0;
        this.canMuteCount = 0;
        this.canPinCount = 0;
        this.canReadCount = 0;
        this.canClearCacheCount = 0;
        this.canReportSpamCount = 0;
        if (!z) {
            int size = this.selectedDialogs.size();
            long clientUserId = getUserConfig().getClientUserId();
            SharedPreferences notificationsSettings = getNotificationsSettings();
            int i5 = 0;
            int i6 = 0;
            int i7 = 0;
            int i8 = 0;
            int i9 = 0;
            int i10 = 0;
            while (i5 < size) {
                TLRPC$Dialog tLRPC$Dialog = getMessagesController().dialogs_dict.get(this.selectedDialogs.get(i5).longValue());
                if (tLRPC$Dialog == null) {
                    i2 = size;
                    i3 = i5;
                } else {
                    long j = tLRPC$Dialog.id;
                    boolean isDialogPinned = isDialogPinned(tLRPC$Dialog);
                    i2 = size;
                    boolean z2 = tLRPC$Dialog.unread_count != 0 || tLRPC$Dialog.unread_mark;
                    if (getMessagesController().isDialogMuted(j)) {
                        i3 = i5;
                        i4 = 1;
                        this.canUnmuteCount++;
                    } else {
                        i3 = i5;
                        i4 = 1;
                        this.canMuteCount++;
                    }
                    if (z2) {
                        this.canReadCount += i4;
                    }
                    if (this.folderId == i4 || tLRPC$Dialog.folder_id == i4) {
                        this.canUnarchiveCount++;
                    } else if (!(j == clientUserId || j == 777000 || getMessagesController().isPromoDialog(j, false))) {
                        i8++;
                    }
                    if (!DialogObject.isUserDialog(j) || j == clientUserId || MessagesController.isSupportUser(getMessagesController().getUser(Long.valueOf(j)))) {
                        i10++;
                    } else {
                        if (notificationsSettings.getBoolean("dialog_bar_report" + j, true)) {
                            this.canReportSpamCount++;
                        }
                    }
                    if (DialogObject.isChannel(tLRPC$Dialog)) {
                        TLRPC$Chat chat = getMessagesController().getChat(Long.valueOf(-j));
                        if (getMessagesController().isPromoDialog(tLRPC$Dialog.id, true)) {
                            this.canClearCacheCount++;
                            if (getMessagesController().promoDialogType == MessagesController.PROMO_TYPE_PSA) {
                                i6++;
                                this.canDeletePsaSelected = true;
                            }
                        } else {
                            if (isDialogPinned) {
                                i9++;
                            } else {
                                this.canPinCount++;
                            }
                            if (chat == null || !chat.megagroup) {
                                this.canClearCacheCount++;
                            } else if (!TextUtils.isEmpty(chat.username)) {
                                this.canClearCacheCount++;
                            }
                            i6++;
                        }
                    } else {
                        boolean isChatDialog = DialogObject.isChatDialog(tLRPC$Dialog.id);
                        if (isChatDialog) {
                            getMessagesController().getChat(Long.valueOf(-tLRPC$Dialog.id));
                        }
                        if (DialogObject.isEncryptedDialog(tLRPC$Dialog.id)) {
                            TLRPC$EncryptedChat encryptedChat = getMessagesController().getEncryptedChat(Integer.valueOf(DialogObject.getEncryptedChatId(tLRPC$Dialog.id)));
                            if (encryptedChat != null) {
                                tLRPC$User = getMessagesController().getUser(Long.valueOf(encryptedChat.user_id));
                            } else {
                                tLRPC$User = new TLRPC$TL_userEmpty();
                            }
                        } else {
                            tLRPC$User = (isChatDialog || !DialogObject.isUserDialog(tLRPC$Dialog.id)) ? null : getMessagesController().getUser(Long.valueOf(tLRPC$Dialog.id));
                        }
                        if (tLRPC$User != null && tLRPC$User.bot) {
                            MessagesController.isSupportUser(tLRPC$User);
                        }
                        if (isDialogPinned) {
                            i9++;
                        } else {
                            this.canPinCount++;
                        }
                    }
                    i7++;
                    i6++;
                }
                i5 = i3 + 1;
                size = i2;
            }
            if (i6 != size) {
                this.deleteItem.setVisibility(8);
            } else {
                this.deleteItem.setVisibility(0);
            }
            int i11 = this.canClearCacheCount;
            if ((i11 == 0 || i11 == size) && (i7 == 0 || i7 == size)) {
                this.clearItem.setVisibility(0);
                if (this.canClearCacheCount != 0) {
                    this.clearItem.setText(LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache));
                } else {
                    this.clearItem.setText(LocaleController.getString("ClearHistory", R.string.ClearHistory));
                }
            } else {
                this.clearItem.setVisibility(8);
            }
            if (this.canUnarchiveCount != 0) {
                String string = LocaleController.getString("Unarchive", R.string.Unarchive);
                this.archiveItem.setTextAndIcon(string, R.drawable.msg_unarchive);
                this.archive2Item.setIcon(R.drawable.msg_unarchive);
                this.archive2Item.setContentDescription(string);
                FilterTabsView filterTabsView = this.filterTabsView;
                if (filterTabsView == null || filterTabsView.getVisibility() != 0) {
                    this.archiveItem.setVisibility(0);
                    this.archive2Item.setVisibility(8);
                } else {
                    this.archive2Item.setVisibility(0);
                    this.archiveItem.setVisibility(8);
                }
            } else if (i8 != 0) {
                String string2 = LocaleController.getString("Archive", R.string.Archive);
                this.archiveItem.setTextAndIcon(string2, R.drawable.msg_archive);
                this.archive2Item.setIcon(R.drawable.msg_archive);
                this.archive2Item.setContentDescription(string2);
                FilterTabsView filterTabsView2 = this.filterTabsView;
                if (filterTabsView2 == null || filterTabsView2.getVisibility() != 0) {
                    this.archiveItem.setVisibility(0);
                    this.archive2Item.setVisibility(8);
                } else {
                    this.archive2Item.setVisibility(0);
                    this.archiveItem.setVisibility(8);
                }
            } else {
                this.archiveItem.setVisibility(8);
                this.archive2Item.setVisibility(8);
            }
            if (this.canPinCount + i9 != size) {
                this.pinItem.setVisibility(8);
                this.pin2Item.setVisibility(8);
                i = 0;
            } else {
                FilterTabsView filterTabsView3 = this.filterTabsView;
                if (filterTabsView3 == null || filterTabsView3.getVisibility() != 0) {
                    i = 0;
                    this.pinItem.setVisibility(0);
                    this.pin2Item.setVisibility(8);
                } else {
                    i = 0;
                    this.pin2Item.setVisibility(0);
                    this.pinItem.setVisibility(8);
                }
            }
            if (i10 != 0) {
                this.blockItem.setVisibility(8);
            } else {
                this.blockItem.setVisibility(i);
            }
            FilterTabsView filterTabsView4 = this.filterTabsView;
            if (filterTabsView4 == null || filterTabsView4.getVisibility() != 0 || this.filterTabsView.getCurrentTabId() == Integer.MAX_VALUE) {
                this.removeFromFolderItem.setVisibility(8);
            } else {
                this.removeFromFolderItem.setVisibility(0);
            }
            FilterTabsView filterTabsView5 = this.filterTabsView;
            if (filterTabsView5 == null || filterTabsView5.getVisibility() != 0 || this.filterTabsView.getCurrentTabId() != Integer.MAX_VALUE || FiltersListBottomSheet.getCanAddDialogFilters(this, this.selectedDialogs).isEmpty()) {
                this.addToFolderItem.setVisibility(8);
            } else {
                this.addToFolderItem.setVisibility(0);
            }
            if (this.canUnmuteCount != 0) {
                this.muteItem.setIcon(R.drawable.msg_unmute);
                this.muteItem.setContentDescription(LocaleController.getString("ChatsUnmute", R.string.ChatsUnmute));
            } else {
                this.muteItem.setIcon(R.drawable.msg_mute);
                this.muteItem.setContentDescription(LocaleController.getString("ChatsMute", R.string.ChatsMute));
            }
            if (this.canReadCount != 0) {
                this.readItem.setTextAndIcon(LocaleController.getString("MarkAsRead", R.string.MarkAsRead), R.drawable.msg_markread);
            } else {
                this.readItem.setTextAndIcon(LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread), R.drawable.msg_markunread);
            }
            if (this.canPinCount != 0) {
                this.pinItem.setIcon(R.drawable.msg_pin);
                this.pinItem.setContentDescription(LocaleController.getString("PinToTop", R.string.PinToTop));
                this.pin2Item.setText(LocaleController.getString("DialogPin", R.string.DialogPin));
                return;
            }
            this.pinItem.setIcon(R.drawable.msg_unpin);
            this.pinItem.setContentDescription(LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop));
            this.pin2Item.setText(LocaleController.getString("DialogUnpin", R.string.DialogUnpin));
        }
    }

    public boolean validateSlowModeDialog(long j) {
        TLRPC$Chat chat;
        ChatActivityEnterView chatActivityEnterView;
        if ((this.messagesCount <= 1 && ((chatActivityEnterView = this.commentView) == null || chatActivityEnterView.getVisibility() != 0 || TextUtils.isEmpty(this.commentView.getFieldText()))) || !DialogObject.isChatDialog(j) || (chat = getMessagesController().getChat(Long.valueOf(-j))) == null || ChatObject.hasAdminRights(chat) || !chat.slowmode_enabled) {
            return true;
        }
        AlertsCreator.showSimpleAlert(this, LocaleController.getString("Slowmode", R.string.Slowmode), LocaleController.getString("SlowmodeSendError", R.string.SlowmodeSendError));
        return false;
    }

    private void showOrUpdateActionMode(long j, View view) {
        addOrRemoveSelectedDialog(j, view);
        boolean z = true;
        if (!this.actionBar.isActionModeShowed()) {
            if (this.searchIsShowed) {
                createActionMode("search_dialogs_action_mode");
                if (this.actionBar.getBackButton().getDrawable() instanceof MenuDrawable) {
                    this.actionBar.setBackButtonDrawable(new BackDrawable(false));
                }
            } else {
                createActionMode(null);
            }
            AndroidUtilities.hideKeyboard(this.fragmentView.findFocus());
            this.actionBar.setActionModeOverrideColor(Theme.getColor("windowBackgroundWhite"));
            this.actionBar.showActionMode();
            resetScroll();
            if (this.menuDrawable != null) {
                this.actionBar.setBackButtonContentDescription(LocaleController.getString("AccDescrGoBack", R.string.AccDescrGoBack));
            }
            if (getPinnedCount() > 1) {
                if (this.viewPages != null) {
                    int i = 0;
                    while (true) {
                        ViewPage[] viewPageArr = this.viewPages;
                        if (i >= viewPageArr.length) {
                            break;
                        }
                        viewPageArr[i].dialogsAdapter.onReorderStateChanged(true);
                        i++;
                    }
                }
                updateVisibleRows(MessagesController.UPDATE_MASK_REORDER);
            }
            if (!this.searchIsShowed) {
                AnimatorSet animatorSet = new AnimatorSet();
                ArrayList arrayList = new ArrayList();
                for (int i2 = 0; i2 < this.actionModeViews.size(); i2++) {
                    View view2 = this.actionModeViews.get(i2);
                    view2.setPivotY(ActionBar.getCurrentActionBarHeight() / 2);
                    AndroidUtilities.clearDrawableAnimation(view2);
                    arrayList.add(ObjectAnimator.ofFloat(view2, View.SCALE_Y, 0.1f, 1.0f));
                }
                animatorSet.playTogether(arrayList);
                animatorSet.setDuration(200L);
                animatorSet.start();
            }
            ValueAnimator valueAnimator = this.actionBarColorAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.progressToActionMode, 1.0f);
            this.actionBarColorAnimator = ofFloat;
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                    DialogsActivity.this.lambda$showOrUpdateActionMode$36(valueAnimator2);
                }
            });
            this.actionBarColorAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
            this.actionBarColorAnimator.setDuration(200L);
            this.actionBarColorAnimator.start();
            FilterTabsView filterTabsView = this.filterTabsView;
            if (filterTabsView != null) {
                filterTabsView.animateColorsTo("profile_tabSelectedLine", "profile_tabSelectedText", "profile_tabText", "profile_tabSelector", "actionBarActionModeDefault");
            }
            MenuDrawable menuDrawable = this.menuDrawable;
            if (menuDrawable != null) {
                menuDrawable.setRotateToBack(false);
                this.menuDrawable.setRotation(1.0f, true);
            } else {
                BackDrawable backDrawable = this.backDrawable;
                if (backDrawable != null) {
                    backDrawable.setRotation(1.0f, true);
                }
            }
            z = false;
        } else if (this.selectedDialogs.isEmpty()) {
            hideActionMode(true);
            return;
        }
        updateCounters(false);
        this.selectedDialogsCountTextView.setNumber(this.selectedDialogs.size(), z);
    }

    public void lambda$showOrUpdateActionMode$36(ValueAnimator valueAnimator) {
        this.progressToActionMode = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        for (int i = 0; i < this.actionBar.getChildCount(); i++) {
            if (!(this.actionBar.getChildAt(i).getVisibility() != 0 || this.actionBar.getChildAt(i) == this.actionBar.getActionMode() || this.actionBar.getChildAt(i) == this.actionBar.getBackButton())) {
                this.actionBar.getChildAt(i).setAlpha(1.0f - this.progressToActionMode);
            }
        }
        View view = this.fragmentView;
        if (view != null) {
            view.invalidate();
        }
    }

    public void closeSearch() {
        if (AndroidUtilities.isTablet()) {
            ActionBar actionBar = this.actionBar;
            if (actionBar != null) {
                actionBar.closeSearchField();
            }
            TLObject tLObject = this.searchObject;
            if (tLObject != null) {
                this.searchViewPager.dialogsSearchAdapter.putRecentSearch(this.searchDialogId, tLObject);
                this.searchObject = null;
                return;
            }
            return;
        }
        this.closeSearchFieldOnHide = true;
    }

    public RecyclerListView getListView() {
        return this.viewPages[0].listView;
    }

    public RecyclerListView getSearchListView() {
        return this.searchViewPager.searchListView;
    }

    public UndoView getUndoView() {
        if (this.undoView[0].getVisibility() == 0) {
            UndoView[] undoViewArr = this.undoView;
            UndoView undoView = undoViewArr[0];
            undoViewArr[0] = undoViewArr[1];
            undoViewArr[1] = undoView;
            undoView.hide(true, 2);
            ContentView contentView = (ContentView) this.fragmentView;
            contentView.removeView(this.undoView[0]);
            contentView.addView(this.undoView[0]);
        }
        return this.undoView[0];
    }

    public void updateProxyButton(boolean z, boolean z2) {
        boolean z3;
        ActionBarMenuItem actionBarMenuItem;
        if (this.proxyDrawable != null) {
            ActionBarMenuItem actionBarMenuItem2 = this.doneItem;
            if (actionBarMenuItem2 == null || actionBarMenuItem2.getVisibility() != 0) {
                boolean z4 = false;
                int i = 0;
                while (true) {
                    if (i >= getDownloadController().downloadingFiles.size()) {
                        z3 = false;
                        break;
                    } else if (getFileLoader().isLoadingFile(getDownloadController().downloadingFiles.get(i).getFileName())) {
                        z3 = true;
                        break;
                    } else {
                        i++;
                    }
                }
                if (this.searching || (!getDownloadController().hasUnviewedDownloads() && !z3 && !(this.downloadsItem.getVisibility() == 0 && this.downloadsItem.getAlpha() == 1.0f && !z2))) {
                    this.downloadsItem.setVisibility(8);
                    this.downloadsItemVisible = false;
                } else {
                    this.downloadsItemVisible = true;
                    this.downloadsItem.setVisibility(0);
                }
                SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                String string = sharedPreferences.getString("proxy_ip", "");
                boolean z5 = sharedPreferences.getBoolean("proxy_enabled", false);
                if ((this.downloadsItemVisible || this.searching || !z5 || TextUtils.isEmpty(string)) && (!getMessagesController().blockedCountry || SharedConfig.proxyList.isEmpty())) {
                    this.proxyItemVisible = false;
                    this.proxyItem.setVisibility(8);
                    return;
                }
                if (!this.actionBar.isSearchFieldVisible() && ((actionBarMenuItem = this.doneItem) == null || actionBarMenuItem.getVisibility() != 0)) {
                    this.proxyItem.setVisibility(0);
                }
                this.proxyItemVisible = true;
                ProxyDrawable proxyDrawable = this.proxyDrawable;
                int i2 = this.currentConnectionState;
                if (i2 == 3 || i2 == 5) {
                    z4 = true;
                }
                proxyDrawable.setConnected(z5, z4, z);
            }
        }
    }

    public void showDoneItem(final boolean z) {
        if (this.doneItem != null) {
            AnimatorSet animatorSet = this.doneItemAnimator;
            if (animatorSet != null) {
                animatorSet.cancel();
                this.doneItemAnimator = null;
            }
            AnimatorSet animatorSet2 = new AnimatorSet();
            this.doneItemAnimator = animatorSet2;
            animatorSet2.setDuration(180L);
            if (z) {
                this.doneItem.setVisibility(0);
            } else {
                this.doneItem.setSelected(false);
                Drawable background = this.doneItem.getBackground();
                if (background != null) {
                    background.setState(StateSet.NOTHING);
                    background.jumpToCurrentState();
                }
                ActionBarMenuItem actionBarMenuItem = this.searchItem;
                if (actionBarMenuItem != null) {
                    actionBarMenuItem.setVisibility(0);
                }
                ActionBarMenuItem actionBarMenuItem2 = this.proxyItem;
                if (actionBarMenuItem2 != null && this.proxyItemVisible) {
                    actionBarMenuItem2.setVisibility(0);
                }
                ActionBarMenuItem actionBarMenuItem3 = this.passcodeItem;
                if (actionBarMenuItem3 != null && this.passcodeItemVisible) {
                    actionBarMenuItem3.setVisibility(0);
                }
                ActionBarMenuItem actionBarMenuItem4 = this.downloadsItem;
                if (actionBarMenuItem4 != null && this.downloadsItemVisible) {
                    actionBarMenuItem4.setVisibility(0);
                }
            }
            ArrayList arrayList = new ArrayList();
            ActionBarMenuItem actionBarMenuItem5 = this.doneItem;
            Property property = View.ALPHA;
            float[] fArr = new float[1];
            float f = 1.0f;
            fArr[0] = z ? 1.0f : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem5, property, fArr));
            if (this.proxyItemVisible) {
                ActionBarMenuItem actionBarMenuItem6 = this.proxyItem;
                Property property2 = View.ALPHA;
                float[] fArr2 = new float[1];
                fArr2[0] = z ? 0.0f : 1.0f;
                arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem6, property2, fArr2));
            }
            if (this.passcodeItemVisible) {
                ActionBarMenuItem actionBarMenuItem7 = this.passcodeItem;
                Property property3 = View.ALPHA;
                float[] fArr3 = new float[1];
                fArr3[0] = z ? 0.0f : 1.0f;
                arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem7, property3, fArr3));
            }
            ActionBarMenuItem actionBarMenuItem8 = this.searchItem;
            Property property4 = View.ALPHA;
            float[] fArr4 = new float[1];
            if (z) {
                f = 0.0f;
            }
            fArr4[0] = f;
            arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem8, property4, fArr4));
            this.doneItemAnimator.playTogether(arrayList);
            this.doneItemAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    DialogsActivity.this.doneItemAnimator = null;
                    if (z) {
                        if (DialogsActivity.this.searchItem != null) {
                            DialogsActivity.this.searchItem.setVisibility(4);
                        }
                        if (DialogsActivity.this.proxyItem != null && DialogsActivity.this.proxyItemVisible) {
                            DialogsActivity.this.proxyItem.setVisibility(4);
                        }
                        if (DialogsActivity.this.passcodeItem != null && DialogsActivity.this.passcodeItemVisible) {
                            DialogsActivity.this.passcodeItem.setVisibility(4);
                        }
                        if (DialogsActivity.this.downloadsItem != null && DialogsActivity.this.downloadsItemVisible) {
                            DialogsActivity.this.downloadsItem.setVisibility(4);
                        }
                    } else if (DialogsActivity.this.doneItem != null) {
                        DialogsActivity.this.doneItem.setVisibility(8);
                    }
                }
            });
            this.doneItemAnimator.start();
        }
    }

    public void updateSelectedCount() {
        CharSequence charSequence = "";
        if (this.commentView != null) {
            if (this.selectedDialogs.isEmpty()) {
                if (this.initialDialogsType == 3 && this.selectAlertString == null) {
                    this.actionBar.setTitle(LocaleController.getString("ForwardTo", R.string.ForwardTo));
                } else {
                    this.actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
                }
                if (this.commentView.getTag() != null) {
                    this.commentView.hidePopup(false);
                    this.commentView.closeKeyboard();
                    AnimatorSet animatorSet = this.commentViewAnimator;
                    if (animatorSet != null) {
                        animatorSet.cancel();
                    }
                    this.commentViewAnimator = new AnimatorSet();
                    this.commentView.setTranslationY(0.0f);
                    AnimatorSet animatorSet2 = this.commentViewAnimator;
                    ChatActivityEnterView chatActivityEnterView = this.commentView;
                    animatorSet2.playTogether(ObjectAnimator.ofFloat(chatActivityEnterView, View.TRANSLATION_Y, chatActivityEnterView.getMeasuredHeight()), ObjectAnimator.ofFloat(this.writeButtonContainer, View.SCALE_X, 0.2f), ObjectAnimator.ofFloat(this.writeButtonContainer, View.SCALE_Y, 0.2f), ObjectAnimator.ofFloat(this.writeButtonContainer, View.ALPHA, 0.0f), ObjectAnimator.ofFloat(this.selectedCountView, View.SCALE_X, 0.2f), ObjectAnimator.ofFloat(this.selectedCountView, View.SCALE_Y, 0.2f), ObjectAnimator.ofFloat(this.selectedCountView, View.ALPHA, 0.0f));
                    this.commentViewAnimator.setDuration(180L);
                    this.commentViewAnimator.setInterpolator(new DecelerateInterpolator());
                    this.commentViewAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            DialogsActivity.this.commentView.setVisibility(8);
                            DialogsActivity.this.writeButtonContainer.setVisibility(8);
                        }
                    });
                    this.commentViewAnimator.start();
                    this.commentView.setTag(null);
                    this.fragmentView.requestLayout();
                }
            } else {
                this.selectedCountView.invalidate();
                if (this.commentView.getTag() == null) {
                    this.commentView.setFieldText(charSequence);
                    AnimatorSet animatorSet3 = this.commentViewAnimator;
                    if (animatorSet3 != null) {
                        animatorSet3.cancel();
                    }
                    this.commentView.setVisibility(0);
                    this.writeButtonContainer.setVisibility(0);
                    AnimatorSet animatorSet4 = new AnimatorSet();
                    this.commentViewAnimator = animatorSet4;
                    ChatActivityEnterView chatActivityEnterView2 = this.commentView;
                    animatorSet4.playTogether(ObjectAnimator.ofFloat(chatActivityEnterView2, View.TRANSLATION_Y, chatActivityEnterView2.getMeasuredHeight(), 0.0f), ObjectAnimator.ofFloat(this.writeButtonContainer, View.SCALE_X, 1.0f), ObjectAnimator.ofFloat(this.writeButtonContainer, View.SCALE_Y, 1.0f), ObjectAnimator.ofFloat(this.writeButtonContainer, View.ALPHA, 1.0f), ObjectAnimator.ofFloat(this.selectedCountView, View.SCALE_X, 1.0f), ObjectAnimator.ofFloat(this.selectedCountView, View.SCALE_Y, 1.0f), ObjectAnimator.ofFloat(this.selectedCountView, View.ALPHA, 1.0f));
                    this.commentViewAnimator.setDuration(180L);
                    this.commentViewAnimator.setInterpolator(new DecelerateInterpolator());
                    this.commentViewAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            DialogsActivity.this.commentView.setTag(2);
                            DialogsActivity.this.commentView.requestLayout();
                        }
                    });
                    this.commentViewAnimator.start();
                    this.commentView.setTag(1);
                }
                this.actionBar.setTitle(LocaleController.formatPluralString("Recipient", this.selectedDialogs.size()));
            }
        } else if (this.initialDialogsType == 10) {
            hideFloatingButton(this.selectedDialogs.isEmpty());
        }
        ArrayList<Long> arrayList = this.selectedDialogs;
        ChatActivityEnterView chatActivityEnterView3 = this.commentView;
        if (chatActivityEnterView3 != null) {
            charSequence = chatActivityEnterView3.getFieldText();
        }
        boolean shouldShowNextButton = shouldShowNextButton(this, arrayList, charSequence, false);
        this.isNextButton = shouldShowNextButton;
        AndroidUtilities.updateViewVisibilityAnimated(this.writeButton[0], !shouldShowNextButton, 0.5f, true);
        AndroidUtilities.updateViewVisibilityAnimated(this.writeButton[1], this.isNextButton, 0.5f, true);
    }

    @TargetApi(R.styleable.MapAttrs_zOrderOnTop)
    private void askForPermissons(boolean z) {
        Activity parentActivity = getParentActivity();
        if (parentActivity != null) {
            ArrayList arrayList = new ArrayList();
            if (getUserConfig().syncContacts && this.askAboutContacts && parentActivity.checkSelfPermission("android.permission.READ_CONTACTS") != 0) {
                if (z) {
                    AlertDialog create = AlertsCreator.createContactsPermissionDialog(parentActivity, new MessagesStorage.IntCallback() {
                        @Override
                        public final void run(int i) {
                            DialogsActivity.this.lambda$askForPermissons$37(i);
                        }
                    }).create();
                    this.permissionDialog = create;
                    showDialog(create);
                    return;
                }
                arrayList.add("android.permission.READ_CONTACTS");
                arrayList.add("android.permission.WRITE_CONTACTS");
                arrayList.add("android.permission.GET_ACCOUNTS");
            }
            if ((Build.VERSION.SDK_INT <= 28 || BuildVars.NO_SCOPED_STORAGE) && parentActivity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                arrayList.add("android.permission.READ_EXTERNAL_STORAGE");
                arrayList.add("android.permission.WRITE_EXTERNAL_STORAGE");
            }
            if (!arrayList.isEmpty()) {
                try {
                    parentActivity.requestPermissions((String[]) arrayList.toArray(new String[0]), 1);
                } catch (Exception unused) {
                }
            } else if (this.askingForPermissions) {
                this.askingForPermissions = false;
                showFiltersHint();
            }
        }
    }

    public void lambda$askForPermissons$37(int i) {
        this.askAboutContacts = i != 0;
        MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askAboutContacts", this.askAboutContacts).commit();
        askForPermissons(false);
    }

    @Override
    public void onDialogDismiss(Dialog dialog) {
        super.onDialogDismiss(dialog);
        AlertDialog alertDialog = this.permissionDialog;
        if (alertDialog != null && dialog == alertDialog && getParentActivity() != null) {
            askForPermissons(false);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        FrameLayout frameLayout;
        super.onConfigurationChanged(configuration);
        ActionBarPopupWindow actionBarPopupWindow = this.scrimPopupWindow;
        if (actionBarPopupWindow != null) {
            actionBarPopupWindow.dismiss();
        }
        if (!this.onlySelect && (frameLayout = this.floatingButtonContainer) != null) {
            frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    DialogsActivity dialogsActivity = DialogsActivity.this;
                    dialogsActivity.floatingButtonTranslation = dialogsActivity.floatingHidden ? AndroidUtilities.dp(100.0f) : 0.0f;
                    DialogsActivity.this.updateFloatingButtonOffset();
                    DialogsActivity.this.floatingButtonContainer.setClickable(!DialogsActivity.this.floatingHidden);
                    if (DialogsActivity.this.floatingButtonContainer != null) {
                        DialogsActivity.this.floatingButtonContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResultFragment(int i, String[] strArr, int[] iArr) {
        FilesMigrationService.FilesMigrationBottomSheet filesMigrationBottomSheet;
        boolean z = true;
        if (i == 1) {
            for (int i2 = 0; i2 < strArr.length; i2++) {
                if (iArr.length > i2) {
                    String str = strArr[i2];
                    str.hashCode();
                    if (!str.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
                        if (str.equals("android.permission.READ_CONTACTS")) {
                            if (iArr[i2] == 0) {
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    @Override
                                    public final void run() {
                                        DialogsActivity.this.lambda$onRequestPermissionsResultFragment$38();
                                    }
                                });
                                getContactsController().forceImportContacts();
                            } else {
                                SharedPreferences.Editor edit = MessagesController.getGlobalNotificationsSettings().edit();
                                this.askAboutContacts = false;
                                edit.putBoolean("askAboutContacts", false).commit();
                            }
                        }
                    } else if (iArr[i2] == 0) {
                        ImageLoader.getInstance().checkMediaPaths();
                    }
                }
            }
            if (this.askingForPermissions) {
                this.askingForPermissions = false;
                showFiltersHint();
            }
        } else if (i == 4) {
            int i3 = 0;
            while (true) {
                if (i3 >= iArr.length) {
                    break;
                } else if (iArr[i3] != 0) {
                    z = false;
                    break;
                } else {
                    i3++;
                }
            }
            if (z && Build.VERSION.SDK_INT >= 30 && (filesMigrationBottomSheet = FilesMigrationService.filesMigrationBottomSheet) != null) {
                filesMigrationBottomSheet.migrateOldFolder();
            }
        }
    }

    public void lambda$onRequestPermissionsResultFragment$38() {
        getNotificationCenter().postNotificationName(NotificationCenter.forceImportContactsStart, new Object[0]);
    }

    @Override
    public void didReceivedNotification(int i, int i2, Object... objArr) {
        ViewPage[] viewPageArr;
        DialogsSearchAdapter dialogsSearchAdapter;
        DialogsSearchAdapter dialogsSearchAdapter2;
        int i3;
        int i4;
        int i5 = 0;
        if (i == NotificationCenter.dialogsNeedReload) {
            if (!(this.viewPages == null || this.dialogsListFrozen)) {
                AccountInstance.getInstance(this.currentAccount).getMessagesController().getDialogs(this.folderId);
                int i6 = 0;
                while (true) {
                    ViewPage[] viewPageArr2 = this.viewPages;
                    if (i6 >= viewPageArr2.length) {
                        break;
                    }
                    if (viewPageArr2[i6].getVisibility() == 0) {
                        int currentCount = this.viewPages[i6].dialogsAdapter.getCurrentCount();
                        if (this.viewPages[i6].dialogsType == 0 && hasHiddenArchive() && this.viewPages[i6].listView.getChildCount() == 0) {
                            ((LinearLayoutManager) this.viewPages[i6].listView.getLayoutManager()).scrollToPositionWithOffset(1, 0);
                        }
                        if (this.viewPages[i6].dialogsAdapter.isDataSetChanged() || objArr.length > 0) {
                            this.viewPages[i6].dialogsAdapter.updateHasHints();
                            int itemCount = this.viewPages[i6].dialogsAdapter.getItemCount();
                            if (itemCount != 1 || currentCount != 1 || this.viewPages[i6].dialogsAdapter.getItemViewType(0) != 5) {
                                this.viewPages[i6].dialogsAdapter.notifyDataSetChanged();
                                if (!(itemCount <= currentCount || (i3 = this.initialDialogsType) == 11 || i3 == 12 || i3 == 13)) {
                                    this.viewPages[i6].recyclerItemsEnterAnimator.showItemsAnimated(currentCount);
                                }
                            } else if (this.viewPages[i6].dialogsAdapter.lastDialogsEmptyType != this.viewPages[i6].dialogsAdapter.dialogsEmptyType()) {
                                this.viewPages[i6].dialogsAdapter.notifyItemChanged(0);
                            }
                        } else {
                            updateVisibleRows(MessagesController.UPDATE_MASK_NEW_MESSAGE);
                            if (!(this.viewPages[i6].dialogsAdapter.getItemCount() <= currentCount || (i4 = this.initialDialogsType) == 11 || i4 == 12 || i4 == 13)) {
                                this.viewPages[i6].recyclerItemsEnterAnimator.showItemsAnimated(currentCount);
                            }
                        }
                        try {
                            this.viewPages[i6].listView.setEmptyView(this.folderId == 0 ? this.viewPages[i6].progressView : null);
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                        checkListLoad(this.viewPages[i6]);
                    }
                    i6++;
                }
                FilterTabsView filterTabsView = this.filterTabsView;
                if (filterTabsView != null && filterTabsView.getVisibility() == 0) {
                    this.filterTabsView.checkTabsCounter();
                }
            }
        } else if (i == NotificationCenter.dialogsUnreadCounterChanged) {
            FilterTabsView filterTabsView2 = this.filterTabsView;
            if (filterTabsView2 != null && filterTabsView2.getVisibility() == 0) {
                this.filterTabsView.notifyTabCounterChanged(ConnectionsManager.DEFAULT_DATACENTER_ID);
            }
        } else if (i == NotificationCenter.dialogsUnreadReactionsCounterChanged) {
            updateVisibleRows(0);
        } else if (i == NotificationCenter.emojiLoaded) {
            updateVisibleRows(0);
            FilterTabsView filterTabsView3 = this.filterTabsView;
            if (filterTabsView3 != null) {
                filterTabsView3.getTabsContainer().invalidateViews();
            }
        } else if (i == NotificationCenter.closeSearchByActiveAction) {
            ActionBar actionBar = this.actionBar;
            if (actionBar != null) {
                actionBar.closeSearchField();
            }
        } else if (i == NotificationCenter.proxySettingsChanged) {
            updateProxyButton(false, false);
        } else if (i == NotificationCenter.updateInterfaces) {
            Integer num = (Integer) objArr[0];
            updateVisibleRows(num.intValue());
            FilterTabsView filterTabsView4 = this.filterTabsView;
            if (!(filterTabsView4 == null || filterTabsView4.getVisibility() != 0 || (num.intValue() & MessagesController.UPDATE_MASK_READ_DIALOG_MESSAGE) == 0)) {
                this.filterTabsView.checkTabsCounter();
            }
            if (this.viewPages != null) {
                while (i5 < this.viewPages.length) {
                    if ((num.intValue() & MessagesController.UPDATE_MASK_STATUS) != 0) {
                        this.viewPages[i5].dialogsAdapter.sortOnlineContacts(true);
                    }
                    i5++;
                }
            }
        } else if (i == NotificationCenter.appDidLogout) {
            dialogsLoaded[this.currentAccount] = false;
        } else if (i == NotificationCenter.encryptedChatUpdated) {
            updateVisibleRows(0);
        } else if (i == NotificationCenter.contactsDidLoad) {
            if (!(this.viewPages == null || this.dialogsListFrozen)) {
                boolean z = this.floatingProgressVisible;
                setFloatingProgressVisible(false, true);
                for (ViewPage viewPage : this.viewPages) {
                    viewPage.dialogsAdapter.setForceUpdatingContacts(false);
                }
                if (z) {
                    setContactsAlpha(0.0f);
                    animateContactsAlpha(1.0f);
                }
                int i7 = 0;
                boolean z2 = false;
                while (true) {
                    ViewPage[] viewPageArr3 = this.viewPages;
                    if (i7 >= viewPageArr3.length) {
                        break;
                    }
                    if (!viewPageArr3[i7].isDefaultDialogType() || getMessagesController().getAllFoldersDialogsCount() > 10) {
                        z2 = true;
                    } else {
                        this.viewPages[i7].dialogsAdapter.notifyDataSetChanged();
                    }
                    i7++;
                }
                if (z2) {
                    updateVisibleRows(0);
                }
            }
        } else if (i == NotificationCenter.openedChatChanged) {
            if (this.viewPages != null) {
                int i8 = 0;
                while (true) {
                    ViewPage[] viewPageArr4 = this.viewPages;
                    if (i8 < viewPageArr4.length) {
                        if (viewPageArr4[i8].isDefaultDialogType() && AndroidUtilities.isTablet()) {
                            boolean booleanValue = ((Boolean) objArr[1]).booleanValue();
                            long longValue = ((Long) objArr[0]).longValue();
                            if (!booleanValue) {
                                this.openedDialogId = longValue;
                            } else if (longValue == this.openedDialogId) {
                                this.openedDialogId = 0L;
                            }
                            this.viewPages[i8].dialogsAdapter.setOpenedDialogId(this.openedDialogId);
                        }
                        i8++;
                    } else {
                        updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                        return;
                    }
                }
            }
        } else if (i == NotificationCenter.notificationsSettingsUpdated) {
            updateVisibleRows(0);
        } else if (i == NotificationCenter.messageReceivedByAck || i == NotificationCenter.messageReceivedByServer || i == NotificationCenter.messageSendError) {
            updateVisibleRows(MessagesController.UPDATE_MASK_SEND_STATE);
        } else if (i == NotificationCenter.didSetPasscode) {
            updatePasscodeButton();
        } else if (i == NotificationCenter.needReloadRecentDialogsSearch) {
            SearchViewPager searchViewPager = this.searchViewPager;
            if (!(searchViewPager == null || (dialogsSearchAdapter2 = searchViewPager.dialogsSearchAdapter) == null)) {
                dialogsSearchAdapter2.loadRecentSearch();
            }
        } else if (i == NotificationCenter.replyMessagesDidLoad) {
            updateVisibleRows(MessagesController.UPDATE_MASK_MESSAGE_TEXT);
        } else if (i == NotificationCenter.reloadHints) {
            SearchViewPager searchViewPager2 = this.searchViewPager;
            if (!(searchViewPager2 == null || (dialogsSearchAdapter = searchViewPager2.dialogsSearchAdapter) == null)) {
                dialogsSearchAdapter.notifyDataSetChanged();
            }
        } else if (i == NotificationCenter.didUpdateConnectionState) {
            int connectionState = AccountInstance.getInstance(i2).getConnectionsManager().getConnectionState();
            if (this.currentConnectionState != connectionState) {
                this.currentConnectionState = connectionState;
                updateProxyButton(true, false);
            }
        } else if (i == NotificationCenter.onDownloadingFilesChanged) {
            updateProxyButton(true, false);
        } else if (i == NotificationCenter.needDeleteDialog) {
            if (this.fragmentView != null && !this.isPaused) {
                final long longValue2 = ((Long) objArr[0]).longValue();
                final TLRPC$User tLRPC$User = (TLRPC$User) objArr[1];
                final TLRPC$Chat tLRPC$Chat = (TLRPC$Chat) objArr[2];
                final boolean booleanValue2 = ((Boolean) objArr[3]).booleanValue();
                Runnable dialogsActivity$$ExternalSyntheticLambda38 = new Runnable() {
                    @Override
                    public final void run() {
                        DialogsActivity.this.lambda$didReceivedNotification$39(tLRPC$Chat, longValue2, booleanValue2, tLRPC$User);
                    }
                };
                if (this.undoView[0] != null) {
                    getUndoView().showWithAction(longValue2, 1, dialogsActivity$$ExternalSyntheticLambda38);
                } else {
                    dialogsActivity$$ExternalSyntheticLambda38.run();
                }
            }
        } else if (i == NotificationCenter.folderBecomeEmpty) {
            int intValue = ((Integer) objArr[0]).intValue();
            int i9 = this.folderId;
            if (i9 == intValue && i9 != 0) {
                finishFragment();
            }
        } else if (i == NotificationCenter.dialogFiltersUpdated) {
            updateFilterTabs(true, true);
        } else if (i == NotificationCenter.filterSettingsUpdated) {
            showFiltersHint();
        } else if (i == NotificationCenter.newSuggestionsAvailable) {
            showNextSupportedSuggestion();
        } else if (i == NotificationCenter.forceImportContactsStart) {
            setFloatingProgressVisible(true, true);
            for (ViewPage viewPage2 : this.viewPages) {
                viewPage2.dialogsAdapter.setForceShowEmptyCell(false);
                viewPage2.dialogsAdapter.setForceUpdatingContacts(true);
                viewPage2.dialogsAdapter.notifyDataSetChanged();
            }
        } else if (i == NotificationCenter.messagesDeleted) {
            if (this.searchIsShowed && this.searchViewPager != null) {
                this.searchViewPager.messagesDeleted(((Long) objArr[1]).longValue(), (ArrayList) objArr[0]);
            }
        } else if (i == NotificationCenter.didClearDatabase) {
            if (this.viewPages != null) {
                while (true) {
                    ViewPage[] viewPageArr5 = this.viewPages;
                    if (i5 >= viewPageArr5.length) {
                        break;
                    }
                    viewPageArr5[i5].dialogsAdapter.didDatabaseCleared();
                    i5++;
                }
            }
            SuggestClearDatabaseBottomSheet.dismissDialog();
        } else if (i == NotificationCenter.appUpdateAvailable) {
            updateMenuButton(true);
        } else if (i == NotificationCenter.fileLoaded || i == NotificationCenter.fileLoadFailed || i == NotificationCenter.fileLoadProgressChanged) {
            String str = (String) objArr[0];
            if (SharedConfig.isAppUpdateAvailable() && FileLoader.getAttachFileName(SharedConfig.pendingAppUpdate.document).equals(str)) {
                updateMenuButton(true);
            }
        } else if (i == NotificationCenter.onDatabaseMigration) {
            boolean booleanValue3 = ((Boolean) objArr[0]).booleanValue();
            if (this.fragmentView == null) {
                return;
            }
            if (booleanValue3) {
                if (this.databaseMigrationHint == null) {
                    DatabaseMigrationHint databaseMigrationHint = new DatabaseMigrationHint(this.fragmentView.getContext(), this.currentAccount);
                    this.databaseMigrationHint = databaseMigrationHint;
                    databaseMigrationHint.setAlpha(0.0f);
                    ((ContentView) this.fragmentView).addView(this.databaseMigrationHint);
                    this.databaseMigrationHint.animate().alpha(1.0f).setDuration(300L).setStartDelay(1000L).start();
                }
                this.databaseMigrationHint.setTag(1);
                return;
            }
            View view = this.databaseMigrationHint;
            if (!(view == null || view.getTag() == null)) {
                final View view2 = this.databaseMigrationHint;
                view2.animate().setListener(null).cancel();
                view2.animate().setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        if (view2.getParent() != null) {
                            ((ViewGroup) view2.getParent()).removeView(view2);
                        }
                        DialogsActivity.this.databaseMigrationHint = null;
                    }
                }).alpha(0.0f).setStartDelay(0L).setDuration(150L).start();
                this.databaseMigrationHint.setTag(null);
            }
        } else if (i == NotificationCenter.onDatabaseOpened) {
            checkSuggestClearDatabase();
        }
    }

    public void lambda$didReceivedNotification$39(TLRPC$Chat tLRPC$Chat, long j, boolean z, TLRPC$User tLRPC$User) {
        if (tLRPC$Chat == null) {
            getMessagesController().deleteDialog(j, 0, z);
            if (tLRPC$User != null && tLRPC$User.bot) {
                getMessagesController().blockPeer(tLRPC$User.id);
            }
        } else if (ChatObject.isNotInChat(tLRPC$Chat)) {
            getMessagesController().deleteDialog(j, 0, z);
        } else {
            getMessagesController().deleteParticipantFromChat(-j, getMessagesController().getUser(Long.valueOf(getUserConfig().getClientUserId())), null, null, z, z);
        }
        getMessagesController().checkIfFolderEmpty(this.folderId);
    }

    private void checkSuggestClearDatabase() {
        if (getMessagesStorage().showClearDatabaseAlert) {
            getMessagesStorage().showClearDatabaseAlert = false;
            SuggestClearDatabaseBottomSheet.show(this);
        }
    }

    private void updateMenuButton(boolean z) {
        int i;
        if (this.menuDrawable != null && this.updateLayout != null) {
            float f = 0.0f;
            if (SharedConfig.isAppUpdateAvailable()) {
                String attachFileName = FileLoader.getAttachFileName(SharedConfig.pendingAppUpdate.document);
                if (getFileLoader().isLoadingFile(attachFileName)) {
                    i = MenuDrawable.TYPE_UDPATE_DOWNLOADING;
                    Float fileProgress = ImageLoader.getInstance().getFileProgress(attachFileName);
                    if (fileProgress != null) {
                        f = fileProgress.floatValue();
                    }
                } else {
                    i = MenuDrawable.TYPE_UDPATE_AVAILABLE;
                }
            } else {
                i = MenuDrawable.TYPE_DEFAULT;
            }
            updateAppUpdateViews(z);
            this.menuDrawable.setType(i, z);
            this.menuDrawable.setUpdateDownloadProgress(f, z);
        }
    }

    private void showNextSupportedSuggestion() {
        if (this.showingSuggestion == null) {
            for (String str : getMessagesController().pendingSuggestions) {
                if (showSuggestion(str)) {
                    this.showingSuggestion = str;
                    return;
                }
            }
        }
    }

    private void onSuggestionDismiss() {
        if (this.showingSuggestion != null) {
            getMessagesController().removeSuggestion(0L, this.showingSuggestion);
            this.showingSuggestion = null;
            showNextSupportedSuggestion();
        }
    }

    private boolean showSuggestion(String str) {
        if (!"AUTOARCHIVE_POPULAR".equals(str)) {
            return false;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString("HideNewChatsAlertTitle", R.string.HideNewChatsAlertTitle));
        builder.setMessage(AndroidUtilities.replaceTags(LocaleController.getString("HideNewChatsAlertText", R.string.HideNewChatsAlertText)));
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        builder.setPositiveButton(LocaleController.getString("GoToSettings", R.string.GoToSettings), new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                DialogsActivity.this.lambda$showSuggestion$40(dialogInterface, i);
            }
        });
        showDialog(builder.create(), new DialogInterface.OnDismissListener() {
            @Override
            public final void onDismiss(DialogInterface dialogInterface) {
                DialogsActivity.this.lambda$showSuggestion$41(dialogInterface);
            }
        });
        return true;
    }

    public void lambda$showSuggestion$40(DialogInterface dialogInterface, int i) {
        presentFragment(new PrivacySettingsActivity());
        AndroidUtilities.scrollToFragmentRow(this.parentLayout, "newChatsRow");
    }

    public void lambda$showSuggestion$41(DialogInterface dialogInterface) {
        onSuggestionDismiss();
    }

    private void showFiltersHint() {
        if (!this.askingForPermissions && getMessagesController().dialogFiltersLoaded && getMessagesController().showFiltersTooltip && this.filterTabsView != null && getMessagesController().dialogFilters.isEmpty() && !this.isPaused && getUserConfig().filtersLoaded && !this.inPreviewMode) {
            SharedPreferences globalMainSettings = MessagesController.getGlobalMainSettings();
            if (!globalMainSettings.getBoolean("filterhint", false)) {
                globalMainSettings.edit().putBoolean("filterhint", true).commit();
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public final void run() {
                        DialogsActivity.this.lambda$showFiltersHint$43();
                    }
                }, 1000L);
            }
        }
    }

    public void lambda$showFiltersHint$42() {
        presentFragment(new FiltersSetupActivity());
    }

    public void lambda$showFiltersHint$43() {
        getUndoView().showWithAction(0L, 15, null, new Runnable() {
            @Override
            public final void run() {
                DialogsActivity.this.lambda$showFiltersHint$42();
            }
        });
    }

    private void setDialogsListFrozen(boolean z, boolean z2) {
        if (this.viewPages != null && this.dialogsListFrozen != z) {
            if (z) {
                this.frozenDialogsList = new ArrayList<>(getDialogsArray(this.currentAccount, this.viewPages[0].dialogsType, this.folderId, false));
            } else {
                this.frozenDialogsList = null;
            }
            this.dialogsListFrozen = z;
            this.viewPages[0].dialogsAdapter.setDialogsListFrozen(z);
            if (!z && z2) {
                this.viewPages[0].dialogsAdapter.notifyDataSetChanged();
            }
        }
    }

    public void setDialogsListFrozen(boolean z) {
        setDialogsListFrozen(z, true);
    }

    public ArrayList<TLRPC$Dialog> getDialogsArray(int i, int i2, int i3, boolean z) {
        ArrayList<TLRPC$Dialog> arrayList;
        if (z && (arrayList = this.frozenDialogsList) != null) {
            return arrayList;
        }
        MessagesController messagesController = AccountInstance.getInstance(i).getMessagesController();
        if (i2 == 0) {
            return messagesController.getDialogs(i3);
        }
        char c = 1;
        if (i2 == 1 || i2 == 10 || i2 == 13) {
            return messagesController.dialogsServerOnly;
        }
        if (i2 == 2) {
            ArrayList<TLRPC$Dialog> arrayList2 = new ArrayList<>(messagesController.dialogsCanAddUsers.size() + messagesController.dialogsMyChannels.size() + messagesController.dialogsMyGroups.size() + 2);
            if (messagesController.dialogsMyChannels.size() > 0) {
                arrayList2.add(null);
                arrayList2.addAll(messagesController.dialogsMyChannels);
            }
            if (messagesController.dialogsMyGroups.size() > 0) {
                arrayList2.add(null);
                arrayList2.addAll(messagesController.dialogsMyGroups);
            }
            if (messagesController.dialogsCanAddUsers.size() > 0) {
                arrayList2.add(null);
                arrayList2.addAll(messagesController.dialogsCanAddUsers);
            }
            return arrayList2;
        } else if (i2 == 3) {
            return messagesController.dialogsForward;
        } else {
            if (i2 == 4 || i2 == 12) {
                return messagesController.dialogsUsersOnly;
            }
            if (i2 == 5) {
                return messagesController.dialogsChannelsOnly;
            }
            if (i2 == 6 || i2 == 11) {
                return messagesController.dialogsGroupsOnly;
            }
            if (i2 == 7 || i2 == 8) {
                MessagesController.DialogFilter[] dialogFilterArr = messagesController.selectedDialogFilter;
                if (i2 == 7) {
                    c = 0;
                }
                MessagesController.DialogFilter dialogFilter = dialogFilterArr[c];
                if (dialogFilter == null) {
                    return messagesController.getDialogs(i3);
                }
                return dialogFilter.dialogs;
            } else if (i2 == 9) {
                return messagesController.dialogsForBlock;
            } else {
                return new ArrayList<>();
            }
        }
    }

    public void setSideMenu(RecyclerView recyclerView) {
        this.sideMenu = recyclerView;
        recyclerView.setBackgroundColor(Theme.getColor("chats_menuBackground"));
        this.sideMenu.setGlowColor(Theme.getColor("chats_menuBackground"));
    }

    public void updatePasscodeButton() {
        if (this.passcodeItem != null) {
            if (SharedConfig.passcodeHash.length() == 0 || this.searching) {
                this.passcodeItem.setVisibility(8);
                this.passcodeItemVisible = false;
                return;
            }
            ActionBarMenuItem actionBarMenuItem = this.doneItem;
            if (actionBarMenuItem == null || actionBarMenuItem.getVisibility() != 0) {
                this.passcodeItem.setVisibility(0);
            }
            this.passcodeItem.setIcon(this.passcodeDrawable);
            this.passcodeItemVisible = true;
        }
    }

    private void setFloatingProgressVisible(final boolean z, boolean z2) {
        if (this.floatingButton != null && this.floatingProgressView != null) {
            float f = 0.0f;
            float f2 = 0.1f;
            if (!z2) {
                AnimatorSet animatorSet = this.floatingProgressAnimator;
                if (animatorSet != null) {
                    animatorSet.cancel();
                }
                this.floatingProgressVisible = z;
                if (z) {
                    this.floatingButton.setAlpha(0.0f);
                    this.floatingButton.setScaleX(0.1f);
                    this.floatingButton.setScaleY(0.1f);
                    this.floatingButton.setVisibility(8);
                    this.floatingProgressView.setAlpha(1.0f);
                    this.floatingProgressView.setScaleX(1.0f);
                    this.floatingProgressView.setScaleY(1.0f);
                    this.floatingProgressView.setVisibility(0);
                    return;
                }
                this.floatingButton.setAlpha(1.0f);
                this.floatingButton.setScaleX(1.0f);
                this.floatingButton.setScaleY(1.0f);
                this.floatingButton.setVisibility(0);
                this.floatingProgressView.setAlpha(0.0f);
                this.floatingProgressView.setScaleX(0.1f);
                this.floatingProgressView.setScaleY(0.1f);
                this.floatingProgressView.setVisibility(8);
            } else if (z != this.floatingProgressVisible) {
                AnimatorSet animatorSet2 = this.floatingProgressAnimator;
                if (animatorSet2 != null) {
                    animatorSet2.cancel();
                }
                this.floatingProgressVisible = z;
                AnimatorSet animatorSet3 = new AnimatorSet();
                this.floatingProgressAnimator = animatorSet3;
                Animator[] animatorArr = new Animator[6];
                RLottieImageView rLottieImageView = this.floatingButton;
                Property property = View.ALPHA;
                float[] fArr = new float[1];
                fArr[0] = z ? 0.0f : 1.0f;
                animatorArr[0] = ObjectAnimator.ofFloat(rLottieImageView, property, fArr);
                RLottieImageView rLottieImageView2 = this.floatingButton;
                Property property2 = View.SCALE_X;
                float[] fArr2 = new float[1];
                fArr2[0] = z ? 0.1f : 1.0f;
                animatorArr[1] = ObjectAnimator.ofFloat(rLottieImageView2, property2, fArr2);
                RLottieImageView rLottieImageView3 = this.floatingButton;
                Property property3 = View.SCALE_Y;
                float[] fArr3 = new float[1];
                fArr3[0] = z ? 0.1f : 1.0f;
                animatorArr[2] = ObjectAnimator.ofFloat(rLottieImageView3, property3, fArr3);
                RadialProgressView radialProgressView = this.floatingProgressView;
                Property property4 = View.ALPHA;
                float[] fArr4 = new float[1];
                if (z) {
                    f = 1.0f;
                }
                fArr4[0] = f;
                animatorArr[3] = ObjectAnimator.ofFloat(radialProgressView, property4, fArr4);
                RadialProgressView radialProgressView2 = this.floatingProgressView;
                Property property5 = View.SCALE_X;
                float[] fArr5 = new float[1];
                fArr5[0] = z ? 1.0f : 0.1f;
                animatorArr[4] = ObjectAnimator.ofFloat(radialProgressView2, property5, fArr5);
                RadialProgressView radialProgressView3 = this.floatingProgressView;
                Property property6 = View.SCALE_Y;
                float[] fArr6 = new float[1];
                if (z) {
                    f2 = 1.0f;
                }
                fArr6[0] = f2;
                animatorArr[5] = ObjectAnimator.ofFloat(radialProgressView3, property6, fArr6);
                animatorSet3.playTogether(animatorArr);
                this.floatingProgressAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        DialogsActivity.this.floatingProgressView.setVisibility(0);
                        DialogsActivity.this.floatingButton.setVisibility(0);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        if (animator == DialogsActivity.this.floatingProgressAnimator) {
                            if (z) {
                                if (DialogsActivity.this.floatingButton != null) {
                                    DialogsActivity.this.floatingButton.setVisibility(8);
                                }
                            } else if (DialogsActivity.this.floatingButton != null) {
                                DialogsActivity.this.floatingProgressView.setVisibility(8);
                            }
                            DialogsActivity.this.floatingProgressAnimator = null;
                        }
                    }
                });
                this.floatingProgressAnimator.setDuration(150L);
                this.floatingProgressAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
                this.floatingProgressAnimator.start();
            }
        }
    }

    public void hideFloatingButton(boolean z) {
        if (this.floatingHidden == z) {
            return;
        }
        if (!z || !this.floatingForceVisible) {
            this.floatingHidden = z;
            AnimatorSet animatorSet = new AnimatorSet();
            float[] fArr = new float[2];
            fArr[0] = this.floatingButtonHideProgress;
            fArr[1] = this.floatingHidden ? 1.0f : 0.0f;
            ValueAnimator ofFloat = ValueAnimator.ofFloat(fArr);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    DialogsActivity.this.lambda$hideFloatingButton$44(valueAnimator);
                }
            });
            animatorSet.playTogether(ofFloat);
            animatorSet.setDuration(300L);
            animatorSet.setInterpolator(this.floatingInterpolator);
            this.floatingButtonContainer.setClickable(!z);
            animatorSet.start();
        }
    }

    public void lambda$hideFloatingButton$44(ValueAnimator valueAnimator) {
        this.floatingButtonHideProgress = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.floatingButtonTranslation = AndroidUtilities.dp(100.0f) * this.floatingButtonHideProgress;
        updateFloatingButtonOffset();
    }

    public float getContactsAlpha() {
        return this.contactsAlpha;
    }

    public void animateContactsAlpha(float f) {
        ValueAnimator valueAnimator = this.contactsAlphaAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        ValueAnimator duration = ValueAnimator.ofFloat(this.contactsAlpha, f).setDuration(250L);
        this.contactsAlphaAnimator = duration;
        duration.setInterpolator(CubicBezierInterpolator.DEFAULT);
        this.contactsAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                DialogsActivity.this.lambda$animateContactsAlpha$45(valueAnimator2);
            }
        });
        this.contactsAlphaAnimator.start();
    }

    public void lambda$animateContactsAlpha$45(ValueAnimator valueAnimator) {
        setContactsAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    public void setContactsAlpha(float f) {
        ViewPage[] viewPageArr;
        this.contactsAlpha = f;
        for (ViewPage viewPage : this.viewPages) {
            DialogsRecyclerView dialogsRecyclerView = viewPage.listView;
            for (int i = 0; i < dialogsRecyclerView.getChildCount(); i++) {
                View childAt = dialogsRecyclerView.getChildAt(i);
                if (dialogsRecyclerView.getChildAdapterPosition(childAt) >= viewPage.dialogsAdapter.getDialogsCount() + 1) {
                    childAt.setAlpha(f);
                }
            }
        }
    }

    public void setScrollDisabled(boolean z) {
        for (ViewPage viewPage : this.viewPages) {
            ((LinearLayoutManager) viewPage.listView.getLayoutManager()).setScrollDisabled(z);
        }
    }

    public void updateDialogIndices() {
        int indexOf;
        if (this.viewPages != null) {
            int i = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i < viewPageArr.length) {
                    if (viewPageArr[i].getVisibility() == 0) {
                        ArrayList<TLRPC$Dialog> dialogsArray = getDialogsArray(this.currentAccount, this.viewPages[i].dialogsType, this.folderId, false);
                        int childCount = this.viewPages[i].listView.getChildCount();
                        for (int i2 = 0; i2 < childCount; i2++) {
                            View childAt = this.viewPages[i].listView.getChildAt(i2);
                            if (childAt instanceof DialogCell) {
                                DialogCell dialogCell = (DialogCell) childAt;
                                TLRPC$Dialog tLRPC$Dialog = getMessagesController().dialogs_dict.get(dialogCell.getDialogId());
                                if (tLRPC$Dialog != null && (indexOf = dialogsArray.indexOf(tLRPC$Dialog)) >= 0) {
                                    dialogCell.setDialogIndex(indexOf);
                                }
                            }
                        }
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    public void updateVisibleRows(int i) {
        updateVisibleRows(i, true);
    }

    private void updateVisibleRows(int i, boolean z) {
        if ((!this.dialogsListFrozen || (MessagesController.UPDATE_MASK_REORDER & i) != 0) && !this.isPaused) {
            for (int i2 = 0; i2 < 3; i2++) {
                RecyclerView recyclerView = null;
                if (i2 == 2) {
                    SearchViewPager searchViewPager = this.searchViewPager;
                    if (searchViewPager != null) {
                        recyclerView = searchViewPager.searchListView;
                    }
                } else {
                    ViewPage[] viewPageArr = this.viewPages;
                    if (viewPageArr != null) {
                        if (i2 < viewPageArr.length) {
                            recyclerView = viewPageArr[i2].listView;
                        }
                        if (!(recyclerView == null || this.viewPages[i2].getVisibility() == 0)) {
                        }
                    }
                }
                if (recyclerView != null) {
                    int childCount = recyclerView.getChildCount();
                    for (int i3 = 0; i3 < childCount; i3++) {
                        View childAt = recyclerView.getChildAt(i3);
                        if ((childAt instanceof DialogCell) && recyclerView.getAdapter() != this.searchViewPager.dialogsSearchAdapter) {
                            DialogCell dialogCell = (DialogCell) childAt;
                            boolean z2 = true;
                            if ((MessagesController.UPDATE_MASK_REORDER & i) != 0) {
                                dialogCell.onReorderStateChanged(this.actionBar.isActionModeShowed(), true);
                                if (this.dialogsListFrozen) {
                                }
                            }
                            if ((MessagesController.UPDATE_MASK_CHECK & i) != 0) {
                                if ((MessagesController.UPDATE_MASK_CHAT & i) == 0) {
                                    z2 = false;
                                }
                                dialogCell.setChecked(false, z2);
                            } else {
                                if ((MessagesController.UPDATE_MASK_NEW_MESSAGE & i) != 0) {
                                    dialogCell.checkCurrentDialogIndex(this.dialogsListFrozen);
                                    if (this.viewPages[i2].isDefaultDialogType() && AndroidUtilities.isTablet()) {
                                        if (dialogCell.getDialogId() != this.openedDialogId) {
                                            z2 = false;
                                        }
                                        dialogCell.setDialogSelected(z2);
                                    }
                                } else if ((MessagesController.UPDATE_MASK_SELECT_DIALOG & i) == 0) {
                                    dialogCell.update(i, z);
                                } else if (this.viewPages[i2].isDefaultDialogType() && AndroidUtilities.isTablet()) {
                                    if (dialogCell.getDialogId() != this.openedDialogId) {
                                        z2 = false;
                                    }
                                    dialogCell.setDialogSelected(z2);
                                }
                                ArrayList<Long> arrayList = this.selectedDialogs;
                                if (arrayList != null) {
                                    dialogCell.setChecked(arrayList.contains(Long.valueOf(dialogCell.getDialogId())), false);
                                }
                            }
                        }
                        if (childAt instanceof UserCell) {
                            ((UserCell) childAt).update(i);
                        } else if (childAt instanceof ProfileSearchCell) {
                            ProfileSearchCell profileSearchCell = (ProfileSearchCell) childAt;
                            profileSearchCell.update(i);
                            ArrayList<Long> arrayList2 = this.selectedDialogs;
                            if (arrayList2 != null) {
                                profileSearchCell.setChecked(arrayList2.contains(Long.valueOf(profileSearchCell.getDialogId())), false);
                            }
                        }
                        if (!this.dialogsListFrozen && (childAt instanceof RecyclerListView)) {
                            RecyclerListView recyclerListView = (RecyclerListView) childAt;
                            int childCount2 = recyclerListView.getChildCount();
                            for (int i4 = 0; i4 < childCount2; i4++) {
                                View childAt2 = recyclerListView.getChildAt(i4);
                                if (childAt2 instanceof HintDialogCell) {
                                    ((HintDialogCell) childAt2).update(i);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void setDelegate(DialogsActivityDelegate dialogsActivityDelegate) {
        this.delegate = dialogsActivityDelegate;
    }

    public void setSearchString(String str) {
        this.searchString = str;
    }

    public void setInitialSearchString(String str) {
        this.initialSearchString = str;
    }

    public boolean isMainDialogList() {
        return this.delegate == null && this.searchString == null;
    }

    public void setInitialSearchType(int i) {
        this.initialSearchType = i;
    }

    private boolean checkCanWrite(long j) {
        if (this.addToGroupAlertString != null || !this.checkCanWrite) {
            return true;
        }
        if (DialogObject.isChatDialog(j)) {
            long j2 = -j;
            TLRPC$Chat chat = getMessagesController().getChat(Long.valueOf(j2));
            if (!ChatObject.isChannel(chat) || chat.megagroup) {
                return true;
            }
            if (!this.cantSendToChannels && ChatObject.isCanWriteToChannel(j2, this.currentAccount) && this.hasPoll != 2) {
                return true;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("SendMessageTitle", R.string.SendMessageTitle));
            if (this.hasPoll == 2) {
                builder.setMessage(LocaleController.getString("PublicPollCantForward", R.string.PublicPollCantForward));
            } else {
                builder.setMessage(LocaleController.getString("ChannelCantSendMessage", R.string.ChannelCantSendMessage));
            }
            builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
            showDialog(builder.create());
            return false;
        } else if (!DialogObject.isEncryptedDialog(j)) {
            return true;
        } else {
            if (this.hasPoll == 0 && !this.hasInvoice) {
                return true;
            }
            AlertDialog.Builder builder2 = new AlertDialog.Builder(getParentActivity());
            builder2.setTitle(LocaleController.getString("SendMessageTitle", R.string.SendMessageTitle));
            if (this.hasPoll != 0) {
                builder2.setMessage(LocaleController.getString("PollCantForwardSecretChat", R.string.PollCantForwardSecretChat));
            } else {
                builder2.setMessage(LocaleController.getString("InvoiceCantForwardSecretChat", R.string.InvoiceCantForwardSecretChat));
            }
            builder2.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
            showDialog(builder2.create());
            return false;
        }
    }

    public void didSelectResult(final long j, boolean z, final boolean z2) {
        final TLRPC$Chat tLRPC$Chat;
        final TLRPC$User tLRPC$User;
        String str;
        String str2;
        String str3;
        if (checkCanWrite(j)) {
            int i = this.initialDialogsType;
            if (i == 11 || i == 12 || i == 13) {
                if (!this.checkingImportDialog) {
                    if (DialogObject.isUserDialog(j)) {
                        TLRPC$User user = getMessagesController().getUser(Long.valueOf(j));
                        if (!user.mutual_contact) {
                            getUndoView().showWithAction(j, 45, (Runnable) null);
                            return;
                        } else {
                            tLRPC$User = user;
                            tLRPC$Chat = null;
                        }
                    } else {
                        TLRPC$Chat chat = getMessagesController().getChat(Long.valueOf(-j));
                        if (!ChatObject.hasAdminRights(chat) || !ChatObject.canChangeChatInfo(chat)) {
                            getUndoView().showWithAction(j, 46, (Runnable) null);
                            return;
                        } else {
                            tLRPC$Chat = chat;
                            tLRPC$User = null;
                        }
                    }
                    final AlertDialog alertDialog = new AlertDialog(getParentActivity(), 3);
                    final TLRPC$TL_messages_checkHistoryImportPeer tLRPC$TL_messages_checkHistoryImportPeer = new TLRPC$TL_messages_checkHistoryImportPeer();
                    tLRPC$TL_messages_checkHistoryImportPeer.peer = getMessagesController().getInputPeer(j);
                    getConnectionsManager().sendRequest(tLRPC$TL_messages_checkHistoryImportPeer, new RequestDelegate() {
                        @Override
                        public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                            DialogsActivity.this.lambda$didSelectResult$48(alertDialog, tLRPC$User, tLRPC$Chat, j, z2, tLRPC$TL_messages_checkHistoryImportPeer, tLObject, tLRPC$TL_error);
                        }
                    });
                    try {
                        alertDialog.showDelayed(300L);
                    } catch (Exception unused) {
                    }
                }
            } else if (!z || ((this.selectAlertString == null || this.selectAlertStringGroup == null) && this.addToGroupAlertString == null)) {
                if (this.delegate != null) {
                    ArrayList<Long> arrayList = new ArrayList<>();
                    arrayList.add(Long.valueOf(j));
                    this.delegate.didSelectDialogs(this, arrayList, null, z2);
                    if (this.resetDelegate) {
                        this.delegate = null;
                        return;
                    }
                    return;
                }
                finishFragment();
            } else if (getParentActivity() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                if (DialogObject.isEncryptedDialog(j)) {
                    TLRPC$User user2 = getMessagesController().getUser(Long.valueOf(getMessagesController().getEncryptedChat(Integer.valueOf(DialogObject.getEncryptedChatId(j))).user_id));
                    if (user2 != null) {
                        str = LocaleController.getString("SendMessageTitle", R.string.SendMessageTitle);
                        str3 = LocaleController.formatStringSimple(this.selectAlertString, UserObject.getUserName(user2));
                        str2 = LocaleController.getString("Send", R.string.Send);
                    } else {
                        return;
                    }
                } else if (!DialogObject.isUserDialog(j)) {
                    TLRPC$Chat chat2 = getMessagesController().getChat(Long.valueOf(-j));
                    if (chat2 != null) {
                        if (this.addToGroupAlertString != null) {
                            str = LocaleController.getString("AddToTheGroupAlertTitle", R.string.AddToTheGroupAlertTitle);
                            str3 = LocaleController.formatStringSimple(this.addToGroupAlertString, chat2.title);
                            str2 = LocaleController.getString("Add", R.string.Add);
                        } else {
                            str = LocaleController.getString("SendMessageTitle", R.string.SendMessageTitle);
                            str3 = LocaleController.formatStringSimple(this.selectAlertStringGroup, chat2.title);
                            str2 = LocaleController.getString("Send", R.string.Send);
                        }
                    } else {
                        return;
                    }
                } else if (j == getUserConfig().getClientUserId()) {
                    str = LocaleController.getString("SendMessageTitle", R.string.SendMessageTitle);
                    str3 = LocaleController.formatStringSimple(this.selectAlertStringGroup, LocaleController.getString("SavedMessages", R.string.SavedMessages));
                    str2 = LocaleController.getString("Send", R.string.Send);
                } else {
                    TLRPC$User user3 = getMessagesController().getUser(Long.valueOf(j));
                    if (user3 != null && this.selectAlertString != null) {
                        str = LocaleController.getString("SendMessageTitle", R.string.SendMessageTitle);
                        str3 = LocaleController.formatStringSimple(this.selectAlertString, UserObject.getUserName(user3));
                        str2 = LocaleController.getString("Send", R.string.Send);
                    } else {
                        return;
                    }
                }
                builder.setTitle(str);
                builder.setMessage(AndroidUtilities.replaceTags(str3));
                builder.setPositiveButton(str2, new DialogInterface.OnClickListener() {
                    @Override
                    public final void onClick(DialogInterface dialogInterface, int i2) {
                        DialogsActivity.this.lambda$didSelectResult$49(j, dialogInterface, i2);
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            }
        }
    }

    public void lambda$didSelectResult$48(final AlertDialog alertDialog, final TLRPC$User tLRPC$User, final TLRPC$Chat tLRPC$Chat, final long j, final boolean z, final TLRPC$TL_messages_checkHistoryImportPeer tLRPC$TL_messages_checkHistoryImportPeer, final TLObject tLObject, final TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public final void run() {
                DialogsActivity.this.lambda$didSelectResult$47(alertDialog, tLObject, tLRPC$User, tLRPC$Chat, j, z, tLRPC$TL_error, tLRPC$TL_messages_checkHistoryImportPeer);
            }
        });
    }

    public void lambda$didSelectResult$47(AlertDialog alertDialog, TLObject tLObject, TLRPC$User tLRPC$User, TLRPC$Chat tLRPC$Chat, final long j, final boolean z, TLRPC$TL_error tLRPC$TL_error, TLRPC$TL_messages_checkHistoryImportPeer tLRPC$TL_messages_checkHistoryImportPeer) {
        try {
            alertDialog.dismiss();
        } catch (Exception e) {
            FileLog.e(e);
        }
        this.checkingImportDialog = false;
        if (tLObject != null) {
            AlertsCreator.createImportDialogAlert(this, this.arguments.getString("importTitle"), ((TLRPC$TL_messages_checkedHistoryImportPeer) tLObject).confirm_text, tLRPC$User, tLRPC$Chat, new Runnable() {
                @Override
                public final void run() {
                    DialogsActivity.this.lambda$didSelectResult$46(j, z);
                }
            });
            return;
        }
        AlertsCreator.processError(this.currentAccount, tLRPC$TL_error, this, tLRPC$TL_messages_checkHistoryImportPeer, new Object[0]);
        getNotificationCenter().postNotificationName(NotificationCenter.historyImportProgressChanged, Long.valueOf(j), tLRPC$TL_messages_checkHistoryImportPeer, tLRPC$TL_error);
    }

    public void lambda$didSelectResult$46(long j, boolean z) {
        setDialogsListFrozen(true);
        ArrayList<Long> arrayList = new ArrayList<>();
        arrayList.add(Long.valueOf(j));
        this.delegate.didSelectDialogs(this, arrayList, null, z);
    }

    public void lambda$didSelectResult$49(long j, DialogInterface dialogInterface, int i) {
        didSelectResult(j, false, false);
    }

    public RLottieImageView getFloatingButton() {
        return this.floatingButton;
    }

    private boolean onSendLongClick(View view) {
        Activity parentActivity = getParentActivity();
        Theme.ResourcesProvider resourceProvider = getResourceProvider();
        if (parentActivity == null) {
            return false;
        }
        LinearLayout linearLayout = new LinearLayout(parentActivity);
        linearLayout.setOrientation(1);
        ActionBarPopupWindow.ActionBarPopupWindowLayout actionBarPopupWindowLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(parentActivity, resourceProvider);
        actionBarPopupWindowLayout.setAnimationEnabled(false);
        actionBarPopupWindowLayout.setOnTouchListener(new View.OnTouchListener() {
            private Rect popupRect = new Rect();

            @Override
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() != 0 || DialogsActivity.this.sendPopupWindow == null || !DialogsActivity.this.sendPopupWindow.isShowing()) {
                    return false;
                }
                view2.getHitRect(this.popupRect);
                if (this.popupRect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
                    return false;
                }
                DialogsActivity.this.sendPopupWindow.dismiss();
                return false;
            }
        });
        actionBarPopupWindowLayout.setDispatchKeyEventListener(new ActionBarPopupWindow.OnDispatchKeyEventListener() {
            @Override
            public final void onDispatchKeyEvent(KeyEvent keyEvent) {
                DialogsActivity.this.lambda$onSendLongClick$50(keyEvent);
            }
        });
        actionBarPopupWindowLayout.setShownFromBotton(false);
        actionBarPopupWindowLayout.setupRadialSelectors(getThemedColor("dialogButtonSelector"));
        ActionBarMenuSubItem actionBarMenuSubItem = new ActionBarMenuSubItem((Context) parentActivity, true, true, resourceProvider);
        actionBarMenuSubItem.setTextAndIcon(LocaleController.getString("SendWithoutSound", R.string.SendWithoutSound), R.drawable.input_notify_off);
        actionBarMenuSubItem.setMinimumWidth(AndroidUtilities.dp(196.0f));
        actionBarPopupWindowLayout.addView((View) actionBarMenuSubItem, LayoutHelper.createLinear(-1, 48));
        actionBarMenuSubItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view2) {
                DialogsActivity.this.lambda$onSendLongClick$51(view2);
            }
        });
        linearLayout.addView(actionBarPopupWindowLayout, LayoutHelper.createLinear(-1, -2));
        ActionBarPopupWindow actionBarPopupWindow = new ActionBarPopupWindow(linearLayout, -2, -2);
        this.sendPopupWindow = actionBarPopupWindow;
        actionBarPopupWindow.setAnimationEnabled(false);
        this.sendPopupWindow.setAnimationStyle(R.style.PopupContextAnimation2);
        this.sendPopupWindow.setOutsideTouchable(true);
        this.sendPopupWindow.setClippingEnabled(true);
        this.sendPopupWindow.setInputMethodMode(2);
        this.sendPopupWindow.setSoftInputMode(0);
        this.sendPopupWindow.getContentView().setFocusableInTouchMode(true);
        SharedConfig.removeScheduledOrNoSoundHint();
        linearLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000.0f), Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000.0f), Integer.MIN_VALUE));
        this.sendPopupWindow.setFocusable(true);
        int[] iArr = new int[2];
        view.getLocationInWindow(iArr);
        this.sendPopupWindow.showAtLocation(view, 51, ((iArr[0] + view.getMeasuredWidth()) - linearLayout.getMeasuredWidth()) + AndroidUtilities.dp(8.0f), (iArr[1] - linearLayout.getMeasuredHeight()) - AndroidUtilities.dp(2.0f));
        this.sendPopupWindow.dimBehind();
        view.performHapticFeedback(3, 2);
        return false;
    }

    public void lambda$onSendLongClick$50(KeyEvent keyEvent) {
        ActionBarPopupWindow actionBarPopupWindow;
        if (keyEvent.getKeyCode() == 4 && keyEvent.getRepeatCount() == 0 && (actionBarPopupWindow = this.sendPopupWindow) != null && actionBarPopupWindow.isShowing()) {
            this.sendPopupWindow.dismiss();
        }
    }

    public void lambda$onSendLongClick$51(View view) {
        ActionBarPopupWindow actionBarPopupWindow = this.sendPopupWindow;
        if (actionBarPopupWindow != null && actionBarPopupWindow.isShowing()) {
            this.sendPopupWindow.dismiss();
        }
        this.notify = false;
        if (this.delegate != null && !this.selectedDialogs.isEmpty()) {
            this.delegate.didSelectDialogs(this, this.selectedDialogs, this.commentView.getFieldText(), false);
        }
    }

    @Override
    public java.util.ArrayList<org.telegram.ui.ActionBar.ThemeDescription> getThemeDescriptions() {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.getThemeDescriptions():java.util.ArrayList");
    }

    public void lambda$getThemeDescriptions$52() {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.lambda$getThemeDescriptions$52():void");
    }

    private void updateFloatingButtonColor() {
        if (getParentActivity() != null && this.floatingButtonContainer != null) {
            Drawable createSimpleSelectorCircleDrawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56.0f), Theme.getColor("chats_actionBackground"), Theme.getColor("chats_actionPressedBackground"));
            if (Build.VERSION.SDK_INT < 21) {
                Drawable mutate = ContextCompat.getDrawable(getParentActivity(), R.drawable.floating_shadow).mutate();
                mutate.setColorFilter(new PorterDuffColorFilter(-16777216, PorterDuff.Mode.MULTIPLY));
                CombinedDrawable combinedDrawable = new CombinedDrawable(mutate, createSimpleSelectorCircleDrawable, 0, 0);
                combinedDrawable.setIconSize(AndroidUtilities.dp(56.0f), AndroidUtilities.dp(56.0f));
                createSimpleSelectorCircleDrawable = combinedDrawable;
            }
            this.floatingButtonContainer.setBackground(createSimpleSelectorCircleDrawable);
        }
    }

    @Override
    public Animator getCustomSlideTransition(boolean z, boolean z2, float f) {
        if (z2) {
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.slideFragmentProgress, 1.0f);
            this.slideBackTransitionAnimator = ofFloat;
            return ofFloat;
        }
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(this.slideFragmentProgress, 1.0f);
        this.slideBackTransitionAnimator = ofFloat2;
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                DialogsActivity.this.lambda$getCustomSlideTransition$53(valueAnimator);
            }
        });
        this.slideBackTransitionAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT);
        this.slideBackTransitionAnimator.setDuration((int) (Math.max((int) ((200.0f / getLayoutContainer().getMeasuredWidth()) * f), 80) * 1.2f));
        this.slideBackTransitionAnimator.start();
        return this.slideBackTransitionAnimator;
    }

    public void lambda$getCustomSlideTransition$53(ValueAnimator valueAnimator) {
        setSlideTransitionProgress(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    @Override
    public void prepareFragmentToSlide(boolean z, boolean z2) {
        if (z || !z2) {
            this.slideBackTransitionAnimator = null;
            this.isSlideBackTransition = false;
            setFragmentIsSliding(false);
            setSlideTransitionProgress(1.0f);
            return;
        }
        this.isSlideBackTransition = true;
        setFragmentIsSliding(true);
    }

    private void setFragmentIsSliding(boolean z) {
        if (SharedConfig.getDevicePerformanceClass() != 0) {
            if (z) {
                ViewPage[] viewPageArr = this.viewPages;
                if (!(viewPageArr == null || viewPageArr[0] == null)) {
                    viewPageArr[0].setLayerType(2, null);
                    this.viewPages[0].setClipChildren(false);
                    this.viewPages[0].setClipToPadding(false);
                    this.viewPages[0].listView.setClipChildren(false);
                }
                ActionBar actionBar = this.actionBar;
                if (actionBar != null) {
                    actionBar.setLayerType(2, null);
                }
                FilterTabsView filterTabsView = this.filterTabsView;
                if (filterTabsView != null) {
                    filterTabsView.getListView().setLayerType(2, null);
                }
                View view = this.fragmentView;
                if (view != null) {
                    ((ViewGroup) view).setClipChildren(false);
                    this.fragmentView.requestLayout();
                    return;
                }
                return;
            }
            int i = 0;
            while (true) {
                ViewPage[] viewPageArr2 = this.viewPages;
                if (i >= viewPageArr2.length) {
                    break;
                }
                ViewPage viewPage = viewPageArr2[i];
                if (viewPage != null) {
                    viewPage.setLayerType(0, null);
                    viewPage.setClipChildren(true);
                    viewPage.setClipToPadding(true);
                    viewPage.listView.setClipChildren(true);
                }
                i++;
            }
            ActionBar actionBar2 = this.actionBar;
            if (actionBar2 != null) {
                actionBar2.setLayerType(0, null);
            }
            FilterTabsView filterTabsView2 = this.filterTabsView;
            if (filterTabsView2 != null) {
                filterTabsView2.getListView().setLayerType(0, null);
            }
            View view2 = this.fragmentView;
            if (view2 != null) {
                ((ViewGroup) view2).setClipChildren(true);
                this.fragmentView.requestLayout();
            }
        }
    }

    @Override
    public void onSlideProgress(boolean z, float f) {
        if (SharedConfig.getDevicePerformanceClass() != 0 && this.isSlideBackTransition && this.slideBackTransitionAnimator == null) {
            setSlideTransitionProgress(f);
        }
    }

    private void setSlideTransitionProgress(float f) {
        if (SharedConfig.getDevicePerformanceClass() != 0) {
            this.slideFragmentProgress = f;
            View view = this.fragmentView;
            if (view != null) {
                view.invalidate();
            }
            FilterTabsView filterTabsView = this.filterTabsView;
            if (filterTabsView != null) {
                float f2 = 1.0f - ((1.0f - this.slideFragmentProgress) * 0.05f);
                filterTabsView.getListView().setScaleX(f2);
                this.filterTabsView.getListView().setScaleY(f2);
                this.filterTabsView.getListView().setTranslationX((this.isDrawerTransition ? AndroidUtilities.dp(4.0f) : -AndroidUtilities.dp(4.0f)) * (1.0f - this.slideFragmentProgress));
                this.filterTabsView.getListView().setPivotX(this.isDrawerTransition ? this.filterTabsView.getMeasuredWidth() : 0.0f);
                this.filterTabsView.getListView().setPivotY(0.0f);
                this.filterTabsView.invalidate();
            }
        }
    }

    @Override
    public void setProgressToDrawerOpened(float f) {
        if (SharedConfig.getDevicePerformanceClass() != 0 && !this.isSlideBackTransition) {
            boolean z = f > 0.0f;
            if (this.searchIsShowed) {
                f = 0.0f;
                z = false;
            }
            if (z != this.isDrawerTransition) {
                this.isDrawerTransition = z;
                if (z) {
                    setFragmentIsSliding(true);
                } else {
                    setFragmentIsSliding(false);
                }
                View view = this.fragmentView;
                if (view != null) {
                    view.requestLayout();
                }
            }
            setSlideTransitionProgress(1.0f - f);
        }
    }

    public void setShowSearch(String str, int i) {
        if (!this.searching) {
            this.initialSearchType = i;
            this.actionBar.openSearchField(str, false);
            return;
        }
        if (!this.searchItem.getSearchField().getText().toString().equals(str)) {
            this.searchItem.getSearchField().setText(str);
        }
        int positionForType = this.searchViewPager.getPositionForType(i);
        if (positionForType >= 0 && this.searchViewPager.getTabsView().getCurrentTabId() != positionForType) {
            this.searchViewPager.getTabsView().scrollToTab(positionForType, positionForType);
        }
    }

    @Override
    public boolean isLightStatusBar() {
        int color = Theme.getColor((!this.searching || !this.whiteActionBar) ? this.folderId == 0 ? "actionBarDefault" : "actionBarDefaultArchived" : "windowBackgroundWhite");
        if (this.actionBar.isActionModeShowed()) {
            color = Theme.getColor("actionBarActionModeDefault");
        }
        return ColorUtils.calculateLuminance(color) > 0.699999988079071d;
    }
}