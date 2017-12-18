package com.simprints.libdata.models.sql_OLD;

import android.support.annotation.NonNull;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.simprints.libcommon.Fingerprint;
import com.simprints.libcommon.Utils;
import com.simprints.libsimprints.FingerIdentifier;

import java.util.ArrayList;
import java.util.List;

@Table(name = "Fingerprints")
public class M_Fingerprint extends Model {

    @Column(name = "FingerIdentifier")
    private FingerIdentifier fingerId;

    @Column(name = "Base64IsoTemplate")
    private String template;

    @Column(name = "QualityScore")
    private int qualityScore;

    @Column(name = "IsSynced")
    private boolean synced;

    @Column(name = "Person",
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete = Column.ForeignKeyAction.CASCADE,
            notNull = true)
    private M_Person mPerson;

    // DB accessors

    /**
     * @param mPerson An M_Person object
     * @return A newly allocated list of all the M_Fingerprint objects stored in the local
     * database for the specified person
     */
    public static
    @NonNull
    List<M_Fingerprint> get(@NonNull M_Person mPerson) {
        List<M_Fingerprint> mPrints = new Select().
                from(M_Fingerprint.class)
                .where("Person = ?", mPerson.getId())
                .execute();

        return mPrints != null ? mPrints : new ArrayList<M_Fingerprint>();
    }

    /**
     * If the specified person has no duplicate (ie fingerprint for the same finger) of the
     * specified fingerprint, adds it in the database.
     * Else, updates the existing fingerprint only if its quality score is higher
     * than the specified fingerprint.
     *
     * @param mPerson    Owner of the specified fingerprint to save/update
     * @param fromServer Set to true if the specified fingerprint was obtained from the remote
     *                   server, and to false else. Makes syncing more efficient.
     */
    public static void saveOrUpdate(@NonNull Fingerprint print, @NonNull M_Person mPerson,
                                    boolean fromServer) {
        // If the person does not exist in the database, creates it
        M_Fingerprint mFingerprint = new Select()
                .from(M_Fingerprint.class)
                .where("Person = ?", mPerson.getId())
                .where("FingerIdentifier = ?", print.getFingerId().name())
                .executeSingle();

        if (mFingerprint == null) {
            mFingerprint = new M_Fingerprint(print, mPerson, fromServer);
        }

        // Else if a server print has the same quality, consider the local print has synced
        else if (mFingerprint.qualityScore == print.getQualityScore() && fromServer && !mFingerprint.synced) {
            mFingerprint.synced = true;
        }
        // Else updates the existing fingerprint if its score is lower
        else if (mFingerprint.qualityScore < print.getQualityScore()) {
            mFingerprint.template = Utils.byteArrayToBase64(print.getTemplateBytes());
            mFingerprint.qualityScore = print.getQualityScore();
            mFingerprint.synced = fromServer;
        }

        mFingerprint.save();
    }

    // Instance methods
    public M_Fingerprint() {
        super();
    }

    public M_Fingerprint(@NonNull Fingerprint print, @NonNull M_Person mPerson, boolean synced) {
        super();
        this.fingerId = print.getFingerId();
        this.template = Utils.byteArrayToBase64(print.getTemplateBytes());
        this.qualityScore = print.getQualityScore();
        this.mPerson = mPerson;
        this.synced = synced;
    }

    public Fingerprint load() {
        return new Fingerprint(fingerId, template);
    }
}

