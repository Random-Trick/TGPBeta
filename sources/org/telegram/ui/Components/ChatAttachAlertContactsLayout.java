package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC$FileLocation;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Components.ChatAttachAlert;
import org.telegram.ui.Components.ChatAttachAlertContactsLayout;
import org.telegram.ui.Components.RecyclerListView;

public class ChatAttachAlertContactsLayout extends ChatAttachAlert.AttachAlertLayout implements NotificationCenter.NotificationCenterDelegate {
    private PhonebookShareAlertDelegate delegate;
    private EmptyTextProgressView emptyView;
    private FrameLayout frameLayout;
    private boolean ignoreLayout;
    private FillLastLinearLayoutManager layoutManager;
    private ShareAdapter listAdapter;
    private RecyclerListView listView;
    private ShareSearchAdapter searchAdapter;
    private SearchField searchField;
    private View shadow;
    private AnimatorSet shadowAnimation;

    public interface PhonebookShareAlertDelegate {
        void didSelectContact(TLRPC$User tLRPC$User, boolean z, int i);
    }

    public static class UserCell extends FrameLayout {
        private AvatarDrawable avatarDrawable;
        private BackupImageView avatarImageView;
        private int currentId;
        private CharSequence currentName;
        private CharSequence currentStatus;
        private TLRPC$User currentUser;
        private CharSequence formattedPhoneNumber;
        private TLRPC$User formattedPhoneNumberUser;
        private TLRPC$FileLocation lastAvatar;
        private String lastName;
        private int lastStatus;
        private SimpleTextView nameTextView;
        private boolean needDivider;
        private final Theme.ResourcesProvider resourcesProvider;
        private SimpleTextView statusTextView;

        public interface CharSequenceCallback {
            CharSequence run();
        }

        @Override
        public boolean hasOverlappingRendering() {
            return false;
        }

        public UserCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            int i = UserConfig.selectedAccount;
            this.resourcesProvider = resourcesProvider;
            this.avatarDrawable = new AvatarDrawable(resourcesProvider);
            BackupImageView backupImageView = new BackupImageView(context);
            this.avatarImageView = backupImageView;
            backupImageView.setRoundRadius(AndroidUtilities.dp(23.0f));
            BackupImageView backupImageView2 = this.avatarImageView;
            boolean z = LocaleController.isRTL;
            int i2 = 5;
            addView(backupImageView2, LayoutHelper.createFrame(46, 46.0f, (z ? 5 : 3) | 48, z ? 0.0f : 14.0f, 9.0f, z ? 14.0f : 0.0f, 0.0f));
            SimpleTextView simpleTextView = new SimpleTextView(context);
            this.nameTextView = simpleTextView;
            simpleTextView.setTextColor(getThemedColor("dialogTextBlack"));
            this.nameTextView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
            this.nameTextView.setTextSize(16);
            this.nameTextView.setGravity((LocaleController.isRTL ? 5 : 3) | 48);
            SimpleTextView simpleTextView2 = this.nameTextView;
            boolean z2 = LocaleController.isRTL;
            addView(simpleTextView2, LayoutHelper.createFrame(-1, 20.0f, (z2 ? 5 : 3) | 48, z2 ? 28.0f : 72.0f, 12.0f, z2 ? 72.0f : 28.0f, 0.0f));
            SimpleTextView simpleTextView3 = new SimpleTextView(context);
            this.statusTextView = simpleTextView3;
            simpleTextView3.setTextSize(13);
            this.statusTextView.setTextColor(getThemedColor("dialogTextGray2"));
            this.statusTextView.setGravity((LocaleController.isRTL ? 5 : 3) | 48);
            SimpleTextView simpleTextView4 = this.statusTextView;
            boolean z3 = LocaleController.isRTL;
            addView(simpleTextView4, LayoutHelper.createFrame(-1, 20.0f, (!z3 ? 3 : i2) | 48, z3 ? 28.0f : 72.0f, 36.0f, z3 ? 72.0f : 28.0f, 0.0f));
        }

        public void setCurrentId(int i) {
            this.currentId = i;
        }

        public void setData(TLRPC$User tLRPC$User, CharSequence charSequence, CharSequence charSequence2, boolean z) {
            if (tLRPC$User == null && charSequence == null && charSequence2 == null) {
                this.currentStatus = null;
                this.currentName = null;
                this.nameTextView.setText("");
                this.statusTextView.setText("");
                this.avatarImageView.setImageDrawable(null);
                return;
            }
            this.currentStatus = charSequence2;
            this.currentName = charSequence;
            this.currentUser = tLRPC$User;
            this.needDivider = z;
            setWillNotDraw(!z);
            update(0);
        }

        public void setData(TLRPC$User tLRPC$User, CharSequence charSequence, final CharSequenceCallback charSequenceCallback, boolean z) {
            setData(tLRPC$User, charSequence, (CharSequence) null, z);
            Utilities.globalQueue.postRunnable(new Runnable() {
                @Override
                public final void run() {
                    ChatAttachAlertContactsLayout.UserCell.this.lambda$setData$1(charSequenceCallback);
                }
            });
        }

        public void lambda$setData$1(CharSequenceCallback charSequenceCallback) {
            final CharSequence run = charSequenceCallback.run();
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public final void run() {
                    ChatAttachAlertContactsLayout.UserCell.this.lambda$setData$0(run);
                }
            });
        }

        public void lambda$setData$0(CharSequence charSequence) {
            CharSequence charSequence2;
            this.currentStatus = charSequence;
            if (charSequence != null) {
                this.statusTextView.setText(charSequence);
                return;
            }
            TLRPC$User tLRPC$User = this.currentUser;
            if (tLRPC$User == null) {
                return;
            }
            if (TextUtils.isEmpty(tLRPC$User.phone)) {
                this.statusTextView.setText(LocaleController.getString("NumberUnknown", R.string.NumberUnknown));
            } else if (this.formattedPhoneNumberUser == this.currentUser || (charSequence2 = this.formattedPhoneNumber) == null) {
                this.statusTextView.setText("");
                Utilities.globalQueue.postRunnable(new Runnable() {
                    @Override
                    public final void run() {
                        ChatAttachAlertContactsLayout.UserCell.this.lambda$setStatus$3();
                    }
                });
            } else {
                this.statusTextView.setText(charSequence2);
            }
        }

        public void lambda$setStatus$3() {
            if (this.currentUser != null) {
                PhoneFormat phoneFormat = PhoneFormat.getInstance();
                this.formattedPhoneNumber = phoneFormat.format("+" + this.currentUser.phone);
                this.formattedPhoneNumberUser = this.currentUser;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public final void run() {
                        ChatAttachAlertContactsLayout.UserCell.this.lambda$setStatus$2();
                    }
                });
            }
        }

        public void lambda$setStatus$2() {
            this.statusTextView.setText(this.formattedPhoneNumber);
        }

        @Override
        protected void onMeasure(int i, int i2) {
            super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64.0f) + (this.needDivider ? 1 : 0), 1073741824));
        }

        public void update(int r12) {
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.ChatAttachAlertContactsLayout.UserCell.update(int):void");
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (this.needDivider) {
                canvas.drawLine(LocaleController.isRTL ? 0.0f : AndroidUtilities.dp(70.0f), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(70.0f) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
            }
        }

        private int getThemedColor(String str) {
            Theme.ResourcesProvider resourcesProvider = this.resourcesProvider;
            Integer color = resourcesProvider != null ? resourcesProvider.getColor(str) : null;
            return color != null ? color.intValue() : Theme.getColor(str);
        }
    }

    public ChatAttachAlertContactsLayout(ChatAttachAlert chatAttachAlert, Context context, final Theme.ResourcesProvider resourcesProvider) {
        super(chatAttachAlert, context, resourcesProvider);
        this.searchAdapter = new ShareSearchAdapter(context);
        FrameLayout frameLayout = new FrameLayout(context);
        this.frameLayout = frameLayout;
        frameLayout.setBackgroundColor(getThemedColor("dialogBackground"));
        SearchField searchField = new SearchField(context, false, resourcesProvider) {
            @Override
            public void onTextChange(String str) {
                if (str.length() != 0) {
                    if (ChatAttachAlertContactsLayout.this.emptyView != null) {
                        ChatAttachAlertContactsLayout.this.emptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
                    }
                } else if (ChatAttachAlertContactsLayout.this.listView.getAdapter() != ChatAttachAlertContactsLayout.this.listAdapter) {
                    int currentTop = ChatAttachAlertContactsLayout.this.getCurrentTop();
                    ChatAttachAlertContactsLayout.this.emptyView.setText(LocaleController.getString("NoContacts", R.string.NoContacts));
                    ChatAttachAlertContactsLayout.this.emptyView.showTextView();
                    ChatAttachAlertContactsLayout.this.listView.setAdapter(ChatAttachAlertContactsLayout.this.listAdapter);
                    ChatAttachAlertContactsLayout.this.listAdapter.notifyDataSetChanged();
                    if (currentTop > 0) {
                        ChatAttachAlertContactsLayout.this.layoutManager.scrollToPositionWithOffset(0, -currentTop);
                    }
                }
                if (ChatAttachAlertContactsLayout.this.searchAdapter != null) {
                    ChatAttachAlertContactsLayout.this.searchAdapter.search(str);
                }
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
                ChatAttachAlertContactsLayout.this.parentAlert.makeFocusable(getSearchEditText(), true);
                return super.onInterceptTouchEvent(motionEvent);
            }

            @Override
            public void processTouchEvent(MotionEvent motionEvent) {
                MotionEvent obtain = MotionEvent.obtain(motionEvent);
                obtain.setLocation(obtain.getRawX(), (obtain.getRawY() - ChatAttachAlertContactsLayout.this.parentAlert.getSheetContainer().getTranslationY()) - AndroidUtilities.dp(58.0f));
                ChatAttachAlertContactsLayout.this.listView.dispatchTouchEvent(obtain);
                obtain.recycle();
            }

            @Override
            protected void onFieldTouchUp(EditTextBoldCursor editTextBoldCursor) {
                ChatAttachAlertContactsLayout.this.parentAlert.makeFocusable(editTextBoldCursor, true);
            }
        };
        this.searchField = searchField;
        searchField.setHint(LocaleController.getString("SearchFriends", R.string.SearchFriends));
        this.frameLayout.addView(this.searchField, LayoutHelper.createFrame(-1, -1, 51));
        EmptyTextProgressView emptyTextProgressView = new EmptyTextProgressView(context, null, resourcesProvider);
        this.emptyView = emptyTextProgressView;
        emptyTextProgressView.showTextView();
        this.emptyView.setText(LocaleController.getString("NoContacts", R.string.NoContacts));
        addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0f, 51, 0.0f, 52.0f, 0.0f, 0.0f));
        RecyclerListView recyclerListView = new RecyclerListView(context, resourcesProvider) {
            @Override
            protected boolean allowSelectChildAtPosition(float f, float f2) {
                return f2 >= ((float) ((ChatAttachAlertContactsLayout.this.parentAlert.scrollOffsetY[0] + AndroidUtilities.dp(30.0f)) + ((Build.VERSION.SDK_INT < 21 || ChatAttachAlertContactsLayout.this.parentAlert.inBubbleMode) ? 0 : AndroidUtilities.statusBarHeight)));
            }
        };
        this.listView = recyclerListView;
        recyclerListView.setClipToPadding(false);
        RecyclerListView recyclerListView2 = this.listView;
        FillLastLinearLayoutManager fillLastLinearLayoutManager = new FillLastLinearLayoutManager(getContext(), 1, false, AndroidUtilities.dp(9.0f), this.listView) {
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int i) {
                LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
                    @Override
                    public int calculateDyToMakeVisible(View view, int i2) {
                        return super.calculateDyToMakeVisible(view, i2) - (ChatAttachAlertContactsLayout.this.listView.getPaddingTop() - AndroidUtilities.dp(8.0f));
                    }

                    @Override
                    public int calculateTimeForDeceleration(int i2) {
                        return super.calculateTimeForDeceleration(i2) * 2;
                    }
                };
                linearSmoothScroller.setTargetPosition(i);
                startSmoothScroll(linearSmoothScroller);
            }
        };
        this.layoutManager = fillLastLinearLayoutManager;
        recyclerListView2.setLayoutManager(fillLastLinearLayoutManager);
        this.layoutManager.setBind(false);
        this.listView.setHorizontalScrollBarEnabled(false);
        this.listView.setVerticalScrollBarEnabled(false);
        addView(this.listView, LayoutHelper.createFrame(-1, -1.0f, 51, 0.0f, 0.0f, 0.0f, 0.0f));
        RecyclerListView recyclerListView3 = this.listView;
        ShareAdapter shareAdapter = new ShareAdapter(context);
        this.listAdapter = shareAdapter;
        recyclerListView3.setAdapter(shareAdapter);
        this.listView.setGlowColor(getThemedColor("dialogScrollGlow"));
        this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public final void onItemClick(View view, int i) {
                ChatAttachAlertContactsLayout.this.lambda$new$1(resourcesProvider, view, i);
            }
        });
        this.listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                ChatAttachAlertContactsLayout chatAttachAlertContactsLayout = ChatAttachAlertContactsLayout.this;
                chatAttachAlertContactsLayout.parentAlert.updateLayout(chatAttachAlertContactsLayout, true, i2);
                ChatAttachAlertContactsLayout.this.updateEmptyViewPosition();
            }
        });
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, AndroidUtilities.getShadowHeight(), 51);
        layoutParams.topMargin = AndroidUtilities.dp(58.0f);
        View view = new View(context);
        this.shadow = view;
        view.setBackgroundColor(getThemedColor("dialogShadowLine"));
        this.shadow.setAlpha(0.0f);
        this.shadow.setTag(1);
        addView(this.shadow, layoutParams);
        addView(this.frameLayout, LayoutHelper.createFrame(-1, 58, 51));
        NotificationCenter.getInstance(this.parentAlert.currentAccount).addObserver(this, NotificationCenter.contactsDidLoad);
        updateEmptyView();
    }

    public void lambda$new$1(Theme.ResourcesProvider resourcesProvider, View view, int i) {
        Object obj;
        String str;
        String str2;
        ContactsController.Contact contact;
        String str3;
        String str4;
        RecyclerView.Adapter adapter = this.listView.getAdapter();
        ShareSearchAdapter shareSearchAdapter = this.searchAdapter;
        if (adapter == shareSearchAdapter) {
            obj = shareSearchAdapter.getItem(i);
        } else {
            int sectionForPosition = this.listAdapter.getSectionForPosition(i);
            int positionInSectionForPosition = this.listAdapter.getPositionInSectionForPosition(i);
            if (positionInSectionForPosition >= 0 && sectionForPosition >= 0) {
                obj = this.listAdapter.getItem(sectionForPosition, positionInSectionForPosition);
            } else {
                return;
            }
        }
        if (obj != null) {
            if (obj instanceof ContactsController.Contact) {
                ContactsController.Contact contact2 = (ContactsController.Contact) obj;
                TLRPC$User tLRPC$User = contact2.user;
                if (tLRPC$User != null) {
                    str3 = tLRPC$User.first_name;
                    str4 = tLRPC$User.last_name;
                } else {
                    str3 = contact2.first_name;
                    str4 = contact2.last_name;
                }
                contact = contact2;
                str = str4;
                str2 = str3;
            } else {
                TLRPC$User tLRPC$User2 = (TLRPC$User) obj;
                ContactsController.Contact contact3 = new ContactsController.Contact();
                String str5 = tLRPC$User2.first_name;
                contact3.first_name = str5;
                String str6 = tLRPC$User2.last_name;
                contact3.last_name = str6;
                contact3.phones.add(tLRPC$User2.phone);
                contact3.user = tLRPC$User2;
                contact = contact3;
                str2 = str5;
                str = str6;
            }
            PhonebookShareAlert phonebookShareAlert = new PhonebookShareAlert(this.parentAlert.baseFragment, contact, null, null, null, str2, str, resourcesProvider);
            phonebookShareAlert.setDelegate(new PhonebookShareAlertDelegate() {
                @Override
                public final void didSelectContact(TLRPC$User tLRPC$User3, boolean z, int i2) {
                    ChatAttachAlertContactsLayout.this.lambda$new$0(tLRPC$User3, z, i2);
                }
            });
            phonebookShareAlert.show();
        }
    }

    public void lambda$new$0(TLRPC$User tLRPC$User, boolean z, int i) {
        this.parentAlert.dismiss(true);
        this.delegate.didSelectContact(tLRPC$User, z, i);
    }

    @Override
    void scrollToTop() {
        this.listView.smoothScrollToPosition(0);
    }

    @Override
    public int getCurrentItemTop() {
        if (this.listView.getChildCount() <= 0) {
            return ConnectionsManager.DEFAULT_DATACENTER_ID;
        }
        View childAt = this.listView.getChildAt(0);
        RecyclerListView.Holder holder = (RecyclerListView.Holder) this.listView.findContainingViewHolder(childAt);
        int top = childAt.getTop() - AndroidUtilities.dp(8.0f);
        int i = (top <= 0 || holder == null || holder.getAdapterPosition() != 0) ? 0 : top;
        if (top < 0 || holder == null || holder.getAdapterPosition() != 0) {
            runShadowAnimation(true);
            top = i;
        } else {
            runShadowAnimation(false);
        }
        this.frameLayout.setTranslationY(top);
        return top + AndroidUtilities.dp(12.0f);
    }

    @Override
    int getFirstOffset() {
        return getListTopPadding() + AndroidUtilities.dp(4.0f);
    }

    @Override
    public void setTranslationY(float f) {
        super.setTranslationY(f);
        this.parentAlert.getSheetContainer().invalidate();
    }

    @Override
    public int getListTopPadding() {
        return this.listView.getPaddingTop();
    }

    @Override
    void onPreMeasure(int i, int i2) {
        int i3;
        if (this.parentAlert.sizeNotifierFrameLayout.measureKeyboardHeight() > AndroidUtilities.dp(20.0f)) {
            i3 = AndroidUtilities.dp(8.0f);
            this.parentAlert.setAllowNestedScroll(false);
        } else {
            if (!AndroidUtilities.isTablet()) {
                Point point = AndroidUtilities.displaySize;
                if (point.x > point.y) {
                    i3 = (int) (i2 / 3.5f);
                    this.parentAlert.setAllowNestedScroll(true);
                }
            }
            i3 = (i2 / 5) * 2;
            this.parentAlert.setAllowNestedScroll(true);
        }
        if (this.listView.getPaddingTop() != i3) {
            this.ignoreLayout = true;
            this.listView.setPadding(0, i3, 0, 0);
            this.ignoreLayout = false;
        }
    }

    @Override
    public void requestLayout() {
        if (!this.ignoreLayout) {
            super.requestLayout();
        }
    }

    private void runShadowAnimation(final boolean z) {
        if ((z && this.shadow.getTag() != null) || (!z && this.shadow.getTag() == null)) {
            this.shadow.setTag(z ? null : 1);
            if (z) {
                this.shadow.setVisibility(0);
            }
            AnimatorSet animatorSet = this.shadowAnimation;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            AnimatorSet animatorSet2 = new AnimatorSet();
            this.shadowAnimation = animatorSet2;
            Animator[] animatorArr = new Animator[1];
            View view = this.shadow;
            Property property = View.ALPHA;
            float[] fArr = new float[1];
            fArr[0] = z ? 1.0f : 0.0f;
            animatorArr[0] = ObjectAnimator.ofFloat(view, property, fArr);
            animatorSet2.playTogether(animatorArr);
            this.shadowAnimation.setDuration(150L);
            this.shadowAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    if (ChatAttachAlertContactsLayout.this.shadowAnimation != null && ChatAttachAlertContactsLayout.this.shadowAnimation.equals(animator)) {
                        if (!z) {
                            ChatAttachAlertContactsLayout.this.shadow.setVisibility(4);
                        }
                        ChatAttachAlertContactsLayout.this.shadowAnimation = null;
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    if (ChatAttachAlertContactsLayout.this.shadowAnimation != null && ChatAttachAlertContactsLayout.this.shadowAnimation.equals(animator)) {
                        ChatAttachAlertContactsLayout.this.shadowAnimation = null;
                    }
                }
            });
            this.shadowAnimation.start();
        }
    }

    public int getCurrentTop() {
        if (this.listView.getChildCount() == 0) {
            return -1000;
        }
        int i = 0;
        View childAt = this.listView.getChildAt(0);
        RecyclerListView.Holder holder = (RecyclerListView.Holder) this.listView.findContainingViewHolder(childAt);
        if (holder == null) {
            return -1000;
        }
        int paddingTop = this.listView.getPaddingTop();
        if (holder.getAdapterPosition() == 0 && childAt.getTop() >= 0) {
            i = childAt.getTop();
        }
        return paddingTop - i;
    }

    public void setDelegate(PhonebookShareAlertDelegate phonebookShareAlertDelegate) {
        this.delegate = phonebookShareAlertDelegate;
    }

    @Override
    public void didReceivedNotification(int i, int i2, Object... objArr) {
        ShareAdapter shareAdapter;
        if (i == NotificationCenter.contactsDidLoad && (shareAdapter = this.listAdapter) != null) {
            shareAdapter.notifyDataSetChanged();
        }
    }

    @Override
    void onDestroy() {
        NotificationCenter.getInstance(this.parentAlert.currentAccount).removeObserver(this, NotificationCenter.contactsDidLoad);
    }

    @Override
    void onShow(ChatAttachAlert.AttachAlertLayout attachAlertLayout) {
        this.layoutManager.scrollToPositionWithOffset(0, 0);
    }

    @Override
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateEmptyViewPosition();
    }

    public void updateEmptyViewPosition() {
        View childAt;
        if (this.emptyView.getVisibility() == 0 && (childAt = this.listView.getChildAt(0)) != null) {
            EmptyTextProgressView emptyTextProgressView = this.emptyView;
            emptyTextProgressView.setTranslationY(((emptyTextProgressView.getMeasuredHeight() - getMeasuredHeight()) + childAt.getTop()) / 2);
        }
    }

    public void updateEmptyView() {
        int i = 0;
        boolean z = this.listView.getAdapter().getItemCount() == 2;
        EmptyTextProgressView emptyTextProgressView = this.emptyView;
        if (!z) {
            i = 8;
        }
        emptyTextProgressView.setVisibility(i);
        updateEmptyViewPosition();
    }

    public class ShareAdapter extends RecyclerListView.SectionsAdapter {
        private int currentAccount = UserConfig.selectedAccount;
        private Context mContext;

        @Override
        public String getLetter(int i) {
            return null;
        }

        @Override
        public View getSectionHeaderView(int i, View view) {
            return null;
        }

        public ShareAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public Object getItem(int i, int i2) {
            if (i == 0) {
                return null;
            }
            int i3 = i - 1;
            HashMap<String, ArrayList<Object>> hashMap = ContactsController.getInstance(this.currentAccount).phoneBookSectionsDict;
            ArrayList<String> arrayList = ContactsController.getInstance(this.currentAccount).phoneBookSectionsArray;
            if (i3 < arrayList.size()) {
                ArrayList<Object> arrayList2 = hashMap.get(arrayList.get(i3));
                if (i2 < arrayList2.size()) {
                    return arrayList2.get(i2);
                }
            }
            return null;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder viewHolder, int i, int i2) {
            if (i == 0 || i == getSectionCount() - 1) {
                return false;
            }
            return i2 < ContactsController.getInstance(this.currentAccount).phoneBookSectionsDict.get(ContactsController.getInstance(this.currentAccount).phoneBookSectionsArray.get(i + (-1))).size();
        }

        @Override
        public int getSectionCount() {
            return ContactsController.getInstance(this.currentAccount).phoneBookSectionsArray.size() + 2;
        }

        @Override
        public int getCountForSection(int i) {
            if (i == 0 || i == getSectionCount() - 1) {
                return 1;
            }
            int i2 = i - 1;
            HashMap<String, ArrayList<Object>> hashMap = ContactsController.getInstance(this.currentAccount).phoneBookSectionsDict;
            ArrayList<String> arrayList = ContactsController.getInstance(this.currentAccount).phoneBookSectionsArray;
            if (i2 < arrayList.size()) {
                return hashMap.get(arrayList.get(i2)).size();
            }
            return 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view;
            if (i == 0) {
                view = new UserCell(this.mContext, ChatAttachAlertContactsLayout.this.resourcesProvider);
            } else if (i != 1) {
                view = new View(this.mContext);
            } else {
                view = new View(this.mContext);
                view.setLayoutParams(new RecyclerView.LayoutParams(-1, AndroidUtilities.dp(56.0f)));
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(int i, int i2, RecyclerView.ViewHolder viewHolder) {
            final TLRPC$User tLRPC$User;
            if (viewHolder.getItemViewType() == 0) {
                UserCell userCell = (UserCell) viewHolder.itemView;
                Object item = getItem(i, i2);
                boolean z = true;
                if (i == getSectionCount() - 2 && i2 == getCountForSection(i) - 1) {
                    z = false;
                }
                if (item instanceof ContactsController.Contact) {
                    final ContactsController.Contact contact = (ContactsController.Contact) item;
                    tLRPC$User = contact.user;
                    if (tLRPC$User == null) {
                        userCell.setCurrentId(contact.contact_id);
                        userCell.setData((TLRPC$User) null, ContactsController.formatName(contact.first_name, contact.last_name), new UserCell.CharSequenceCallback() {
                            @Override
                            public final CharSequence run() {
                                CharSequence lambda$onBindViewHolder$0;
                                lambda$onBindViewHolder$0 = ChatAttachAlertContactsLayout.ShareAdapter.lambda$onBindViewHolder$0(ContactsController.Contact.this);
                                return lambda$onBindViewHolder$0;
                            }
                        }, z);
                        tLRPC$User = null;
                    }
                } else {
                    tLRPC$User = (TLRPC$User) item;
                }
                if (tLRPC$User != null) {
                    userCell.setData(tLRPC$User, (CharSequence) null, new UserCell.CharSequenceCallback() {
                        @Override
                        public final CharSequence run() {
                            CharSequence lambda$onBindViewHolder$1;
                            lambda$onBindViewHolder$1 = ChatAttachAlertContactsLayout.ShareAdapter.lambda$onBindViewHolder$1(TLRPC$User.this);
                            return lambda$onBindViewHolder$1;
                        }
                    }, z);
                }
            }
        }

        public static CharSequence lambda$onBindViewHolder$0(ContactsController.Contact contact) {
            return contact.phones.isEmpty() ? "" : PhoneFormat.getInstance().format(contact.phones.get(0));
        }

        public static CharSequence lambda$onBindViewHolder$1(TLRPC$User tLRPC$User) {
            PhoneFormat phoneFormat = PhoneFormat.getInstance();
            return phoneFormat.format("+" + tLRPC$User.phone);
        }

        @Override
        public int getItemViewType(int i, int i2) {
            if (i == 0) {
                return 1;
            }
            return i == getSectionCount() - 1 ? 2 : 0;
        }

        @Override
        public void getPositionForScrollProgress(RecyclerListView recyclerListView, float f, int[] iArr) {
            iArr[0] = 0;
            iArr[1] = 0;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            ChatAttachAlertContactsLayout.this.updateEmptyView();
        }
    }

    public class ShareSearchAdapter extends RecyclerListView.SelectionAdapter {
        private int lastSearchId;
        private Context mContext;
        private ArrayList<Object> searchResult = new ArrayList<>();
        private ArrayList<CharSequence> searchResultNames = new ArrayList<>();
        private Runnable searchRunnable;

        public ShareSearchAdapter(Context context) {
            this.mContext = context;
        }

        public void search(final String str) {
            if (this.searchRunnable != null) {
                Utilities.searchQueue.cancelRunnable(this.searchRunnable);
                this.searchRunnable = null;
            }
            if (str == null) {
                this.searchResult.clear();
                this.searchResultNames.clear();
                notifyDataSetChanged();
                return;
            }
            final int i = this.lastSearchId + 1;
            this.lastSearchId = i;
            DispatchQueue dispatchQueue = Utilities.searchQueue;
            Runnable chatAttachAlertContactsLayout$ShareSearchAdapter$$ExternalSyntheticLambda1 = new Runnable() {
                @Override
                public final void run() {
                    ChatAttachAlertContactsLayout.ShareSearchAdapter.this.lambda$search$0(str, i);
                }
            };
            this.searchRunnable = chatAttachAlertContactsLayout$ShareSearchAdapter$$ExternalSyntheticLambda1;
            dispatchQueue.postRunnable(chatAttachAlertContactsLayout$ShareSearchAdapter$$ExternalSyntheticLambda1, 300L);
        }

        public void lambda$search$0(final String str, final int i) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public final void run() {
                    ChatAttachAlertContactsLayout.ShareSearchAdapter.this.lambda$processSearch$2(str, i);
                }
            });
        }

        public void lambda$processSearch$2(final String str, final int i) {
            final int i2 = UserConfig.selectedAccount;
            final ArrayList arrayList = new ArrayList(ContactsController.getInstance(i2).contactsBook.values());
            final ArrayList arrayList2 = new ArrayList(ContactsController.getInstance(i2).contacts);
            Utilities.searchQueue.postRunnable(new Runnable() {
                @Override
                public final void run() {
                    ChatAttachAlertContactsLayout.ShareSearchAdapter.this.lambda$processSearch$1(str, arrayList, arrayList2, i2, i);
                }
            });
        }

        public void lambda$processSearch$1(java.lang.String r19, java.util.ArrayList r20, java.util.ArrayList r21, int r22, int r23) {
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.ChatAttachAlertContactsLayout.ShareSearchAdapter.lambda$processSearch$1(java.lang.String, java.util.ArrayList, java.util.ArrayList, int, int):void");
        }

        private void updateSearchResults(String str, final ArrayList<Object> arrayList, final ArrayList<CharSequence> arrayList2, final int i) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public final void run() {
                    ChatAttachAlertContactsLayout.ShareSearchAdapter.this.lambda$updateSearchResults$3(i, arrayList, arrayList2);
                }
            });
        }

        public void lambda$updateSearchResults$3(int i, ArrayList arrayList, ArrayList arrayList2) {
            if (i == this.lastSearchId) {
                if (!(i == -1 || ChatAttachAlertContactsLayout.this.listView.getAdapter() == ChatAttachAlertContactsLayout.this.searchAdapter)) {
                    ChatAttachAlertContactsLayout.this.listView.setAdapter(ChatAttachAlertContactsLayout.this.searchAdapter);
                }
                this.searchResult = arrayList;
                this.searchResultNames = arrayList2;
                notifyDataSetChanged();
            }
        }

        @Override
        public int getItemCount() {
            return this.searchResult.size() + 2;
        }

        public Object getItem(int i) {
            int i2 = i - 1;
            if (i2 < 0 || i2 >= this.searchResult.size()) {
                return null;
            }
            return this.searchResult.get(i2);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view;
            if (i == 0) {
                view = new UserCell(this.mContext, ChatAttachAlertContactsLayout.this.resourcesProvider);
            } else if (i != 1) {
                view = new View(this.mContext);
            } else {
                view = new View(this.mContext);
                view.setLayoutParams(new RecyclerView.LayoutParams(-1, AndroidUtilities.dp(56.0f)));
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            final TLRPC$User tLRPC$User;
            if (viewHolder.getItemViewType() == 0) {
                UserCell userCell = (UserCell) viewHolder.itemView;
                boolean z = i != getItemCount() + (-2);
                Object item = getItem(i);
                if (item instanceof ContactsController.Contact) {
                    final ContactsController.Contact contact = (ContactsController.Contact) item;
                    tLRPC$User = contact.user;
                    if (tLRPC$User == null) {
                        userCell.setCurrentId(contact.contact_id);
                        userCell.setData((TLRPC$User) null, this.searchResultNames.get(i - 1), new UserCell.CharSequenceCallback() {
                            @Override
                            public final CharSequence run() {
                                CharSequence lambda$onBindViewHolder$4;
                                lambda$onBindViewHolder$4 = ChatAttachAlertContactsLayout.ShareSearchAdapter.lambda$onBindViewHolder$4(ContactsController.Contact.this);
                                return lambda$onBindViewHolder$4;
                            }
                        }, z);
                        tLRPC$User = null;
                    }
                } else {
                    tLRPC$User = (TLRPC$User) item;
                }
                if (tLRPC$User != null) {
                    userCell.setData(tLRPC$User, this.searchResultNames.get(i - 1), new UserCell.CharSequenceCallback() {
                        @Override
                        public final CharSequence run() {
                            CharSequence lambda$onBindViewHolder$5;
                            lambda$onBindViewHolder$5 = ChatAttachAlertContactsLayout.ShareSearchAdapter.lambda$onBindViewHolder$5(TLRPC$User.this);
                            return lambda$onBindViewHolder$5;
                        }
                    }, z);
                }
            }
        }

        public static CharSequence lambda$onBindViewHolder$4(ContactsController.Contact contact) {
            return contact.phones.isEmpty() ? "" : PhoneFormat.getInstance().format(contact.phones.get(0));
        }

        public static CharSequence lambda$onBindViewHolder$5(TLRPC$User tLRPC$User) {
            PhoneFormat phoneFormat = PhoneFormat.getInstance();
            return phoneFormat.format("+" + tLRPC$User.phone);
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder viewHolder) {
            return viewHolder.getItemViewType() == 0;
        }

        @Override
        public int getItemViewType(int i) {
            if (i == 0) {
                return 1;
            }
            return i == getItemCount() - 1 ? 2 : 0;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            ChatAttachAlertContactsLayout.this.updateEmptyView();
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ThemeDescription.ThemeDescriptionDelegate chatAttachAlertContactsLayout$$ExternalSyntheticLambda0 = new ThemeDescription.ThemeDescriptionDelegate() {
            @Override
            public final void didSetColor() {
                ChatAttachAlertContactsLayout.this.lambda$getThemeDescriptions$2();
            }

            @Override
            public void onAnimationProgress(float f) {
                ThemeDescription.ThemeDescriptionDelegate.CC.$default$onAnimationProgress(this, f);
            }
        };
        ArrayList<ThemeDescription> arrayList = new ArrayList<>();
        arrayList.add(new ThemeDescription(this.frameLayout, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "dialogBackground"));
        arrayList.add(new ThemeDescription(this.shadow, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "dialogShadowLine"));
        arrayList.add(new ThemeDescription(this.searchField.getSearchBackground(), ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "dialogSearchBackground"));
        arrayList.add(new ThemeDescription(this.searchField, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{SearchField.class}, new String[]{"searchIconImageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogSearchIcon"));
        arrayList.add(new ThemeDescription(this.searchField, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{SearchField.class}, new String[]{"clearSearchImageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogSearchIcon"));
        arrayList.add(new ThemeDescription(this.searchField.getSearchEditText(), ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "dialogSearchText"));
        arrayList.add(new ThemeDescription(this.searchField.getSearchEditText(), ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "dialogSearchHint"));
        arrayList.add(new ThemeDescription(this.searchField.getSearchEditText(), ThemeDescription.FLAG_CURSORCOLOR, null, null, null, null, "featuredStickers_addedIcon"));
        arrayList.add(new ThemeDescription(this.emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "emptyListPlaceholder"));
        arrayList.add(new ThemeDescription(this.emptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, "progressCircle"));
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "dialogScrollGlow"));
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, "divider"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, new String[]{"nameTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogTextGray2"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, new String[]{"statusTextView"}, (Paint[]) null, (Drawable[]) null, chatAttachAlertContactsLayout$$ExternalSyntheticLambda0, "dialogTextGray2"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, null, Theme.avatarDrawables, null, "avatar_text"));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, chatAttachAlertContactsLayout$$ExternalSyntheticLambda0, "avatar_backgroundRed"));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, chatAttachAlertContactsLayout$$ExternalSyntheticLambda0, "avatar_backgroundOrange"));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, chatAttachAlertContactsLayout$$ExternalSyntheticLambda0, "avatar_backgroundViolet"));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, chatAttachAlertContactsLayout$$ExternalSyntheticLambda0, "avatar_backgroundGreen"));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, chatAttachAlertContactsLayout$$ExternalSyntheticLambda0, "avatar_backgroundCyan"));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, chatAttachAlertContactsLayout$$ExternalSyntheticLambda0, "avatar_backgroundBlue"));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, chatAttachAlertContactsLayout$$ExternalSyntheticLambda0, "avatar_backgroundPink"));
        return arrayList;
    }

    public void lambda$getThemeDescriptions$2() {
        RecyclerListView recyclerListView = this.listView;
        if (recyclerListView != null) {
            int childCount = recyclerListView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = this.listView.getChildAt(i);
                if (childAt instanceof UserCell) {
                    ((UserCell) childAt).update(0);
                }
            }
        }
    }
}
