package org.telegram.tgnet;

import java.util.ArrayList;

public class TLRPC$TL_account_authorizationForm extends TLObject {
    public static int constructor = -1389486888;
    public int flags;
    public String privacy_policy_url;
    public ArrayList<TLRPC$SecureRequiredType> required_types = new ArrayList<>();
    public ArrayList<TLRPC$TL_secureValue> values = new ArrayList<>();
    public ArrayList<TLRPC$SecureValueError> errors = new ArrayList<>();
    public ArrayList<TLRPC$User> users = new ArrayList<>();

    public static TLRPC$TL_account_authorizationForm TLdeserialize(AbstractSerializedData abstractSerializedData, int i, boolean z) {
        if (constructor == i) {
            TLRPC$TL_account_authorizationForm tLRPC$TL_account_authorizationForm = new TLRPC$TL_account_authorizationForm();
            tLRPC$TL_account_authorizationForm.readParams(abstractSerializedData, z);
            return tLRPC$TL_account_authorizationForm;
        } else if (!z) {
            return null;
        } else {
            throw new RuntimeException(String.format("can't parse magic %x in TL_account_authorizationForm", Integer.valueOf(i)));
        }
    }

    @Override
    public void readParams(AbstractSerializedData abstractSerializedData, boolean z) {
        this.flags = abstractSerializedData.readInt32(z);
        int readInt32 = abstractSerializedData.readInt32(z);
        if (readInt32 == 481674261) {
            int readInt322 = abstractSerializedData.readInt32(z);
            for (int i = 0; i < readInt322; i++) {
                TLRPC$SecureRequiredType TLdeserialize = TLRPC$SecureRequiredType.TLdeserialize(abstractSerializedData, abstractSerializedData.readInt32(z), z);
                if (TLdeserialize != null) {
                    this.required_types.add(TLdeserialize);
                } else {
                    return;
                }
            }
            int readInt323 = abstractSerializedData.readInt32(z);
            if (readInt323 == 481674261) {
                int readInt324 = abstractSerializedData.readInt32(z);
                for (int i2 = 0; i2 < readInt324; i2++) {
                    TLRPC$TL_secureValue TLdeserialize2 = TLRPC$TL_secureValue.TLdeserialize(abstractSerializedData, abstractSerializedData.readInt32(z), z);
                    if (TLdeserialize2 != null) {
                        this.values.add(TLdeserialize2);
                    } else {
                        return;
                    }
                }
                int readInt325 = abstractSerializedData.readInt32(z);
                if (readInt325 == 481674261) {
                    int readInt326 = abstractSerializedData.readInt32(z);
                    for (int i3 = 0; i3 < readInt326; i3++) {
                        TLRPC$SecureValueError TLdeserialize3 = TLRPC$SecureValueError.TLdeserialize(abstractSerializedData, abstractSerializedData.readInt32(z), z);
                        if (TLdeserialize3 != null) {
                            this.errors.add(TLdeserialize3);
                        } else {
                            return;
                        }
                    }
                    int readInt327 = abstractSerializedData.readInt32(z);
                    if (readInt327 == 481674261) {
                        int readInt328 = abstractSerializedData.readInt32(z);
                        for (int i4 = 0; i4 < readInt328; i4++) {
                            TLRPC$User TLdeserialize4 = TLRPC$User.TLdeserialize(abstractSerializedData, abstractSerializedData.readInt32(z), z);
                            if (TLdeserialize4 != null) {
                                this.users.add(TLdeserialize4);
                            } else {
                                return;
                            }
                        }
                        if ((this.flags & 1) != 0) {
                            this.privacy_policy_url = abstractSerializedData.readString(z);
                        }
                    } else if (z) {
                        throw new RuntimeException(String.format("wrong Vector magic, got %x", Integer.valueOf(readInt327)));
                    }
                } else if (z) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", Integer.valueOf(readInt325)));
                }
            } else if (z) {
                throw new RuntimeException(String.format("wrong Vector magic, got %x", Integer.valueOf(readInt323)));
            }
        } else if (z) {
            throw new RuntimeException(String.format("wrong Vector magic, got %x", Integer.valueOf(readInt32)));
        }
    }

    @Override
    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
        abstractSerializedData.writeInt32(this.flags);
        abstractSerializedData.writeInt32(481674261);
        int size = this.required_types.size();
        abstractSerializedData.writeInt32(size);
        for (int i = 0; i < size; i++) {
            this.required_types.get(i).serializeToStream(abstractSerializedData);
        }
        abstractSerializedData.writeInt32(481674261);
        int size2 = this.values.size();
        abstractSerializedData.writeInt32(size2);
        for (int i2 = 0; i2 < size2; i2++) {
            this.values.get(i2).serializeToStream(abstractSerializedData);
        }
        abstractSerializedData.writeInt32(481674261);
        int size3 = this.errors.size();
        abstractSerializedData.writeInt32(size3);
        for (int i3 = 0; i3 < size3; i3++) {
            this.errors.get(i3).serializeToStream(abstractSerializedData);
        }
        abstractSerializedData.writeInt32(481674261);
        int size4 = this.users.size();
        abstractSerializedData.writeInt32(size4);
        for (int i4 = 0; i4 < size4; i4++) {
            this.users.get(i4).serializeToStream(abstractSerializedData);
        }
        if ((this.flags & 1) != 0) {
            abstractSerializedData.writeString(this.privacy_policy_url);
        }
    }
}
