package org.telegram.messenger;

import java.util.Comparator;
import org.telegram.tgnet.TLRPC$TL_topPeer;

public final class MediaDataController$$ExternalSyntheticLambda128 implements Comparator {
    public static final MediaDataController$$ExternalSyntheticLambda128 INSTANCE = new MediaDataController$$ExternalSyntheticLambda128();

    private MediaDataController$$ExternalSyntheticLambda128() {
    }

    @Override
    public final int compare(Object obj, Object obj2) {
        int lambda$increaseInlineRaiting$112;
        lambda$increaseInlineRaiting$112 = MediaDataController.lambda$increaseInlineRaiting$112((TLRPC$TL_topPeer) obj, (TLRPC$TL_topPeer) obj2);
        return lambda$increaseInlineRaiting$112;
    }
}
