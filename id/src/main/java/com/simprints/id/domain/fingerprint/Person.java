package com.simprints.id.domain.fingerprint;

import android.os.Parcel;
import android.os.Parcelable;

import com.simprints.id.FingerIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public class Person implements Parcelable {

    private final static Random RANDOM_GENERATOR = new Random();
    private final static FingerIdentifier[] FINGER_IDENTIFIERS = FingerIdentifier.values();

    private final String guid;
    private final Map<FingerIdentifier, Fingerprint> fingerprints;

    /**
     * Constructor without fingerprints
     * @param guid Global Unique Id of the person
     */
    public Person(@NonNull String guid)
    {
        this(guid, Collections.<Fingerprint>emptyList());
    }

    /**
     * Constructor with fingerprints, equivalent to calling {@link #Person(String)} then
     * {@link #addFingerprint(Fingerprint)} for each fingerprint of the list
     *
     * Note: if the specified list of fingerprints contains several fingerprints of the same
     * finger, only the one with the highest score will be kept.
     *
     * @param guid          Global Unique Id of the person
     * @param fingerprints  Fingerprints of the person
     */
    public Person(@NonNull String guid, @NonNull List<Fingerprint> fingerprints)
    {
        this.guid = guid;
        this.fingerprints = new HashMap<>();
        for (Fingerprint print : fingerprints) {
            if (!hasBetterOrSameThan(print)) {
                this.fingerprints.put(print.getFingerId(), print);
            }
        }
    }

    /**
     * @param print A fingerprint
     *
     * @return True if and only if this person has a {@link Fingerprint} for
     * the {@link FingerIdentifier} of the specified {@link Fingerprint}, and its quality
     * score is greater than or equal to the specified fingerprint's
     */
    public boolean hasBetterOrSameThan(@NonNull Fingerprint print)
    {
        Fingerprint currentPrint = fingerprints.get(print.getFingerId());
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
    public List<Fingerprint> getFingerprints()
    {
        return new ArrayList<>(fingerprints.values());
    }

    /**
     * @param fingerId {@link FingerIdentifier} of the requested {@link Fingerprint}
     *
     * @return The {@link Fingerprint} of this person for the specified
     * {@link FingerIdentifier} if it exists, null else
     *
     */
    public Fingerprint getFingerprint(FingerIdentifier fingerId) {
        return fingerprints.get(fingerId);
    }


    public void addFingerprint(@NonNull Fingerprint print)
    {
        this.fingerprints.put(print.getFingerId(), print);
    }

    /**
     * @return A person with a random global unique id and a random set of fingerprints
     */
    static public Person generateRandomPerson()
    {
        String guid = UUID.randomUUID().toString();
        // Generate a random mask with one bit for each finger, to decide
        // if the Person should have a fingerprint for them or not
        int printsMask = RANDOM_GENERATOR.nextInt(1 << FINGER_IDENTIFIERS.length);

        List<Fingerprint> prints = new ArrayList<>();
        for (int fingerNo = 0; fingerNo < FINGER_IDENTIFIERS.length; fingerNo++) {
            if ((printsMask & (1 << fingerNo)) != 0) {
                prints.add(Fingerprint.generateRandomFingerprint(FINGER_IDENTIFIERS[fingerNo]));
            }
        }

        return new Person(guid, prints);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        return guid.equals(person.guid) && fingerprints.equals(person.fingerprints);

    }

    @Override
    public int hashCode()
    {
        int result = guid.hashCode();
        result = 31 * result + fingerprints.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(Locale.UK, "Person %s, Fingerprints:\n", guid));

        for (FingerIdentifier fingerId : FINGER_IDENTIFIERS) {
            if (fingerprints.containsKey(fingerId)) {
                builder.append(String.format(Locale.UK, "%s\n", fingerprints.get(fingerId)));
            }
        }
        builder.append("\n");
        return builder.toString();
    }

    protected Person(Parcel in)
    {
        guid = in.readString();
        int nbFingerprints = in.readInt();
        fingerprints = new HashMap<>(nbFingerprints);
        for (int i = 0; i < nbFingerprints; i++) {
            Fingerprint fingerprint = in.readParcelable(Fingerprint.class.getClassLoader());
            fingerprints.put(fingerprint.getFingerId(), fingerprint);
        }
    }

    public static final Creator<Person> CREATOR = new Creator<Person>()
    {
        @Override
        public Person createFromParcel(Parcel in)
        {
            return new Person(in);
        }

        @Override
        public Person[] newArray(int size)
        {
            return new Person[size];
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
        for (Fingerprint fp : fingerprints.values()) {
            dest.writeParcelable(fp, flags);
        }
    }
}
