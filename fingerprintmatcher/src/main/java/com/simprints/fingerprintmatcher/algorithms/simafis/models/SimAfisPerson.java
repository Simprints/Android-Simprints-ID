package com.simprints.fingerprintmatcher.algorithms.simafis.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class SimAfisPerson implements Parcelable {

    private final static Random RANDOM_GENERATOR = new Random();
    private final static SimAfisFingerIdentifier[] FINGER_IDENTIFIERS = SimAfisFingerIdentifier.values();

    private final String guid;
    private final Map<SimAfisFingerIdentifier, SimAfisFingerprint> fingerprints;

    /**
     * Constructor without fingerprints
     * @param guid Global Unique Id of the person
     */
    public SimAfisPerson(@NonNull String guid)
    {
        this(guid, Collections.<SimAfisFingerprint>emptyList());
    }

    /**
     * Constructor with fingerprints, equivalent to calling {@link #SimAfisPerson(String)} then
     * {@link #addFingerprint(SimAfisFingerprint)} for each fingerprint of the list
     *
     * Note: if the specified list of fingerprints contains several fingerprints of the same
     * finger, only the one with the highest score will be kept.
     *
     * @param guid          Global Unique Id of the person
     * @param fingerprints  Fingerprints of the person
     */
    public SimAfisPerson(@NonNull String guid, @NonNull List<SimAfisFingerprint> fingerprints)
    {
        this.guid = guid;
        this.fingerprints = new HashMap<>();
        for (SimAfisFingerprint print : fingerprints) {
            if (!hasBetterOrSameThan(print)) {
                this.fingerprints.put(print.getFingerId(), print);
            }
        }
    }

    /**
     * @param print A fingerprint
     *
     * @return True if and only if this person has a {@link SimAfisFingerprint} for
     * the {@link SimAfisFingerIdentifier} of the specified {@link SimAfisFingerprint}, and its quality
     * score is greater than or equal to the specified fingerprint's
     */
    public boolean hasBetterOrSameThan(@NonNull SimAfisFingerprint print)
    {
        SimAfisFingerprint currentPrint = fingerprints.get(print.getFingerId());
        return (currentPrint != null &&
                currentPrint.getQualityScore() >= print.getQualityScore());
    }

    public String getGuid()
    {
        return guid;
    }

    /**
     * @return A newly allocated list of the fingerprints of this Person
     */
    public List<SimAfisFingerprint> getFingerprints()
    {
        return new ArrayList<>(fingerprints.values());
    }

    /**
     * @param fingerId {@link SimAfisFingerIdentifier} of the requested {@link SimAfisFingerprint}
     *
     * @return The {@link SimAfisFingerprint} of this person for the specified
     * {@link SimAfisFingerIdentifier} if it exists, null else
     *
     */
    public SimAfisFingerprint getFingerprint(SimAfisFingerIdentifier fingerId) {
        return fingerprints.get(fingerId);
    }


    public void addFingerprint(@NonNull SimAfisFingerprint print)
    {
        this.fingerprints.put(print.getFingerId(), print);
    }

    /**
     * @return A person with a random global unique id and a random set of fingerprints
     */
    static public SimAfisPerson generateRandomPerson()
    {
        String guid = UUID.randomUUID().toString();
        // Generate a random mask with one bit for each finger, to decide
        // if the Person should have a fingerprint for them or not
        int printsMask = RANDOM_GENERATOR.nextInt(1 << FINGER_IDENTIFIERS.length);

        List<SimAfisFingerprint> prints = new ArrayList<>();
        for (int fingerNo = 0; fingerNo < FINGER_IDENTIFIERS.length; fingerNo++) {
            if ((printsMask & (1 << fingerNo)) != 0) {
                prints.add(SimAfisFingerprint.generateRandomFingerprint(FINGER_IDENTIFIERS[fingerNo]));
            }
        }

        return new SimAfisPerson(guid, prints);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimAfisPerson person = (SimAfisPerson) o;

        return guid.equals(person.guid) && fingerprints.equals(person.fingerprints);

    }

    @Override
    public int hashCode()
    {
        int result = guid.hashCode();
        result = 31 * result + fingerprints.hashCode();
        return result;
    }

    @NotNull
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(Locale.UK, "Person %s, Fingerprints:\n", guid));

        for (SimAfisFingerIdentifier fingerId : FINGER_IDENTIFIERS) {
            if (fingerprints.containsKey(fingerId)) {
                builder.append(String.format(Locale.UK, "%s\n", fingerprints.get(fingerId)));
            }
        }
        builder.append("\n");
        return builder.toString();
    }

    protected SimAfisPerson(Parcel in)
    {
        guid = in.readString();
        int nbFingerprints = in.readInt();
        fingerprints = new HashMap<>(nbFingerprints);
        for (int i = 0; i < nbFingerprints; i++) {
            SimAfisFingerprint fingerprint = in.readParcelable(SimAfisFingerprint.class.getClassLoader());
            if (fingerprint != null) {
                fingerprints.put(fingerprint.getFingerId(), fingerprint);
            }
        }
    }

    public static final Creator<SimAfisPerson> CREATOR = new Creator<SimAfisPerson>()
    {
        @Override
        public SimAfisPerson createFromParcel(Parcel in)
        {
            return new SimAfisPerson(in);
        }

        @Override
        public SimAfisPerson[] newArray(int size)
        {
            return new SimAfisPerson[size];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(guid);
        dest.writeInt(fingerprints.size());
        for (SimAfisFingerprint fp : fingerprints.values()) {
            dest.writeParcelable(fp, flags);
        }
    }

}
