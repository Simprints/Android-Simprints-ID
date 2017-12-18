package com.simprints.libdata.models.sql_OLD;

import android.support.annotation.NonNull;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

@Table(name = "SyncOperations")
public class M_SyncOp extends Model {

    @Column(name = "SyncId")
    private long syncId;

    @Column(name = "TableName")
    private String tableName;

    @Column(name = "Action")
    private String action;

    @Column(name = "ObjectId")
    private int objectId;

    @Column(name = "Date")
    private Long date;

    @Column(name = "Key")
    private M_ApiKey mApiKey;

    // DB accessors
    public M_SyncOp() {
        super();
    }

    /**
     * @param mApiKey An api key
     * @return A newly allocated list of the sync operations linked to the specified api key
     */
    public static
    @NonNull
    List<M_SyncOp> getAll(@NonNull M_ApiKey mApiKey) {
        List<M_SyncOp> mSyncOps = new Select()
                .from(M_SyncOp.class)
                .where("Key = ?", mApiKey.getId())
                .orderBy("SyncId ASC")
                .execute();
        return mSyncOps != null ? mSyncOps : new ArrayList<M_SyncOp>();
    }
}
