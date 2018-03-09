package com.simprints.id.sync.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmSyncInfo extends RealmObject {
    @PrimaryKey
    public int id;
    public Date lastSyncTime;

    public RealmSyncInfo() {
    }

    public RealmSyncInfo(Date lastSyncTime) {
        this.id = 0;
        this.lastSyncTime = lastSyncTime;
    }
}
