package org.telegram.messenger;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;

public final class MessagesController$$ExternalSyntheticLambda347 implements RequestDelegate {
    public static final MessagesController$$ExternalSyntheticLambda347 INSTANCE = new MessagesController$$ExternalSyntheticLambda347();

    private MessagesController$$ExternalSyntheticLambda347() {
    }

    @Override
    public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        MessagesController.lambda$blockPeer$69(tLObject, tLRPC$TL_error);
    }
}
