package org.telegram.tgnet;

public class TLRPC$TL_inputPrivacyKeyAddedByPhone extends TLRPC$InputPrivacyKey {
    public static int constructor = -786326563;

    @Override
    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
    }
}
