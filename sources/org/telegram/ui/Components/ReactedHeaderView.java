package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$ChatFull;
import org.telegram.tgnet.TLRPC$Peer;
import org.telegram.tgnet.TLRPC$TL_availableReaction;
import org.telegram.tgnet.TLRPC$TL_channelParticipantsRecent;
import org.telegram.tgnet.TLRPC$TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC$TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_messageActionChatJoinedByRequest;
import org.telegram.tgnet.TLRPC$TL_messageReactions;
import org.telegram.tgnet.TLRPC$TL_messages_chatFull;
import org.telegram.tgnet.TLRPC$TL_messages_getFullChat;
import org.telegram.tgnet.TLRPC$TL_messages_getMessageReactionsList;
import org.telegram.tgnet.TLRPC$TL_messages_getMessageReadParticipants;
import org.telegram.tgnet.TLRPC$TL_messages_messageReactionsList;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.tgnet.TLRPC$Vector;
import org.telegram.ui.ActionBar.Theme;

public class ReactedHeaderView extends FrameLayout {
    private AvatarsImageView avatarsImageView;
    private int currentAccount;
    private FlickerLoadingView flickerLoadingView;
    private ImageView iconView;
    private boolean ignoreLayout;
    private boolean isLoaded;
    private MessageObject message;
    private BackupImageView reactView;
    private Consumer<List<TLRPC$User>> seenCallback;
    private TextView titleView;
    private List<TLRPC$User> seenUsers = new ArrayList();
    private List<TLRPC$User> users = new ArrayList();

    public ReactedHeaderView(Context context, int i, MessageObject messageObject, long j) {
        super(context);
        this.currentAccount = i;
        this.message = messageObject;
        FlickerLoadingView flickerLoadingView = new FlickerLoadingView(context);
        this.flickerLoadingView = flickerLoadingView;
        flickerLoadingView.setColors("actionBarDefaultSubmenuBackground", "listSelectorSDK21", null);
        this.flickerLoadingView.setViewType(13);
        this.flickerLoadingView.setIsSingleCell(false);
        addView(this.flickerLoadingView, LayoutHelper.createFrame(-2, -1.0f));
        TextView textView = new TextView(context);
        this.titleView = textView;
        textView.setTextColor(Theme.getColor("actionBarDefaultSubmenuItem"));
        this.titleView.setTextSize(1, 16.0f);
        this.titleView.setLines(1);
        this.titleView.setEllipsize(TextUtils.TruncateAt.END);
        addView(this.titleView, LayoutHelper.createFrameRelatively(-2.0f, -2.0f, 8388627, 40.0f, 0.0f, 62.0f, 0.0f));
        AvatarsImageView avatarsImageView = new AvatarsImageView(context, false);
        this.avatarsImageView = avatarsImageView;
        avatarsImageView.setStyle(11);
        addView(this.avatarsImageView, LayoutHelper.createFrameRelatively(56.0f, -1.0f, 8388629, 0.0f, 0.0f, 0.0f, 0.0f));
        ImageView imageView = new ImageView(context);
        this.iconView = imageView;
        addView(imageView, LayoutHelper.createFrameRelatively(24.0f, 24.0f, 8388627, 11.0f, 0.0f, 0.0f, 0.0f));
        Drawable mutate = ContextCompat.getDrawable(context, R.drawable.msg_reactions).mutate();
        mutate.setColorFilter(new PorterDuffColorFilter(Theme.getColor("actionBarDefaultSubmenuItemIcon"), PorterDuff.Mode.MULTIPLY));
        this.iconView.setImageDrawable(mutate);
        this.iconView.setVisibility(8);
        BackupImageView backupImageView = new BackupImageView(context);
        this.reactView = backupImageView;
        addView(backupImageView, LayoutHelper.createFrameRelatively(24.0f, 24.0f, 8388627, 11.0f, 0.0f, 0.0f, 0.0f));
        this.titleView.setAlpha(0.0f);
        this.avatarsImageView.setAlpha(0.0f);
        setBackground(Theme.getSelectorDrawable(false));
    }

    public void setSeenCallback(Consumer<List<TLRPC$User>> consumer) {
        this.seenCallback = consumer;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.isLoaded) {
            MessagesController messagesController = MessagesController.getInstance(this.currentAccount);
            final TLRPC$Chat chat = messagesController.getChat(Long.valueOf(this.message.getChatId()));
            TLRPC$ChatFull chatFull = messagesController.getChatFull(this.message.getChatId());
            if (chat != null && this.message.isOutOwner() && this.message.isSent() && !this.message.isEditing() && !this.message.isSending() && !this.message.isSendError() && !this.message.isContentUnread() && !this.message.isUnread() && ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() - this.message.messageOwner.date < 604800 && (ChatObject.isMegagroup(chat) || !ChatObject.isChannel(chat)) && chatFull != null && chatFull.participants_count <= MessagesController.getInstance(this.currentAccount).chatReadMarkSizeThreshold && !(this.message.messageOwner.action instanceof TLRPC$TL_messageActionChatJoinedByRequest)) {
                TLRPC$TL_messages_getMessageReadParticipants tLRPC$TL_messages_getMessageReadParticipants = new TLRPC$TL_messages_getMessageReadParticipants();
                tLRPC$TL_messages_getMessageReadParticipants.msg_id = this.message.getId();
                tLRPC$TL_messages_getMessageReadParticipants.peer = MessagesController.getInstance(this.currentAccount).getInputPeer(this.message.getDialogId());
                TLRPC$Peer tLRPC$Peer = this.message.messageOwner.from_id;
                final long j = tLRPC$Peer != null ? tLRPC$Peer.user_id : 0L;
                ConnectionsManager.getInstance(this.currentAccount).sendRequest(tLRPC$TL_messages_getMessageReadParticipants, new RequestDelegate() {
                    @Override
                    public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                        ReactedHeaderView.this.lambda$onAttachedToWindow$5(j, chat, tLObject, tLRPC$TL_error);
                    }
                }, 64);
                return;
            }
            loadReactions();
        }
    }

    public void lambda$onAttachedToWindow$5(long j, TLRPC$Chat tLRPC$Chat, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        if (tLObject instanceof TLRPC$Vector) {
            final ArrayList arrayList = new ArrayList();
            Iterator<Object> it = ((TLRPC$Vector) tLObject).objects.iterator();
            while (it.hasNext()) {
                Object next = it.next();
                if (next instanceof Long) {
                    long longValue = ((Long) next).longValue();
                    if (j != longValue) {
                        arrayList.add(Long.valueOf(longValue));
                    }
                }
            }
            arrayList.add(Long.valueOf(j));
            final ArrayList arrayList2 = new ArrayList();
            final Runnable reactedHeaderView$$ExternalSyntheticLambda1 = new Runnable() {
                @Override
                public final void run() {
                    ReactedHeaderView.this.lambda$onAttachedToWindow$0(arrayList2);
                }
            };
            if (ChatObject.isChannel(tLRPC$Chat)) {
                TLRPC$TL_channels_getParticipants tLRPC$TL_channels_getParticipants = new TLRPC$TL_channels_getParticipants();
                tLRPC$TL_channels_getParticipants.limit = MessagesController.getInstance(this.currentAccount).chatReadMarkSizeThreshold;
                tLRPC$TL_channels_getParticipants.offset = 0;
                tLRPC$TL_channels_getParticipants.filter = new TLRPC$TL_channelParticipantsRecent();
                tLRPC$TL_channels_getParticipants.channel = MessagesController.getInstance(this.currentAccount).getInputChannel(tLRPC$Chat.id);
                ConnectionsManager.getInstance(this.currentAccount).sendRequest(tLRPC$TL_channels_getParticipants, new RequestDelegate() {
                    @Override
                    public final void run(TLObject tLObject2, TLRPC$TL_error tLRPC$TL_error2) {
                        ReactedHeaderView.this.lambda$onAttachedToWindow$2(arrayList, arrayList2, reactedHeaderView$$ExternalSyntheticLambda1, tLObject2, tLRPC$TL_error2);
                    }
                });
                return;
            }
            TLRPC$TL_messages_getFullChat tLRPC$TL_messages_getFullChat = new TLRPC$TL_messages_getFullChat();
            tLRPC$TL_messages_getFullChat.chat_id = tLRPC$Chat.id;
            ConnectionsManager.getInstance(this.currentAccount).sendRequest(tLRPC$TL_messages_getFullChat, new RequestDelegate() {
                @Override
                public final void run(TLObject tLObject2, TLRPC$TL_error tLRPC$TL_error2) {
                    ReactedHeaderView.this.lambda$onAttachedToWindow$4(arrayList, arrayList2, reactedHeaderView$$ExternalSyntheticLambda1, tLObject2, tLRPC$TL_error2);
                }
            });
        }
    }

    public void lambda$onAttachedToWindow$0(List list) {
        this.seenUsers.addAll(list);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            TLRPC$User tLRPC$User = (TLRPC$User) it.next();
            boolean z = false;
            int i = 0;
            while (true) {
                if (i >= this.users.size()) {
                    break;
                } else if (this.users.get(i).id == tLRPC$User.id) {
                    z = true;
                    break;
                } else {
                    i++;
                }
            }
            if (!z) {
                this.users.add(tLRPC$User);
            }
        }
        Consumer<List<TLRPC$User>> consumer = this.seenCallback;
        if (consumer != null) {
            consumer.accept(list);
        }
        loadReactions();
    }

    public void lambda$onAttachedToWindow$2(final List list, final List list2, final Runnable runnable, final TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public final void run() {
                ReactedHeaderView.this.lambda$onAttachedToWindow$1(tLObject, list, list2, runnable);
            }
        });
    }

    public void lambda$onAttachedToWindow$1(TLObject tLObject, List list, List list2, Runnable runnable) {
        if (tLObject != null) {
            TLRPC$TL_channels_channelParticipants tLRPC$TL_channels_channelParticipants = (TLRPC$TL_channels_channelParticipants) tLObject;
            for (int i = 0; i < tLRPC$TL_channels_channelParticipants.users.size(); i++) {
                TLRPC$User tLRPC$User = tLRPC$TL_channels_channelParticipants.users.get(i);
                MessagesController.getInstance(this.currentAccount).putUser(tLRPC$User, false);
                if (!tLRPC$User.self && list.contains(Long.valueOf(tLRPC$User.id))) {
                    list2.add(tLRPC$User);
                }
            }
        }
        runnable.run();
    }

    public void lambda$onAttachedToWindow$4(final List list, final List list2, final Runnable runnable, final TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public final void run() {
                ReactedHeaderView.this.lambda$onAttachedToWindow$3(tLObject, list, list2, runnable);
            }
        });
    }

    public void lambda$onAttachedToWindow$3(TLObject tLObject, List list, List list2, Runnable runnable) {
        if (tLObject != null) {
            TLRPC$TL_messages_chatFull tLRPC$TL_messages_chatFull = (TLRPC$TL_messages_chatFull) tLObject;
            for (int i = 0; i < tLRPC$TL_messages_chatFull.users.size(); i++) {
                TLRPC$User tLRPC$User = tLRPC$TL_messages_chatFull.users.get(i);
                MessagesController.getInstance(this.currentAccount).putUser(tLRPC$User, false);
                if (!tLRPC$User.self && list.contains(Long.valueOf(tLRPC$User.id))) {
                    list2.add(tLRPC$User);
                }
            }
        }
        runnable.run();
    }

    private void loadReactions() {
        MessagesController messagesController = MessagesController.getInstance(this.currentAccount);
        TLRPC$TL_messages_getMessageReactionsList tLRPC$TL_messages_getMessageReactionsList = new TLRPC$TL_messages_getMessageReactionsList();
        tLRPC$TL_messages_getMessageReactionsList.peer = messagesController.getInputPeer(this.message.getDialogId());
        tLRPC$TL_messages_getMessageReactionsList.id = this.message.getId();
        tLRPC$TL_messages_getMessageReactionsList.limit = 3;
        tLRPC$TL_messages_getMessageReactionsList.reaction = null;
        tLRPC$TL_messages_getMessageReactionsList.offset = null;
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(tLRPC$TL_messages_getMessageReactionsList, new RequestDelegate() {
            @Override
            public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                ReactedHeaderView.this.lambda$loadReactions$7(tLObject, tLRPC$TL_error);
            }
        }, 64);
    }

    public void lambda$loadReactions$7(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        if (tLObject instanceof TLRPC$TL_messages_messageReactionsList) {
            final TLRPC$TL_messages_messageReactionsList tLRPC$TL_messages_messageReactionsList = (TLRPC$TL_messages_messageReactionsList) tLObject;
            final int i = tLRPC$TL_messages_messageReactionsList.count;
            post(new Runnable() {
                @Override
                public final void run() {
                    ReactedHeaderView.this.lambda$loadReactions$6(i, tLRPC$TL_messages_messageReactionsList);
                }
            });
        }
    }

    public void lambda$loadReactions$6(int i, TLRPC$TL_messages_messageReactionsList tLRPC$TL_messages_messageReactionsList) {
        String str;
        boolean z;
        boolean z2;
        if (this.seenUsers.isEmpty() || this.seenUsers.size() < i) {
            str = LocaleController.formatPluralString("ReactionsCount", i, new Object[0]);
        } else {
            str = String.format(LocaleController.getPluralString("Reacted", i), i == this.seenUsers.size() ? String.valueOf(i) : i + "/" + this.seenUsers.size());
        }
        this.titleView.setText(str);
        TLRPC$TL_messageReactions tLRPC$TL_messageReactions = this.message.messageOwner.reactions;
        if (tLRPC$TL_messageReactions != null && tLRPC$TL_messageReactions.results.size() == 1 && !tLRPC$TL_messages_messageReactionsList.reactions.isEmpty()) {
            for (TLRPC$TL_availableReaction tLRPC$TL_availableReaction : MediaDataController.getInstance(this.currentAccount).getReactionsList()) {
                if (tLRPC$TL_availableReaction.reaction.equals(tLRPC$TL_messages_messageReactionsList.reactions.get(0).reaction)) {
                    this.reactView.setImage(ImageLocation.getForDocument(tLRPC$TL_availableReaction.center_icon), "40_40_lastframe", "webp", (Drawable) null, tLRPC$TL_availableReaction);
                    this.reactView.setVisibility(0);
                    this.reactView.setAlpha(0.0f);
                    this.reactView.animate().alpha(1.0f).start();
                    this.iconView.setVisibility(8);
                    z = false;
                    break;
                }
            }
        }
        z = true;
        if (z) {
            this.iconView.setVisibility(0);
            this.iconView.setAlpha(0.0f);
            this.iconView.animate().alpha(1.0f).start();
        }
        Iterator<TLRPC$User> it = tLRPC$TL_messages_messageReactionsList.users.iterator();
        while (it.hasNext()) {
            TLRPC$User next = it.next();
            TLRPC$Peer tLRPC$Peer = this.message.messageOwner.from_id;
            if (!(tLRPC$Peer == null || next.id == tLRPC$Peer.user_id)) {
                int i2 = 0;
                while (true) {
                    if (i2 >= this.users.size()) {
                        z2 = false;
                        break;
                    } else if (this.users.get(i2).id == next.id) {
                        z2 = true;
                        break;
                    } else {
                        i2++;
                    }
                }
                if (!z2) {
                    this.users.add(next);
                }
            }
        }
        updateView();
    }

    public List<TLRPC$User> getSeenUsers() {
        return this.seenUsers;
    }

    private void updateView() {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.ReactedHeaderView.updateView():void");
    }

    @Override
    public void requestLayout() {
        if (!this.ignoreLayout) {
            super.requestLayout();
        }
    }

    @Override
    protected void onMeasure(int i, int i2) {
        if (this.flickerLoadingView.getVisibility() == 0) {
            this.ignoreLayout = true;
            this.flickerLoadingView.setVisibility(8);
            super.onMeasure(i, i2);
            this.flickerLoadingView.getLayoutParams().width = getMeasuredWidth();
            this.flickerLoadingView.setVisibility(0);
            this.ignoreLayout = false;
            super.onMeasure(i, i2);
            return;
        }
        super.onMeasure(i, i2);
    }
}
