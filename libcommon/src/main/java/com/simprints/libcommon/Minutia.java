package com.simprints.libcommon;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

public class Minutia {
    private short x;
    private short y;
    private byte type;
    private byte direction;
    private byte quality;

    public Minutia(short x, short y, byte type, byte direction, byte quality) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.direction = direction;
        this.quality = quality;
    }

    public short getX() {
        return x;
    }

    public short getY() {
        return y;
    }

    public byte getType() {
        return type;
    }

    public byte getDirection() {
        return direction;
    }

    public byte getQuality() {
        return quality;
    }

    @Override
    public String toString() {
        return String.format(Locale.UK, "X: %d, Y: %d, type: %d, direction: %d, quality: %d",
                x, y, type, direction, quality);
    }

    protected Minutia(Parcel in) {
        x = (short) in.readInt();
        y = (short) in.readInt();
        type = in.readByte();
        direction = in.readByte();
        quality = in.readByte();
    }

    public static final Parcelable.Creator<Minutia> CREATOR = new Parcelable.Creator<Minutia>() {
        @Override
        public Minutia createFromParcel(Parcel in) {
            return new Minutia(in);
        }

        @Override
        public Minutia[] newArray(int size) {
            return new Minutia[size];
        }
    };

}
