package org.telegram.tgnet;

public class TLRPC$TL_messages_readEncryptedHistory extends TLObject {
    public static int constructor = 2135648522;
    public int max_date;
    public TLRPC$TL_inputEncryptedChat peer;

    @Override
    public TLObject deserializeResponse(AbstractSerializedData abstractSerializedData, int i, boolean z) {
        return TLRPC$Bool.TLdeserialize(abstractSerializedData, i, z);
    }

    @Override
    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
        this.peer.serializeToStream(abstractSerializedData);
        abstractSerializedData.writeInt32(this.max_date);
    }
}
