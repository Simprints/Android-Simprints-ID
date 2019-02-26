package com.simprints.id.domain.fingerprint;

import android.os.Parcel;
import android.os.Parcelable;

import com.simprints.id.FingerIdentifier;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public class Fingerprint implements Parcelable {

    private final static int ISO_FORMAT_ID = Integer.parseInt("464D5200", 16);     // 'F' 'M' 'R' 00hex
    private final static int ISO_2005_VERSION = Integer.parseInt("20323000", 16);  // ' ' '2' '0' 00hex
    private final static short SECUGEN_HAMSTER_WIDTH = 300;
    private final static short SECUGEN_HAMSTER_HEIGHT = 400;
    private final static short SECUGEN_HAMSTER_PPCM = 500;


    private final static int HEADER_SIZE = 28;
    private final static int MINUTIAE_SIZE = 6;

    private final static int FORMAT_ID = 0;              // INT
    private final static int VERSION = 4;                // INT
    private final static int RECORD_LENGTH = 8;          // INT
    private final static int WIDTH = 14;                 // SHORT
    private final static int HEIGHT = 16;                // SHORT
    private final static int HORIZONTAL_PPCM = 18;       // SHORT
    private final static int VERTICAL_PPCM = 20;         // SHORT
    private final static int NB_FINGERPRINTS = 22;       // BYTE
    private final static int FIRST_FINGER_POSITION = 24; // BYTE
    private final static int FIRST_QUALITY = 26;         // BYTE
    private final static int FIRST_NB_MINUTIAE = 27;     // BYTE
    private final static int FIRST_MINUTIAE_START = 28;

    private final static int TYPE_AND_X_SHIFT = 0;       // SHORT
    private final static int ZEROS_AND_Y_SHIFT = 2;      // SHORT
    private final static int ANGLE_SHIFT = 4;            // SHORT
    private final static int QUALITY_SHIFT = 5;          // SHORT

    private final static Random RANDOM_GENERATOR = new Random();
    private final static FingerIdentifier[] FINGER_IDENTIFIERS = FingerIdentifier.values();


    private final FingerIdentifier fingerId;
    private final ByteBuffer template;

    /**
     * ISO 2005 byte array constructor
     *
     * @param fingerId         Finger identifier of the fingerprint
     * @param isoTemplateBytes Byte array containing an ISO 2005 fingerprint template
     * @throws IllegalArgumentException If the bytes array specified is not a valid ISO 2005
     *                                  (2011 not supported yet) template containing only 1 fingerprint.
     */
    public Fingerprint(@NonNull FingerIdentifier fingerId, @NonNull byte[] isoTemplateBytes)
            throws IllegalArgumentException {
        this.fingerId = fingerId;
        //noinspection ConstantConditions
        if (isoTemplateBytes == null)
            throw new IllegalArgumentException("Invalid template");
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
                throw new IllegalArgumentException("Invalid template: only single fingerprint template ares supported");
            }
        } catch (IndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("Invalid template: Processing byte[] failed");
        }
    }

    /**
     * ISO 2005 base 64 string constructor
     *
     * @param fingerId          Finger identifier of the fingerprint
     * @param isoBase64Template Base64 encoded ISO 2005 fingerprint template
     * @throws IllegalArgumentException If the string specified is not a valid base 64
     *                                  encoded ISO 2005 (2011 not supported yet) template containing only 1 fingerprint.
     */
    public Fingerprint(@NonNull FingerIdentifier fingerId, @NonNull String isoBase64Template)
            throws IllegalArgumentException {
        this(fingerId, Utils.base64ToBytes(isoBase64Template));
    }


    public FingerIdentifier getFingerId() {
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
     * @return A reference to the direct ByteBuffer containing the ISO 2005 template of
     * the fingerprint
     */
    public ByteBuffer getTemplateDirectBuffer() {
        template.position(0);
        return template;
    }

    /**
     * @return The quality score of this fingerprint, as stored in its template
     */
    public int getQualityScore() {
        return template.get(FIRST_QUALITY);
    }


    /**
     * @return A random valid {@link Fingerprint} with a random {@link FingerIdentifier}
     */
    public static Fingerprint generateRandomFingerprint() {
        int fingerNo = RANDOM_GENERATOR.nextInt(FINGER_IDENTIFIERS.length);
        FingerIdentifier fingerId = FINGER_IDENTIFIERS[fingerNo];
        return generateRandomFingerprint(fingerId);
    }

    /**
     * @param fingerId Finger identifier of the fingerprint
     * @return A random valid {@link Fingerprint} with specified {@link FingerIdentifier}
     */
    public static Fingerprint generateRandomFingerprint(@NonNull FingerIdentifier fingerId) {
        byte qualityScore = (byte) RANDOM_GENERATOR.nextInt(101);
        return generateRandomFingerprint(fingerId, qualityScore);
    }


    /**
     * @param fingerId     Finger identifier of the fingerprint
     * @param qualityScore Quality score of the fingerprint
     * @return A random valid {@link Fingerprint} with specified {@link FingerIdentifier}
     */
    public static Fingerprint generateRandomFingerprint(@NonNull FingerIdentifier fingerId,
                                                          byte qualityScore) {
        byte nbMinutiae = (byte) RANDOM_GENERATOR.nextInt(128);
        int length = HEADER_SIZE + nbMinutiae * MINUTIAE_SIZE;

        ByteBuffer bb = ByteBuffer.allocateDirect(length);
        bb.order(ByteOrder.BIG_ENDIAN);

        bb.putInt(FORMAT_ID, ISO_FORMAT_ID);
        bb.putInt(VERSION, ISO_2005_VERSION);
        bb.putInt(RECORD_LENGTH, length);
        bb.putShort(WIDTH, SECUGEN_HAMSTER_WIDTH);
        bb.putShort(HEIGHT, SECUGEN_HAMSTER_HEIGHT);
        bb.putShort(HORIZONTAL_PPCM, SECUGEN_HAMSTER_PPCM);
        bb.putShort(VERTICAL_PPCM, SECUGEN_HAMSTER_PPCM);
        bb.put(NB_FINGERPRINTS, (byte) 1);
        bb.put(FIRST_FINGER_POSITION, (byte) 0);
        bb.put(FIRST_QUALITY, qualityScore);
        bb.put(FIRST_NB_MINUTIAE, nbMinutiae);

        for (int minutiaNo = 0; minutiaNo < nbMinutiae; minutiaNo++) {
            short type = (short) (RANDOM_GENERATOR.nextInt(3) << 14);
            short x = (short) RANDOM_GENERATOR.nextInt(SECUGEN_HAMSTER_WIDTH);
            short y = (short) RANDOM_GENERATOR.nextInt(SECUGEN_HAMSTER_HEIGHT);
            byte angle = (byte) RANDOM_GENERATOR.nextInt(256);
            byte quality = (byte) RANDOM_GENERATOR.nextInt(101);
            bb.putShort(FIRST_MINUTIAE_START + minutiaNo * MINUTIAE_SIZE + TYPE_AND_X_SHIFT, (short) (type + x));
            bb.putShort(FIRST_MINUTIAE_START + minutiaNo * MINUTIAE_SIZE + ZEROS_AND_Y_SHIFT, y);
            bb.put(FIRST_MINUTIAE_START + minutiaNo * MINUTIAE_SIZE + ANGLE_SHIFT, angle);
            bb.put(FIRST_MINUTIAE_START + minutiaNo * MINUTIAE_SIZE + QUALITY_SHIFT, quality);
        }

        bb.position(0);
        byte[] templateBytes = new byte[bb.remaining()];
        bb.get(templateBytes);
        return new Fingerprint(fingerId, templateBytes);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Fingerprint that = (Fingerprint) o;

        return (fingerId.equals(that.fingerId) &&
                Arrays.equals(this.getTemplateBytes(), that.getTemplateBytes()));
    }

    @Override
    public int hashCode() {
        int result = fingerId.hashCode();
        result = 31 * result + Arrays.hashCode(this.getTemplateBytes());
        return result;
    }

    @Override
    public String toString() {
        return String.format(Locale.UK, "%s:%s", fingerId.toString(), Utils.byteArrayToBase64(getTemplateBytes()));
    }

    protected Fingerprint(Parcel in) {
        fingerId = FingerIdentifier.values()[in.readInt()];
        byte[] temp = new byte[in.readInt()];
        in.readByteArray(temp);
        template = ByteBuffer.allocateDirect(temp.length);
        template.put(temp);
    }

    public static final Creator<Fingerprint> CREATOR = new Creator<Fingerprint>() {
        @Override
        public Fingerprint createFromParcel(Parcel in) {
            return new Fingerprint(in);
        }

        @Override
        public Fingerprint[] newArray(int size) {
            return new Fingerprint[size];
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
