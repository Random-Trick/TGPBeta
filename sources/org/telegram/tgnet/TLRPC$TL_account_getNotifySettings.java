package org.telegram.tgnet;

public class TLRPC$TL_account_getNotifySettings extends TLObject {
    public static int constructor = 313765169;
    public TLRPC$InputNotifyPeer peer;

    @Override
    public TLObject deserializeResponse(AbstractSerializedData abstractSerializedData, int i, boolean z) {
        return TLRPC$PeerNotifySettings.TLdeserialize(abstractSerializedData, i, z);
    }

    @Override
    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
        this.peer.serializeToStream(abstractSerializedData);
    }
}
