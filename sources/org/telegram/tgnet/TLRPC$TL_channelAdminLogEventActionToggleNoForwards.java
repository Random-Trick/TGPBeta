package org.telegram.tgnet;

public class TLRPC$TL_channelAdminLogEventActionToggleNoForwards extends TLRPC$ChannelAdminLogEventAction {
    public static int constructor = -886388890;
    public boolean new_value;

    @Override
    public void readParams(AbstractSerializedData abstractSerializedData, boolean z) {
        this.new_value = abstractSerializedData.readBool(z);
    }

    @Override
    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
        abstractSerializedData.writeBool(this.new_value);
    }
}
