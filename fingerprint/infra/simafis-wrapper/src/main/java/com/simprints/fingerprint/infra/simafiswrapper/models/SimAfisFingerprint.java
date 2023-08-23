package com.simprints.fingerprint.infra.simafiswrapper.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Represents a fingerprint in the Simprints Afis system.
 * A fingerprint is identified by a finger identifier and has a quality score.
 */
public class SimAfisFingerprint implements Parcelable {

    private static final int ISO_FORMAT_ID = Integer.parseInt("464D5200", 16);     // 'F' 'M' 'R' 00hex
    private static final int ISO_2005_VERSION = Integer.parseInt("20323000", 16);  // ' ' '2' '0' 00hex

    private static final int FORMAT_ID = 0;              // INT
    private static final int VERSION = 4;                // INT
    private static final int RECORD_LENGTH = 8;          // INT
    private static final int NB_FINGERPRINTS = 22;       // BYTE
    private static final int FIRST_QUALITY = 26;         // BYTE

    private final SimAfisFingerIdentifier fingerId;
    private final ByteBuffer template;

    /**
     * ISO 2005 byte array constructor
     *
     * @param fingerId         Finger identifier of the fingerprint
     * @param isoTemplateBytes Byte array containing an ISO 2005 fingerprint template
     * @throws IllegalArgumentException If the bytes array specified is not a valid ISO 2005
     *                                  (2011 not supported yet) template containing only 1 fingerprint.
     */
    public SimAfisFingerprint(@NonNull SimAfisFingerIdentifier fingerId, @NonNull byte[] isoTemplateBytes)
            throws IllegalArgumentException {
        this.fingerId = fingerId;
        this.template = ByteBuffer.allocateDirect(isoTemplateBytes.length);
        this.template.put(isoTemplateBytes);
        template.order(ByteOrder.BIG_ENDIAN);

        try {
            // Checks the format identifier
            if (this.template.getInt(FORMAT_ID) != ISO_FORMAT_ID) {
                throw new IllegalArgumentException("Invalid template: not an ISO template");
            }

            // Checks the ISO version
            if (this.template.getInt(VERSION) != ISO_2005_VERSION) {
                throw new IllegalArgumentException("Invalid template: only ISO 2005 is supported");
            }

            // Checks the length of the record
            if (this.template.getInt(RECORD_LENGTH) != isoTemplateBytes.length) {
                throw new IllegalArgumentException("Invalid template: invalid length");
            }

            // Checks the number of fingers
            if (this.template.get(NB_FINGERPRINTS) != 1) {
                throw new IllegalArgumentException("Invalid template: only single fingerprint templates are supported");
            }
        } catch (IndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("Invalid template: Processing byte[] failed");
        }
    }
    /**
     * @return A reference to the direct ByteBuffer containing the ISO 2005 template of
     * the fingerprint
     */
    public ByteBuffer getTemplateDirectBuffer() {
        template.position(0);
        return template;
    }

    public SimAfisFingerIdentifier getFingerId() {
        return fingerId;
    }

    /**
     * @return A newly allocated byte array containing the ISO 2005 template of
     * the fingerprint
     */
    public byte[] getTemplateBytes() {
        template.position(0);
        byte[] templateBytes = new byte[template.remaining()];
        template.get(templateBytes);
        return templateBytes;
    }


    /**
     * @return The quality score of this fingerprint, as stored in its template
     */
    public int getQualityScore() {
        return template.get(FIRST_QUALITY);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimAfisFingerprint that = (SimAfisFingerprint) o;

        return (fingerId.equals(that.fingerId) &&
                Arrays.equals(this.getTemplateBytes(), that.getTemplateBytes()));
    }

    @Override
    public int hashCode() {
        int result = fingerId.hashCode();
        result = 31 * result + Arrays.hashCode(this.getTemplateBytes());
        return result;
    }

    protected SimAfisFingerprint(Parcel in) {
        fingerId = SimAfisFingerIdentifier.values()[in.readInt()];
        byte[] temp = new byte[in.readInt()];
        in.readByteArray(temp);
        template = ByteBuffer.allocateDirect(temp.length);
        template.put(temp);
    }

    public static final Creator<SimAfisFingerprint> CREATOR = new Creator() {
        @Override
        public SimAfisFingerprint createFromParcel(Parcel in) {
            return new SimAfisFingerprint(in);
        }

        @Override
        public SimAfisFingerprint[] newArray(int size) {
            return new SimAfisFingerprint[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(fingerId.ordinal());
        byte[] bytes = this.getTemplateBytes();
        dest.writeInt(bytes.length);
        dest.writeByteArray(bytes);
    }

}
