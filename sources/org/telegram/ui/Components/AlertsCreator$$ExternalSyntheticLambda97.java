package org.telegram.ui.Components;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;

public final class AlertsCreator$$ExternalSyntheticLambda97 implements RequestDelegate {
    public static final AlertsCreator$$ExternalSyntheticLambda97 INSTANCE = new AlertsCreator$$ExternalSyntheticLambda97();

    private AlertsCreator$$ExternalSyntheticLambda97() {
    }

    @Override
    public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AlertsCreator.lambda$sendReport$83(tLObject, tLRPC$TL_error);
    }
}
