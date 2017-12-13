package com.simprints.libdata.models.sql_OLD;

import android.support.annotation.NonNull;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"FieldCanBeLocal"})
@Table(name = "ApiKeys")
public class M_ApiKey extends Model {
    @Column(name = "Key", unique = true)
    private String apiKey;
    @Column(name = "Status")
    private Status status;
    @Column(name = "LatestSyncId")
    private long latestSyncId;

    /**
     * Default constructor used by ActiveAndroid
     * DO NOT USE FOR ANYTHING ELSE
     */
    public M_ApiKey() {
        super();
    }

    // DB accessors

    /**
     * Builds a new model for the specified api key
     * Note: the initial syncId is 0, and the initial status is UNVERIFIED
     *
     * @param apiKey An api key
     */
    public M_ApiKey(@NonNull String apiKey) {
        super();
        status = Status.UNVERIFIED;
        latestSyncId = 0;
        this.apiKey = apiKey;
    }

    /**
     * @param apiKey An api key
     * @return If it exists, the M_ApiKey object stored in the local database for the
     * specified key, else null
     */
    public static M_ApiKey get(@NonNull String apiKey) {
        return new Select()
                .from(M_ApiKey.class)
                .where("Key = ?", apiKey)
                .executeSingle();
    }

    // Instance methods

    /**
     * @return A newly allocated list of all the M_ApiKey objects stored in the local database
     */
    public static List<M_ApiKey> getAll() {
        List<M_ApiKey> mApiKeys = new Select().from(M_ApiKey.class).execute();
        return mApiKeys == null ? new ArrayList<M_ApiKey>() : mApiKeys;
    }

    /**
     * @return The api key of this M_ApiKey object
     */
    public String asString() {
        return this.apiKey;
    }

    /**
     * @return The status (VALID, INVALID, or UNVERIFIED) of this M_ApiKey object
     */
    public Status getStatus() {
        return this.status;
    }

    public enum Status {
        UNVERIFIED,
        VALID,
        INVALID
    }
}

