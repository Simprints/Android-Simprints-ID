package com.simprints.libcommon;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;

public class Template {
    private byte[] bytes;

    private int formatId;
    private int version;
    private int recordLength;
    private short unknown1;
    private short width;
    private short height;
    private short horizontalSamplingRate;
    private short verticalSamplingRate;
    private byte nbFingers;
    private byte unknown2;
    private byte fingerPosition;
    private byte unknown3;
    private byte quality;

    private List<Minutia> minutiae;

    private static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    public Template(@NonNull byte[] templateBytes) {
        try {
            bytes = templateBytes;
            ByteBuffer bb = ByteBuffer.wrap(templateBytes);
            bb.order(ByteOrder.BIG_ENDIAN);
            formatId = bb.getInt();
            version = bb.getInt();
            recordLength = bb.getInt();
            if (recordLength != templateBytes.length) {
                Log.d("Simprints", String.format(
                        "Incoherent record length %d when there are %d template bytes",
                        recordLength, templateBytes.length));
            }
            unknown1 = bb.getShort();
            width = bb.getShort();
            height = bb.getShort();
            horizontalSamplingRate = bb.getShort();
            verticalSamplingRate = bb.getShort();
            nbFingers = bb.get();
            unknown2 = bb.get();
            fingerPosition = bb.get();
            unknown3 = bb.get();
            quality = bb.get();
            byte nbOfMinutae = bb.get();
            minutiae = new ArrayList<>(nbOfMinutae);
            for (int i = 0; i < nbOfMinutae; i++) {
                short b1 = bb.getShort();
                short b2 = bb.getShort();
                short x = (short) (b1 & 0x3fff);
                short y = (short) (b2 & 0x3fff);
                byte type = (byte) ((b1 & 0xc000) >> 14);
                byte direction = bb.get();
                byte quality = bb.get();
                minutiae.add(new Minutia(x, y, type, direction, quality));
            }
        } catch (BufferUnderflowException | BufferOverflowException e) {
            throw new IllegalArgumentException("Invalid bytes caused buffer under/over flow");
        }
    }

    public boolean isValidISO() {
        byte[] fBytes = intToByteArray(formatId);
//        byte[] vBytes = intToByteArray(version);
        return (fBytes[0] == 'F' && fBytes[1] == 'M' && fBytes[2] == 'R' && fBytes[3] == 0 &&
//                vBytes [0] == '0' && vBytes[1] == '3' && vBytes[2] == '0' && vBytes[3] == 0 &&
                recordLength == bytes.length &&
                nbFingers == 1);
    }

    public int getFormatId() {
        return formatId;
    }

    public int getVersion() {
        return version;
    }

    public int getRecordLength() {
        return recordLength;
    }

    public short getUnknown1() {
        return unknown1;
    }

    public short getWidth() {
        return width;
    }

    public short getHeight() {
        return height;
    }

    public short getHorizontalSamplingRate() {
        return horizontalSamplingRate;
    }

    public short getVerticalSamplingRate() {
        return verticalSamplingRate;
    }

    public byte getNbFingers() {
        return nbFingers;
    }

    public byte getUnknown2() {
        return unknown2;
    }

    public byte getFingerPosition() {
        return fingerPosition;
    }

    public byte getUnknown3() {
        return unknown3;
    }

    public byte getQuality() {
        return quality;
    }

    public List<Minutia> getMinutiae() {
        return minutiae;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        byte[] formatBytes = intToByteArray(formatId);
        stringBuilder.append(String.format(Locale.UK, "format id: %c %c %c %d\n",
                formatBytes[0], formatBytes[1], formatBytes[2], formatBytes[3]));
        byte[] versionBytes = intToByteArray(version);
        stringBuilder.append(String.format(Locale.UK, "version: %c %c %c %d\n",
                versionBytes[0], versionBytes[1], versionBytes[2], versionBytes[3]));
        stringBuilder.append(String.format(Locale.UK, "record length: %d bytes\n", recordLength));
        stringBuilder.append(String.format(Locale.UK, "unknown1: %d\n", unknown1));
        stringBuilder.append(String.format(Locale.UK, "width: %d pixels\n", width));
        stringBuilder.append(String.format(Locale.UK, "height: %d pixels\n", height));
        stringBuilder.append(String.format(Locale.UK, "horizontal sampling rate: %d pixels per cm\n", horizontalSamplingRate));
        stringBuilder.append(String.format(Locale.UK, "vertical sampling rate: %d pixels per cm\n", verticalSamplingRate));
        stringBuilder.append(String.format(Locale.UK, "number of fingers: %d\n", nbFingers));
        stringBuilder.append(String.format(Locale.UK, "unknown2: %d\n", unknown2));
        stringBuilder.append(String.format(Locale.UK, "finger position: %d\n", fingerPosition));
        stringBuilder.append(String.format(Locale.UK, "unknown3: %d\n", unknown3));
        stringBuilder.append(String.format(Locale.UK, "quality: %d\n", quality));
        stringBuilder.append(String.format(Locale.UK, "number of minutia(e): %d\n", minutiae.size()));
        int i = 1;
        for (Minutia m : minutiae) {
            stringBuilder.append(String.format(Locale.UK, "Minutia %d - %s\n", i, m.toString()));
            i++;
        }
        return stringBuilder.toString();
    }

    private Template(Parcel in) {
        bytes = new byte[in.readInt()];
        in.readByteArray(bytes);
        formatId = in.readInt();
        version = in.readInt();
        recordLength = in.readInt();
        unknown1 = (short)in.readInt();
        width = (short)in.readInt();
        height = (short)in.readInt();
        horizontalSamplingRate = (short)in.readInt();
        verticalSamplingRate = (short)in.readInt();
        nbFingers = in.readByte();
        unknown2 = in.readByte();
        fingerPosition = in.readByte();
        unknown3 = in.readByte();
        quality = in.readByte();
        minutiae = new ArrayList<>();
        in.readTypedList(minutiae, Minutia.CREATOR);
    }

    public static final Parcelable.Creator<Template> CREATOR = new Parcelable.Creator<Template>() {
        @Override
        public Template createFromParcel(Parcel in) {
            return new Template(in);
        }

        @Override
        public Template[] newArray(int size) {
            return new Template[size];
        }
    };
}
