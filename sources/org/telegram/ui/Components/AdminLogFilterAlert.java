package org.telegram.ui.Components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.collection.LongSparseArray;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC$ChannelParticipant;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventsFilter;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.CheckBoxUserCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.ContentPreviewViewer;

public class AdminLogFilterAlert extends BottomSheet {
    private ListAdapter adapter;
    private int adminsRow;
    private int allAdminsRow;
    private int callsRow;
    private ArrayList<TLRPC$ChannelParticipant> currentAdmins;
    private TLRPC$TL_channelAdminLogEventsFilter currentFilter;
    private AdminLogFilterAlertDelegate delegate;
    private int deleteRow;
    private int editRow;
    private boolean ignoreLayout;
    private int infoRow;
    private int invitesRow;
    private boolean isMegagroup;
    private int leavingRow;
    private RecyclerListView listView;
    private int membersRow;
    private int pinnedRow;
    private int restrictionsRow;
    private BottomSheet.BottomSheetCell saveButton;
    private int scrollOffsetY;
    private LongSparseArray<TLRPC$User> selectedAdmins;
    private Drawable shadowDrawable;

    public interface AdminLogFilterAlertDelegate {
        void didSelectRights(TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter, LongSparseArray<TLRPC$User> longSparseArray);
    }

    @Override
    protected boolean canDismissWithSwipe() {
        return false;
    }

    public AdminLogFilterAlert(Context context, TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter, LongSparseArray<TLRPC$User> longSparseArray, boolean z) {
        super(context, false);
        int i;
        if (tLRPC$TL_channelAdminLogEventsFilter != null) {
            TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter2 = new TLRPC$TL_channelAdminLogEventsFilter();
            this.currentFilter = tLRPC$TL_channelAdminLogEventsFilter2;
            tLRPC$TL_channelAdminLogEventsFilter2.join = tLRPC$TL_channelAdminLogEventsFilter.join;
            tLRPC$TL_channelAdminLogEventsFilter2.leave = tLRPC$TL_channelAdminLogEventsFilter.leave;
            tLRPC$TL_channelAdminLogEventsFilter2.invite = tLRPC$TL_channelAdminLogEventsFilter.invite;
            tLRPC$TL_channelAdminLogEventsFilter2.ban = tLRPC$TL_channelAdminLogEventsFilter.ban;
            tLRPC$TL_channelAdminLogEventsFilter2.unban = tLRPC$TL_channelAdminLogEventsFilter.unban;
            tLRPC$TL_channelAdminLogEventsFilter2.kick = tLRPC$TL_channelAdminLogEventsFilter.kick;
            tLRPC$TL_channelAdminLogEventsFilter2.unkick = tLRPC$TL_channelAdminLogEventsFilter.unkick;
            tLRPC$TL_channelAdminLogEventsFilter2.promote = tLRPC$TL_channelAdminLogEventsFilter.promote;
            tLRPC$TL_channelAdminLogEventsFilter2.demote = tLRPC$TL_channelAdminLogEventsFilter.demote;
            tLRPC$TL_channelAdminLogEventsFilter2.info = tLRPC$TL_channelAdminLogEventsFilter.info;
            tLRPC$TL_channelAdminLogEventsFilter2.settings = tLRPC$TL_channelAdminLogEventsFilter.settings;
            tLRPC$TL_channelAdminLogEventsFilter2.pinned = tLRPC$TL_channelAdminLogEventsFilter.pinned;
            tLRPC$TL_channelAdminLogEventsFilter2.edit = tLRPC$TL_channelAdminLogEventsFilter.edit;
            tLRPC$TL_channelAdminLogEventsFilter2.delete = tLRPC$TL_channelAdminLogEventsFilter.delete;
            tLRPC$TL_channelAdminLogEventsFilter2.group_call = tLRPC$TL_channelAdminLogEventsFilter.group_call;
            tLRPC$TL_channelAdminLogEventsFilter2.invites = tLRPC$TL_channelAdminLogEventsFilter.invites;
        }
        if (longSparseArray != null) {
            this.selectedAdmins = longSparseArray.clone();
        }
        this.isMegagroup = z;
        if (z) {
            i = 2;
            this.restrictionsRow = 1;
        } else {
            this.restrictionsRow = -1;
            i = 1;
        }
        int i2 = i + 1;
        this.adminsRow = i;
        int i3 = i2 + 1;
        this.membersRow = i2;
        int i4 = i3 + 1;
        this.invitesRow = i3;
        int i5 = i4 + 1;
        this.infoRow = i4;
        int i6 = i5 + 1;
        this.deleteRow = i5;
        int i7 = i6 + 1;
        this.editRow = i6;
        if (z) {
            i7++;
            this.pinnedRow = i7;
        } else {
            this.pinnedRow = -1;
        }
        int i8 = i7 + 1;
        this.leavingRow = i7;
        this.callsRow = i8;
        this.allAdminsRow = i8 + 1 + 1;
        Drawable mutate = context.getResources().getDrawable(R.drawable.sheet_shadow_round).mutate();
        this.shadowDrawable = mutate;
        mutate.setColorFilter(new PorterDuffColorFilter(Theme.getColor("dialogBackground"), PorterDuff.Mode.MULTIPLY));
        FrameLayout frameLayout = new FrameLayout(context) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
                if (motionEvent.getAction() != 0 || AdminLogFilterAlert.this.scrollOffsetY == 0 || motionEvent.getY() >= AdminLogFilterAlert.this.scrollOffsetY) {
                    return super.onInterceptTouchEvent(motionEvent);
                }
                AdminLogFilterAlert.this.dismiss();
                return true;
            }

            @Override
            public boolean onTouchEvent(MotionEvent motionEvent) {
                return !AdminLogFilterAlert.this.isDismissed() && super.onTouchEvent(motionEvent);
            }

            @Override
            protected void onMeasure(int i9, int i10) {
                int size = View.MeasureSpec.getSize(i10);
                if (Build.VERSION.SDK_INT >= 21) {
                    size -= AndroidUtilities.statusBarHeight;
                }
                getMeasuredWidth();
                int dp = AndroidUtilities.dp(48.0f) + ((AdminLogFilterAlert.this.isMegagroup ? 11 : 8) * AndroidUtilities.dp(48.0f)) + ((BottomSheet) AdminLogFilterAlert.this).backgroundPaddingTop + AndroidUtilities.dp(17.0f);
                if (AdminLogFilterAlert.this.currentAdmins != null) {
                    dp += ((AdminLogFilterAlert.this.currentAdmins.size() + 1) * AndroidUtilities.dp(48.0f)) + AndroidUtilities.dp(20.0f);
                }
                int i11 = size / 5;
                int i12 = ((float) dp) < ((float) i11) * 3.2f ? 0 : i11 * 2;
                if (i12 != 0 && dp < size) {
                    i12 -= size - dp;
                }
                if (i12 == 0) {
                    i12 = ((BottomSheet) AdminLogFilterAlert.this).backgroundPaddingTop;
                }
                if (AdminLogFilterAlert.this.listView.getPaddingTop() != i12) {
                    AdminLogFilterAlert.this.ignoreLayout = true;
                    AdminLogFilterAlert.this.listView.setPadding(0, i12, 0, 0);
                    AdminLogFilterAlert.this.ignoreLayout = false;
                }
                super.onMeasure(i9, View.MeasureSpec.makeMeasureSpec(Math.min(dp, size), 1073741824));
            }

            @Override
            protected void onLayout(boolean z2, int i9, int i10, int i11, int i12) {
                super.onLayout(z2, i9, i10, i11, i12);
                AdminLogFilterAlert.this.updateLayout();
            }

            @Override
            public void requestLayout() {
                if (!AdminLogFilterAlert.this.ignoreLayout) {
                    super.requestLayout();
                }
            }

            @Override
            protected void onDraw(Canvas canvas) {
                AdminLogFilterAlert.this.shadowDrawable.setBounds(0, AdminLogFilterAlert.this.scrollOffsetY - ((BottomSheet) AdminLogFilterAlert.this).backgroundPaddingTop, getMeasuredWidth(), getMeasuredHeight());
                AdminLogFilterAlert.this.shadowDrawable.draw(canvas);
            }
        };
        this.containerView = frameLayout;
        frameLayout.setWillNotDraw(false);
        ViewGroup viewGroup = this.containerView;
        int i9 = this.backgroundPaddingLeft;
        viewGroup.setPadding(i9, 0, i9, 0);
        RecyclerListView recyclerListView = new RecyclerListView(context) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
                return super.onInterceptTouchEvent(motionEvent) || ContentPreviewViewer.getInstance().onInterceptTouchEvent(motionEvent, AdminLogFilterAlert.this.listView, 0, null, this.resourcesProvider);
            }

            @Override
            public void requestLayout() {
                if (!AdminLogFilterAlert.this.ignoreLayout) {
                    super.requestLayout();
                }
            }
        };
        this.listView = recyclerListView;
        recyclerListView.setLayoutManager(new LinearLayoutManager(getContext(), 1, false));
        RecyclerListView recyclerListView2 = this.listView;
        ListAdapter listAdapter = new ListAdapter(context);
        this.adapter = listAdapter;
        recyclerListView2.setAdapter(listAdapter);
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setClipToPadding(false);
        this.listView.setEnabled(true);
        this.listView.setGlowColor(Theme.getColor("dialogScrollGlow"));
        this.listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int i10, int i11) {
                AdminLogFilterAlert.this.updateLayout();
            }
        });
        this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public final void onItemClick(View view, int i10) {
                AdminLogFilterAlert.this.lambda$new$0(view, i10);
            }
        });
        this.containerView.addView(this.listView, LayoutHelper.createFrame(-1, -1.0f, 51, 0.0f, 0.0f, 0.0f, 48.0f));
        View view = new View(context);
        view.setBackgroundResource(R.drawable.header_shadow_reverse);
        this.containerView.addView(view, LayoutHelper.createFrame(-1, 3.0f, 83, 0.0f, 0.0f, 0.0f, 48.0f));
        BottomSheet.BottomSheetCell bottomSheetCell = new BottomSheet.BottomSheetCell(context, 1);
        this.saveButton = bottomSheetCell;
        bottomSheetCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
        this.saveButton.setTextAndIcon(LocaleController.getString("Save", R.string.Save).toUpperCase(), 0);
        this.saveButton.setTextColor(Theme.getColor("dialogTextBlue2"));
        this.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view2) {
                AdminLogFilterAlert.this.lambda$new$1(view2);
            }
        });
        this.containerView.addView(this.saveButton, LayoutHelper.createFrame(-1, 48, 83));
        this.adapter.notifyDataSetChanged();
    }

    public void lambda$new$0(View view, int i) {
        TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter;
        TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter2;
        TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter3;
        TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter4;
        TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter5;
        TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter6;
        if (view instanceof CheckBoxCell) {
            CheckBoxCell checkBoxCell = (CheckBoxCell) view;
            boolean isChecked = checkBoxCell.isChecked();
            checkBoxCell.setChecked(!isChecked, true);
            if (i == 0) {
                if (isChecked) {
                    TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter7 = new TLRPC$TL_channelAdminLogEventsFilter();
                    this.currentFilter = tLRPC$TL_channelAdminLogEventsFilter7;
                    tLRPC$TL_channelAdminLogEventsFilter7.invites = false;
                    tLRPC$TL_channelAdminLogEventsFilter7.group_call = false;
                    tLRPC$TL_channelAdminLogEventsFilter7.delete = false;
                    tLRPC$TL_channelAdminLogEventsFilter7.edit = false;
                    tLRPC$TL_channelAdminLogEventsFilter7.pinned = false;
                    tLRPC$TL_channelAdminLogEventsFilter7.settings = false;
                    tLRPC$TL_channelAdminLogEventsFilter7.info = false;
                    tLRPC$TL_channelAdminLogEventsFilter7.demote = false;
                    tLRPC$TL_channelAdminLogEventsFilter7.promote = false;
                    tLRPC$TL_channelAdminLogEventsFilter7.unkick = false;
                    tLRPC$TL_channelAdminLogEventsFilter7.kick = false;
                    tLRPC$TL_channelAdminLogEventsFilter7.unban = false;
                    tLRPC$TL_channelAdminLogEventsFilter7.ban = false;
                    tLRPC$TL_channelAdminLogEventsFilter7.invite = false;
                    tLRPC$TL_channelAdminLogEventsFilter7.leave = false;
                    tLRPC$TL_channelAdminLogEventsFilter7.join = false;
                } else {
                    this.currentFilter = null;
                }
                int childCount = this.listView.getChildCount();
                for (int i2 = 0; i2 < childCount; i2++) {
                    View childAt = this.listView.getChildAt(i2);
                    RecyclerView.ViewHolder findContainingViewHolder = this.listView.findContainingViewHolder(childAt);
                    int adapterPosition = findContainingViewHolder.getAdapterPosition();
                    if (findContainingViewHolder.getItemViewType() == 0 && adapterPosition > 0 && adapterPosition < this.allAdminsRow - 1) {
                        ((CheckBoxCell) childAt).setChecked(!isChecked, true);
                    }
                }
            } else if (i == this.allAdminsRow) {
                if (isChecked) {
                    this.selectedAdmins = new LongSparseArray<>();
                } else {
                    this.selectedAdmins = null;
                }
                int childCount2 = this.listView.getChildCount();
                for (int i3 = 0; i3 < childCount2; i3++) {
                    View childAt2 = this.listView.getChildAt(i3);
                    RecyclerView.ViewHolder findContainingViewHolder2 = this.listView.findContainingViewHolder(childAt2);
                    findContainingViewHolder2.getAdapterPosition();
                    if (findContainingViewHolder2.getItemViewType() == 2) {
                        ((CheckBoxUserCell) childAt2).setChecked(!isChecked, true);
                    }
                }
            } else {
                if (this.currentFilter == null) {
                    TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter8 = new TLRPC$TL_channelAdminLogEventsFilter();
                    this.currentFilter = tLRPC$TL_channelAdminLogEventsFilter8;
                    tLRPC$TL_channelAdminLogEventsFilter8.invites = true;
                    tLRPC$TL_channelAdminLogEventsFilter8.group_call = true;
                    tLRPC$TL_channelAdminLogEventsFilter8.delete = true;
                    tLRPC$TL_channelAdminLogEventsFilter8.edit = true;
                    tLRPC$TL_channelAdminLogEventsFilter8.pinned = true;
                    tLRPC$TL_channelAdminLogEventsFilter8.settings = true;
                    tLRPC$TL_channelAdminLogEventsFilter8.info = true;
                    tLRPC$TL_channelAdminLogEventsFilter8.demote = true;
                    tLRPC$TL_channelAdminLogEventsFilter8.promote = true;
                    tLRPC$TL_channelAdminLogEventsFilter8.unkick = true;
                    tLRPC$TL_channelAdminLogEventsFilter8.kick = true;
                    tLRPC$TL_channelAdminLogEventsFilter8.unban = true;
                    tLRPC$TL_channelAdminLogEventsFilter8.ban = true;
                    tLRPC$TL_channelAdminLogEventsFilter8.invite = true;
                    tLRPC$TL_channelAdminLogEventsFilter8.leave = true;
                    tLRPC$TL_channelAdminLogEventsFilter8.join = true;
                    RecyclerView.ViewHolder findViewHolderForAdapterPosition = this.listView.findViewHolderForAdapterPosition(0);
                    if (findViewHolderForAdapterPosition != null) {
                        ((CheckBoxCell) findViewHolderForAdapterPosition.itemView).setChecked(false, true);
                    }
                }
                if (i == this.restrictionsRow) {
                    TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter9 = this.currentFilter;
                    boolean z = !tLRPC$TL_channelAdminLogEventsFilter9.kick;
                    tLRPC$TL_channelAdminLogEventsFilter9.unban = z;
                    tLRPC$TL_channelAdminLogEventsFilter9.unkick = z;
                    tLRPC$TL_channelAdminLogEventsFilter9.ban = z;
                    tLRPC$TL_channelAdminLogEventsFilter9.kick = z;
                } else if (i == this.adminsRow) {
                    TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter10 = this.currentFilter;
                    boolean z2 = !tLRPC$TL_channelAdminLogEventsFilter10.demote;
                    tLRPC$TL_channelAdminLogEventsFilter10.demote = z2;
                    tLRPC$TL_channelAdminLogEventsFilter10.promote = z2;
                } else if (i == this.membersRow) {
                    TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter11 = this.currentFilter;
                    boolean z3 = !tLRPC$TL_channelAdminLogEventsFilter11.join;
                    tLRPC$TL_channelAdminLogEventsFilter11.join = z3;
                    tLRPC$TL_channelAdminLogEventsFilter11.invite = z3;
                } else if (i == this.infoRow) {
                    TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter12 = this.currentFilter;
                    boolean z4 = !tLRPC$TL_channelAdminLogEventsFilter12.info;
                    tLRPC$TL_channelAdminLogEventsFilter12.settings = z4;
                    tLRPC$TL_channelAdminLogEventsFilter12.info = z4;
                } else if (i == this.deleteRow) {
                    this.currentFilter.delete = !tLRPC$TL_channelAdminLogEventsFilter6.delete;
                } else if (i == this.editRow) {
                    this.currentFilter.edit = !tLRPC$TL_channelAdminLogEventsFilter5.edit;
                } else if (i == this.pinnedRow) {
                    this.currentFilter.pinned = !tLRPC$TL_channelAdminLogEventsFilter4.pinned;
                } else if (i == this.leavingRow) {
                    this.currentFilter.leave = !tLRPC$TL_channelAdminLogEventsFilter3.leave;
                } else if (i == this.callsRow) {
                    this.currentFilter.group_call = !tLRPC$TL_channelAdminLogEventsFilter2.group_call;
                } else if (i == this.invitesRow) {
                    this.currentFilter.invites = !tLRPC$TL_channelAdminLogEventsFilter.invites;
                }
            }
            TLRPC$TL_channelAdminLogEventsFilter tLRPC$TL_channelAdminLogEventsFilter13 = this.currentFilter;
            if (tLRPC$TL_channelAdminLogEventsFilter13 == null || tLRPC$TL_channelAdminLogEventsFilter13.join || tLRPC$TL_channelAdminLogEventsFilter13.leave || tLRPC$TL_channelAdminLogEventsFilter13.invite || tLRPC$TL_channelAdminLogEventsFilter13.ban || tLRPC$TL_channelAdminLogEventsFilter13.invites || tLRPC$TL_channelAdminLogEventsFilter13.unban || tLRPC$TL_channelAdminLogEventsFilter13.kick || tLRPC$TL_channelAdminLogEventsFilter13.unkick || tLRPC$TL_channelAdminLogEventsFilter13.promote || tLRPC$TL_channelAdminLogEventsFilter13.demote || tLRPC$TL_channelAdminLogEventsFilter13.info || tLRPC$TL_channelAdminLogEventsFilter13.settings || tLRPC$TL_channelAdminLogEventsFilter13.pinned || tLRPC$TL_channelAdminLogEventsFilter13.edit || tLRPC$TL_channelAdminLogEventsFilter13.delete || tLRPC$TL_channelAdminLogEventsFilter13.group_call) {
                this.saveButton.setEnabled(true);
                this.saveButton.setAlpha(1.0f);
                return;
            }
            this.saveButton.setEnabled(false);
            this.saveButton.setAlpha(0.5f);
        } else if (view instanceof CheckBoxUserCell) {
            CheckBoxUserCell checkBoxUserCell = (CheckBoxUserCell) view;
            if (this.selectedAdmins == null) {
                this.selectedAdmins = new LongSparseArray<>();
                RecyclerView.ViewHolder findViewHolderForAdapterPosition2 = this.listView.findViewHolderForAdapterPosition(this.allAdminsRow);
                if (findViewHolderForAdapterPosition2 != null) {
                    ((CheckBoxCell) findViewHolderForAdapterPosition2.itemView).setChecked(false, true);
                }
                for (int i4 = 0; i4 < this.currentAdmins.size(); i4++) {
                    TLRPC$User user = MessagesController.getInstance(this.currentAccount).getUser(Long.valueOf(MessageObject.getPeerId(this.currentAdmins.get(i4).peer)));
                    this.selectedAdmins.put(user.id, user);
                }
            }
            boolean isChecked2 = checkBoxUserCell.isChecked();
            TLRPC$User currentUser = checkBoxUserCell.getCurrentUser();
            if (isChecked2) {
                this.selectedAdmins.remove(currentUser.id);
            } else {
                this.selectedAdmins.put(currentUser.id, currentUser);
            }
            checkBoxUserCell.setChecked(!isChecked2, true);
        }
    }

    public void lambda$new$1(View view) {
        this.delegate.didSelectRights(this.currentFilter, this.selectedAdmins);
        dismiss();
    }

    public void setCurrentAdmins(ArrayList<TLRPC$ChannelParticipant> arrayList) {
        this.currentAdmins = arrayList;
        ListAdapter listAdapter = this.adapter;
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    public void setAdminLogFilterAlertDelegate(AdminLogFilterAlertDelegate adminLogFilterAlertDelegate) {
        this.delegate = adminLogFilterAlertDelegate;
    }

    @SuppressLint({"NewApi"})
    public void updateLayout() {
        if (this.listView.getChildCount() <= 0) {
            RecyclerListView recyclerListView = this.listView;
            int paddingTop = recyclerListView.getPaddingTop();
            this.scrollOffsetY = paddingTop;
            recyclerListView.setTopGlowOffset(paddingTop);
            this.containerView.invalidate();
            return;
        }
        int i = 0;
        View childAt = this.listView.getChildAt(0);
        RecyclerListView.Holder holder = (RecyclerListView.Holder) this.listView.findContainingViewHolder(childAt);
        int top = childAt.getTop() - AndroidUtilities.dp(8.0f);
        if (top > 0 && holder != null && holder.getAdapterPosition() == 0) {
            i = top;
        }
        if (this.scrollOffsetY != i) {
            RecyclerListView recyclerListView2 = this.listView;
            this.scrollOffsetY = i;
            recyclerListView2.setTopGlowOffset(i);
            this.containerView.invalidate();
        }
    }

    public class ListAdapter extends RecyclerListView.SelectionAdapter {
        private Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getItemCount() {
            return (AdminLogFilterAlert.this.isMegagroup ? 11 : 8) + (AdminLogFilterAlert.this.currentAdmins != null ? AdminLogFilterAlert.this.currentAdmins.size() + 2 : 0);
        }

        @Override
        public int getItemViewType(int i) {
            if (i < AdminLogFilterAlert.this.allAdminsRow - 1 || i == AdminLogFilterAlert.this.allAdminsRow) {
                return 0;
            }
            return i == AdminLogFilterAlert.this.allAdminsRow - 1 ? 1 : 2;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder viewHolder) {
            return viewHolder.getItemViewType() != 1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            FrameLayout frameLayout;
            FrameLayout frameLayout2;
            if (i == 0) {
                frameLayout2 = new CheckBoxCell(this.context, 1, 21, ((BottomSheet) AdminLogFilterAlert.this).resourcesProvider);
            } else if (i == 1) {
                View shadowSectionCell = new ShadowSectionCell(this.context, 18);
                frameLayout2 = new FrameLayout(this.context);
                frameLayout2.addView(shadowSectionCell, LayoutHelper.createFrame(-1, -1.0f));
                frameLayout2.setBackgroundColor(Theme.getColor("dialogBackgroundGray"));
            } else if (i != 2) {
                frameLayout = null;
                return new RecyclerListView.Holder(frameLayout);
            } else {
                frameLayout2 = new CheckBoxUserCell(this.context, true);
            }
            frameLayout = frameLayout2;
            return new RecyclerListView.Holder(frameLayout);
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder viewHolder) {
            int adapterPosition = viewHolder.getAdapterPosition();
            int itemViewType = viewHolder.getItemViewType();
            boolean z = true;
            if (itemViewType == 0) {
                CheckBoxCell checkBoxCell = (CheckBoxCell) viewHolder.itemView;
                if (adapterPosition == 0) {
                    if (AdminLogFilterAlert.this.currentFilter != null) {
                        z = false;
                    }
                    checkBoxCell.setChecked(z, false);
                } else if (adapterPosition == AdminLogFilterAlert.this.restrictionsRow) {
                    if (AdminLogFilterAlert.this.currentFilter != null && (!AdminLogFilterAlert.this.currentFilter.kick || !AdminLogFilterAlert.this.currentFilter.ban || !AdminLogFilterAlert.this.currentFilter.unkick || !AdminLogFilterAlert.this.currentFilter.unban)) {
                        z = false;
                    }
                    checkBoxCell.setChecked(z, false);
                } else if (adapterPosition == AdminLogFilterAlert.this.adminsRow) {
                    if (AdminLogFilterAlert.this.currentFilter != null && (!AdminLogFilterAlert.this.currentFilter.promote || !AdminLogFilterAlert.this.currentFilter.demote)) {
                        z = false;
                    }
                    checkBoxCell.setChecked(z, false);
                } else if (adapterPosition == AdminLogFilterAlert.this.membersRow) {
                    if (AdminLogFilterAlert.this.currentFilter != null && (!AdminLogFilterAlert.this.currentFilter.invite || !AdminLogFilterAlert.this.currentFilter.join)) {
                        z = false;
                    }
                    checkBoxCell.setChecked(z, false);
                } else if (adapterPosition == AdminLogFilterAlert.this.infoRow) {
                    if (AdminLogFilterAlert.this.currentFilter != null && !AdminLogFilterAlert.this.currentFilter.info) {
                        z = false;
                    }
                    checkBoxCell.setChecked(z, false);
                } else if (adapterPosition == AdminLogFilterAlert.this.deleteRow) {
                    if (AdminLogFilterAlert.this.currentFilter != null && !AdminLogFilterAlert.this.currentFilter.delete) {
                        z = false;
                    }
                    checkBoxCell.setChecked(z, false);
                } else if (adapterPosition == AdminLogFilterAlert.this.editRow) {
                    if (AdminLogFilterAlert.this.currentFilter != null && !AdminLogFilterAlert.this.currentFilter.edit) {
                        z = false;
                    }
                    checkBoxCell.setChecked(z, false);
                } else if (adapterPosition == AdminLogFilterAlert.this.pinnedRow) {
                    if (AdminLogFilterAlert.this.currentFilter != null && !AdminLogFilterAlert.this.currentFilter.pinned) {
                        z = false;
                    }
                    checkBoxCell.setChecked(z, false);
                } else if (adapterPosition == AdminLogFilterAlert.this.leavingRow) {
                    if (AdminLogFilterAlert.this.currentFilter != null && !AdminLogFilterAlert.this.currentFilter.leave) {
                        z = false;
                    }
                    checkBoxCell.setChecked(z, false);
                } else if (adapterPosition == AdminLogFilterAlert.this.callsRow) {
                    if (AdminLogFilterAlert.this.currentFilter != null && !AdminLogFilterAlert.this.currentFilter.group_call) {
                        z = false;
                    }
                    checkBoxCell.setChecked(z, false);
                } else if (adapterPosition == AdminLogFilterAlert.this.invitesRow) {
                    if (AdminLogFilterAlert.this.currentFilter != null && !AdminLogFilterAlert.this.currentFilter.invites) {
                        z = false;
                    }
                    checkBoxCell.setChecked(z, false);
                } else if (adapterPosition == AdminLogFilterAlert.this.allAdminsRow) {
                    if (AdminLogFilterAlert.this.selectedAdmins != null) {
                        z = false;
                    }
                    checkBoxCell.setChecked(z, false);
                }
            } else if (itemViewType == 2) {
                CheckBoxUserCell checkBoxUserCell = (CheckBoxUserCell) viewHolder.itemView;
                long peerId = MessageObject.getPeerId(((TLRPC$ChannelParticipant) AdminLogFilterAlert.this.currentAdmins.get((adapterPosition - AdminLogFilterAlert.this.allAdminsRow) - 1)).peer);
                if (AdminLogFilterAlert.this.selectedAdmins != null && AdminLogFilterAlert.this.selectedAdmins.indexOfKey(peerId) < 0) {
                    z = false;
                }
                checkBoxUserCell.setChecked(z, false);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            int itemViewType = viewHolder.getItemViewType();
            boolean z = false;
            boolean z2 = true;
            if (itemViewType == 0) {
                CheckBoxCell checkBoxCell = (CheckBoxCell) viewHolder.itemView;
                if (i == 0) {
                    String string = LocaleController.getString("EventLogFilterAll", R.string.EventLogFilterAll);
                    if (AdminLogFilterAlert.this.currentFilter == null) {
                        z = true;
                    }
                    checkBoxCell.setText(string, "", z, true);
                } else if (i == AdminLogFilterAlert.this.restrictionsRow) {
                    String string2 = LocaleController.getString("EventLogFilterNewRestrictions", R.string.EventLogFilterNewRestrictions);
                    if (AdminLogFilterAlert.this.currentFilter == null || (AdminLogFilterAlert.this.currentFilter.kick && AdminLogFilterAlert.this.currentFilter.ban && AdminLogFilterAlert.this.currentFilter.unkick && AdminLogFilterAlert.this.currentFilter.unban)) {
                        z = true;
                    }
                    checkBoxCell.setText(string2, "", z, true);
                } else if (i == AdminLogFilterAlert.this.adminsRow) {
                    String string3 = LocaleController.getString("EventLogFilterNewAdmins", R.string.EventLogFilterNewAdmins);
                    if (AdminLogFilterAlert.this.currentFilter == null || (AdminLogFilterAlert.this.currentFilter.promote && AdminLogFilterAlert.this.currentFilter.demote)) {
                        z = true;
                    }
                    checkBoxCell.setText(string3, "", z, true);
                } else if (i == AdminLogFilterAlert.this.membersRow) {
                    String string4 = LocaleController.getString("EventLogFilterNewMembers", R.string.EventLogFilterNewMembers);
                    if (AdminLogFilterAlert.this.currentFilter == null || (AdminLogFilterAlert.this.currentFilter.invite && AdminLogFilterAlert.this.currentFilter.join)) {
                        z = true;
                    }
                    checkBoxCell.setText(string4, "", z, true);
                } else if (i == AdminLogFilterAlert.this.infoRow) {
                    if (AdminLogFilterAlert.this.isMegagroup) {
                        String string5 = LocaleController.getString("EventLogFilterGroupInfo", R.string.EventLogFilterGroupInfo);
                        if (AdminLogFilterAlert.this.currentFilter == null || AdminLogFilterAlert.this.currentFilter.info) {
                            z = true;
                        }
                        checkBoxCell.setText(string5, "", z, true);
                        return;
                    }
                    String string6 = LocaleController.getString("EventLogFilterChannelInfo", R.string.EventLogFilterChannelInfo);
                    if (AdminLogFilterAlert.this.currentFilter == null || AdminLogFilterAlert.this.currentFilter.info) {
                        z = true;
                    }
                    checkBoxCell.setText(string6, "", z, true);
                } else if (i == AdminLogFilterAlert.this.deleteRow) {
                    String string7 = LocaleController.getString("EventLogFilterDeletedMessages", R.string.EventLogFilterDeletedMessages);
                    if (AdminLogFilterAlert.this.currentFilter == null || AdminLogFilterAlert.this.currentFilter.delete) {
                        z = true;
                    }
                    checkBoxCell.setText(string7, "", z, true);
                } else if (i == AdminLogFilterAlert.this.editRow) {
                    String string8 = LocaleController.getString("EventLogFilterEditedMessages", R.string.EventLogFilterEditedMessages);
                    if (AdminLogFilterAlert.this.currentFilter == null || AdminLogFilterAlert.this.currentFilter.edit) {
                        z = true;
                    }
                    checkBoxCell.setText(string8, "", z, true);
                } else if (i == AdminLogFilterAlert.this.pinnedRow) {
                    String string9 = LocaleController.getString("EventLogFilterPinnedMessages", R.string.EventLogFilterPinnedMessages);
                    if (AdminLogFilterAlert.this.currentFilter == null || AdminLogFilterAlert.this.currentFilter.pinned) {
                        z = true;
                    }
                    checkBoxCell.setText(string9, "", z, true);
                } else if (i == AdminLogFilterAlert.this.leavingRow) {
                    String string10 = LocaleController.getString("EventLogFilterLeavingMembers", R.string.EventLogFilterLeavingMembers);
                    boolean z3 = AdminLogFilterAlert.this.currentFilter == null || AdminLogFilterAlert.this.currentFilter.leave;
                    if (AdminLogFilterAlert.this.callsRow != -1) {
                        z = true;
                    }
                    checkBoxCell.setText(string10, "", z3, z);
                } else if (i == AdminLogFilterAlert.this.callsRow) {
                    String string11 = LocaleController.getString("EventLogFilterCalls", R.string.EventLogFilterCalls);
                    if (AdminLogFilterAlert.this.currentFilter != null && !AdminLogFilterAlert.this.currentFilter.group_call) {
                        z2 = false;
                    }
                    checkBoxCell.setText(string11, "", z2, false);
                } else if (i == AdminLogFilterAlert.this.invitesRow) {
                    String string12 = LocaleController.getString("EventLogFilterInvites", R.string.EventLogFilterInvites);
                    if (AdminLogFilterAlert.this.currentFilter == null || AdminLogFilterAlert.this.currentFilter.invites) {
                        z = true;
                    }
                    checkBoxCell.setText(string12, "", z, true);
                } else if (i == AdminLogFilterAlert.this.allAdminsRow) {
                    String string13 = LocaleController.getString("EventLogAllAdmins", R.string.EventLogAllAdmins);
                    if (AdminLogFilterAlert.this.selectedAdmins == null) {
                        z = true;
                    }
                    checkBoxCell.setText(string13, "", z, true);
                }
            } else if (itemViewType == 2) {
                CheckBoxUserCell checkBoxUserCell = (CheckBoxUserCell) viewHolder.itemView;
                long peerId = MessageObject.getPeerId(((TLRPC$ChannelParticipant) AdminLogFilterAlert.this.currentAdmins.get((i - AdminLogFilterAlert.this.allAdminsRow) - 1)).peer);
                TLRPC$User user = MessagesController.getInstance(((BottomSheet) AdminLogFilterAlert.this).currentAccount).getUser(Long.valueOf(peerId));
                boolean z4 = AdminLogFilterAlert.this.selectedAdmins == null || AdminLogFilterAlert.this.selectedAdmins.indexOfKey(peerId) >= 0;
                if (i != getItemCount() - 1) {
                    z = true;
                }
                checkBoxUserCell.setUser(user, z4, z);
            }
        }
    }
}
