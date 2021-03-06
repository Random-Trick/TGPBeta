package org.telegram.messenger;

import android.os.Handler;
import android.os.Looper;
import org.telegram.messenger.NotificationCenter;

public final class ContactsLoadingObserver {
    private final Callback callback;
    private final ContactsController contactsController;
    private final int currentAccount;
    private final NotificationCenter notificationCenter;
    private boolean released;
    private final NotificationCenter.NotificationCenterDelegate observer = new NotificationCenter.NotificationCenterDelegate() {
        @Override
        public final void didReceivedNotification(int i, int i2, Object[] objArr) {
            ContactsLoadingObserver.this.lambda$new$0(i, i2, objArr);
        }
    };
    private final Runnable releaseRunnable = new Runnable() {
        @Override
        public final void run() {
            ContactsLoadingObserver.this.lambda$new$1();
        }
    };
    private final Handler handler = new Handler(Looper.myLooper());

    public interface Callback {
        void onResult(boolean z);
    }

    public static void observe(Callback callback, long j) {
        new ContactsLoadingObserver(callback).start(j);
    }

    public void lambda$new$0(int i, int i2, Object[] objArr) {
        if (i == NotificationCenter.contactsDidLoad) {
            onContactsLoadingStateUpdated(i2, false);
        }
    }

    private ContactsLoadingObserver(Callback callback) {
        this.callback = callback;
        int i = UserConfig.selectedAccount;
        this.currentAccount = i;
        this.contactsController = ContactsController.getInstance(i);
        this.notificationCenter = NotificationCenter.getInstance(i);
    }

    public void lambda$new$1() {
        onContactsLoadingStateUpdated(this.currentAccount, true);
    }

    public void start(long j) {
        if (!onContactsLoadingStateUpdated(this.currentAccount, false)) {
            this.notificationCenter.addObserver(this.observer, NotificationCenter.contactsDidLoad);
            this.handler.postDelayed(this.releaseRunnable, j);
        }
    }

    public void release() {
        if (!this.released) {
            NotificationCenter notificationCenter = this.notificationCenter;
            if (notificationCenter != null) {
                notificationCenter.removeObserver(this.observer, NotificationCenter.contactsDidLoad);
            }
            Handler handler = this.handler;
            if (handler != null) {
                handler.removeCallbacks(this.releaseRunnable);
            }
            this.released = true;
        }
    }

    private boolean onContactsLoadingStateUpdated(int i, boolean z) {
        if (this.released) {
            return false;
        }
        boolean z2 = this.contactsController.contactsLoaded;
        if (!z2 && !z) {
            return false;
        }
        release();
        this.callback.onResult(z2);
        return true;
    }
}
