package org.telegram.tgnet;

public class TLRPC$TL_photoCachedSize extends TLRPC$PhotoSize {
    public static int constructor = 35527382;

    @Override
    public void readParams(AbstractSerializedData abstractSerializedData, boolean z) {
        this.type = abstractSerializedData.readString(z);
        this.w = abstractSerializedData.readInt32(z);
        this.h = abstractSerializedData.readInt32(z);
        this.bytes = abstractSerializedData.readByteArray(z);
    }

    @Override
    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
        abstractSerializedData.writeString(this.type);
        abstractSerializedData.writeInt32(this.w);
        abstractSerializedData.writeInt32(this.h);
        abstractSerializedData.writeByteArray(this.bytes);
    }
}
