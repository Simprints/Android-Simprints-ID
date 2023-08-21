package com.simprints.fingerprint.infra.simafiswrapper.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Represents a person in the Simprints Afis system.
 * A person is identified by a Global Unique Id (GUID) and has a list of fingerprints.
 * <p>
 * Note: if the specified list of fingerprints contains several fingerprints of the same
 * finger, only the one with the highest score will be kept.
 */
@ExcludedFromGeneratedTestCoverageReports(reason ="POJO")
public class SimAfisPerson implements Parcelable {

    private final static SimAfisFingerIdentifier[] FINGER_IDENTIFIERS = SimAfisFingerIdentifier.values();

    private final String guid;
    private final Map<SimAfisFingerIdentifier, SimAfisFingerprint> fingerprints;


    /**
     * Constructor with fingerprints
     * Note: if the specified list of fingerprints contains several fingerprints of the same
     * finger, only the one with the highest score will be kept.
     *
     * @param guid         Global Unique Id of the person
     * @param fingerprints Fingerprints of the person
     */
    public SimAfisPerson(@NonNull String guid, @NonNull List<SimAfisFingerprint> fingerprints) {
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
     * @return True if and only if this person has a {@link SimAfisFingerprint} for
     * the {@link SimAfisFingerIdentifier} of the specified {@link SimAfisFingerprint}, and its quality
     * score is greater than or equal to the specified fingerprint's
     */
    public boolean hasBetterOrSameThan(@NonNull SimAfisFingerprint print) {
        SimAfisFingerprint currentPrint = fingerprints.get(print.getFingerId());
        return (currentPrint != null &&
            currentPrint.getQualityScore() >= print.getQualityScore());
    }

    public String getGuid() {
        return guid;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimAfisPerson person = (SimAfisPerson) o;

        return guid.equals(person.guid) && fingerprints.equals(person.fingerprints);

    }

    @Override
    public int hashCode() {
        int result = guid.hashCode();
        result = 31 * result + fingerprints.hashCode();
        return result;
    }

    @NotNull
    @Override
    public String toString() {
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

    protected SimAfisPerson(Parcel in) {
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

    public static final Creator<SimAfisPerson> CREATOR = new Creator() {
        @Override
        public SimAfisPerson createFromParcel(Parcel in) {
            return new SimAfisPerson(in);
        }

        @Override
        public SimAfisPerson[] newArray(int size) {
            return new SimAfisPerson[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(guid);
        dest.writeInt(fingerprints.size());
        for (SimAfisFingerprint fp : fingerprints.values()) {
            dest.writeParcelable(fp, flags);
        }
    }

}
