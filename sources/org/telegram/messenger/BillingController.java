package org.telegram.messenger;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.util.Consumer;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_payments_assignPlayMarketTransaction;
import org.telegram.tgnet.TLRPC$Updates;

public class BillingController implements PurchasesUpdatedListener, BillingClientStateListener {
    public static ProductDetails PREMIUM_PRODUCT_DETAILS = null;
    private static BillingController instance;
    private BillingClient billingClient;
    public static final String PREMIUM_PRODUCT_ID = "telegram_premium";
    public static final QueryProductDetailsParams.Product PREMIUM_PRODUCT = QueryProductDetailsParams.Product.newBuilder().setProductType("subs").setProductId(PREMIUM_PRODUCT_ID).build();
    private Map<String, Consumer<BillingResult>> resultListeners = new HashMap();
    private List<String> requestingTokens = new ArrayList();

    public static BillingController getInstance() {
        if (instance == null) {
            instance = new BillingController(ApplicationLoader.applicationContext);
        }
        return instance;
    }

    private BillingController(Context context) {
        this.billingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener(this).build();
    }

    public void startConnection() {
        if (!isReady()) {
            this.billingClient.startConnection(this);
        }
    }

    public boolean isReady() {
        return this.billingClient.isReady();
    }

    public void queryProductDetails(List<QueryProductDetailsParams.Product> list, ProductDetailsResponseListener productDetailsResponseListener) {
        if (isReady()) {
            this.billingClient.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(list).build(), productDetailsResponseListener);
            return;
        }
        throw new IllegalStateException("Billing controller should be ready for this call!");
    }

    public void queryPurchases(String str, PurchasesResponseListener purchasesResponseListener) {
        this.billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(str).build(), purchasesResponseListener);
    }

    public boolean startManageSubscription(Context context, String str) {
        try {
            context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(String.format("https://play.google.com/store/account/subscriptions?sku=%s&package=%s", str, context.getPackageName()))));
            return true;
        } catch (ActivityNotFoundException unused) {
            return false;
        }
    }

    public void addResultListener(String str, Consumer<BillingResult> consumer) {
        this.resultListeners.put(str, consumer);
    }

    public boolean launchBillingFlow(Activity activity, List<BillingFlowParams.ProductDetailsParams> list) {
        return isReady() && this.billingClient.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(list).build()).getResponseCode() == 0;
    }

    @Override
    public void onPurchasesUpdated(final BillingResult billingResult, List<Purchase> list) {
        FileLog.d("Billing purchases updated: " + billingResult + ", " + list);
        if (list != null) {
            for (final Purchase purchase : list) {
                if (purchase.getPurchaseState() == 1 && !purchase.isAcknowledged() && !this.requestingTokens.contains(purchase.getPurchaseToken())) {
                    this.requestingTokens.add(purchase.getPurchaseToken());
                    TLRPC$TL_payments_assignPlayMarketTransaction tLRPC$TL_payments_assignPlayMarketTransaction = new TLRPC$TL_payments_assignPlayMarketTransaction();
                    tLRPC$TL_payments_assignPlayMarketTransaction.purchase_token = purchase.getPurchaseToken();
                    final AccountInstance accountInstance = AccountInstance.getInstance(UserConfig.selectedAccount);
                    accountInstance.getConnectionsManager().sendRequest(tLRPC$TL_payments_assignPlayMarketTransaction, new RequestDelegate() {
                        @Override
                        public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                            BillingController.this.lambda$onPurchasesUpdated$0(accountInstance, purchase, billingResult, tLObject, tLRPC$TL_error);
                        }
                    });
                }
            }
        }
    }

    public void lambda$onPurchasesUpdated$0(AccountInstance accountInstance, Purchase purchase, BillingResult billingResult, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        if (tLObject instanceof TLRPC$Updates) {
            accountInstance.getMessagesController().processUpdates((TLRPC$Updates) tLObject, false);
            this.requestingTokens.remove(purchase.getPurchaseToken());
            for (String str : purchase.getProducts()) {
                this.resultListeners.remove(str).accept(billingResult);
            }
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        FileLog.d("Billing service disconnected");
    }

    @Override
    public void onBillingSetupFinished(BillingResult billingResult) {
        if (billingResult.getResponseCode() == 0) {
            queryProductDetails(Collections.singletonList(PREMIUM_PRODUCT), BillingController$$ExternalSyntheticLambda0.INSTANCE);
            queryPurchases("subs", new PurchasesResponseListener() {
                @Override
                public final void onQueryPurchasesResponse(BillingResult billingResult2, List list) {
                    BillingController.this.onPurchasesUpdated(billingResult2, list);
                }
            });
        }
    }

    public static void lambda$onBillingSetupFinished$2(BillingResult billingResult, List list) {
        if (billingResult.getResponseCode() == 0) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                ProductDetails productDetails = (ProductDetails) it.next();
                if (productDetails.getProductId().equals(PREMIUM_PRODUCT_ID)) {
                    PREMIUM_PRODUCT_DETAILS = productDetails;
                }
            }
            AndroidUtilities.runOnUIThread(BillingController$$ExternalSyntheticLambda2.INSTANCE);
        }
    }

    public static void lambda$onBillingSetupFinished$1() {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.billingProductDetailsUpdated, new Object[0]);
    }
}
