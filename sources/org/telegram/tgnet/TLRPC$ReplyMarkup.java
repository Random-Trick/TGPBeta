package org.telegram.tgnet;

import java.util.ArrayList;

public abstract class TLRPC$ReplyMarkup extends TLObject {
    public int flags;
    public String placeholder;
    public boolean resize;
    public ArrayList<TLRPC$TL_keyboardButtonRow> rows = new ArrayList<>();
    public boolean selective;
    public boolean single_use;

    public static TLRPC$ReplyMarkup TLdeserialize(AbstractSerializedData abstractSerializedData, int i, boolean z) {
        TLRPC$ReplyMarkup tLRPC$ReplyMarkup;
        switch (i) {
            case -2049074735:
                tLRPC$ReplyMarkup = new TLRPC$TL_replyKeyboardMarkup();
                break;
            case -2035021048:
                tLRPC$ReplyMarkup = new TLRPC$TL_replyKeyboardForceReply();
                break;
            case -1606526075:
                tLRPC$ReplyMarkup = new TLRPC$TL_replyKeyboardHide();
                break;
            case -200242528:
                tLRPC$ReplyMarkup = new TLRPC$TL_replyKeyboardForceReply() {
                    public static int constructor = -200242528;

                    @Override
                    public void readParams(AbstractSerializedData abstractSerializedData2, boolean z2) {
                        int readInt32 = abstractSerializedData2.readInt32(z2);
                        this.flags = readInt32;
                        boolean z3 = true;
                        this.single_use = (readInt32 & 2) != 0;
                        if ((readInt32 & 4) == 0) {
                            z3 = false;
                        }
                        this.selective = z3;
                    }

                    @Override
                    public void serializeToStream(AbstractSerializedData abstractSerializedData2) {
                        abstractSerializedData2.writeInt32(constructor);
                        int i2 = this.single_use ? this.flags | 2 : this.flags & (-3);
                        this.flags = i2;
                        int i3 = this.selective ? i2 | 4 : i2 & (-5);
                        this.flags = i3;
                        abstractSerializedData2.writeInt32(i3);
                    }
                };
                break;
            case 889353612:
                tLRPC$ReplyMarkup = new TLRPC$TL_replyKeyboardMarkup() {
                    public static int constructor = 889353612;

                    @Override
                    public void readParams(AbstractSerializedData abstractSerializedData2, boolean z2) {
                        int readInt32 = abstractSerializedData2.readInt32(z2);
                        this.flags = readInt32;
                        this.resize = (readInt32 & 1) != 0;
                        this.single_use = (readInt32 & 2) != 0;
                        this.selective = (readInt32 & 4) != 0;
                        int readInt322 = abstractSerializedData2.readInt32(z2);
                        if (readInt322 == 481674261) {
                            int readInt323 = abstractSerializedData2.readInt32(z2);
                            for (int i2 = 0; i2 < readInt323; i2++) {
                                TLRPC$TL_keyboardButtonRow TLdeserialize = TLRPC$TL_keyboardButtonRow.TLdeserialize(abstractSerializedData2, abstractSerializedData2.readInt32(z2), z2);
                                if (TLdeserialize != null) {
                                    this.rows.add(TLdeserialize);
                                } else {
                                    return;
                                }
                            }
                        } else if (z2) {
                            throw new RuntimeException(String.format("wrong Vector magic, got %x", Integer.valueOf(readInt322)));
                        }
                    }

                    @Override
                    public void serializeToStream(AbstractSerializedData abstractSerializedData2) {
                        abstractSerializedData2.writeInt32(constructor);
                        int i2 = this.resize ? this.flags | 1 : this.flags & (-2);
                        this.flags = i2;
                        int i3 = this.single_use ? i2 | 2 : i2 & (-3);
                        this.flags = i3;
                        int i4 = this.selective ? i3 | 4 : i3 & (-5);
                        this.flags = i4;
                        abstractSerializedData2.writeInt32(i4);
                        abstractSerializedData2.writeInt32(481674261);
                        int size = this.rows.size();
                        abstractSerializedData2.writeInt32(size);
                        for (int i5 = 0; i5 < size; i5++) {
                            this.rows.get(i5).serializeToStream(abstractSerializedData2);
                        }
                    }
                };
                break;
            case 1218642516:
                tLRPC$ReplyMarkup = new TLRPC$TL_replyInlineMarkup();
                break;
            default:
                tLRPC$ReplyMarkup = null;
                break;
        }
        if (tLRPC$ReplyMarkup != null || !z) {
            if (tLRPC$ReplyMarkup != null) {
                tLRPC$ReplyMarkup.readParams(abstractSerializedData, z);
            }
            return tLRPC$ReplyMarkup;
        }
        throw new RuntimeException(String.format("can't parse magic %x in ReplyMarkup", Integer.valueOf(i)));
    }
}
