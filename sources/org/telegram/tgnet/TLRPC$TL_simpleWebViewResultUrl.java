package org.telegram.tgnet;

public class TLRPC$TL_simpleWebViewResultUrl extends TLObject {
    public static int constructor = -2010155333;
    public String url;

    public static TLRPC$TL_simpleWebViewResultUrl TLdeserialize(AbstractSerializedData abstractSerializedData, int i, boolean z) {
        if (constructor == i) {
            TLRPC$TL_simpleWebViewResultUrl tLRPC$TL_simpleWebViewResultUrl = new TLRPC$TL_simpleWebViewResultUrl();
            tLRPC$TL_simpleWebViewResultUrl.readParams(abstractSerializedData, z);
            return tLRPC$TL_simpleWebViewResultUrl;
        } else if (!z) {
            return null;
        } else {
            throw new RuntimeException(String.format("can't parse magic %x in TL_simpleWebViewResultUrl", Integer.valueOf(i)));
        }
    }

    @Override
    public void readParams(AbstractSerializedData abstractSerializedData, boolean z) {
        this.url = abstractSerializedData.readString(z);
    }

    @Override
    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
        abstractSerializedData.writeString(this.url);
    }
}
