package com.simprints.libdata.models.realm;

import android.support.annotation.NonNull;

import java.util.Arrays;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmConfig {
    public static RealmConfiguration get(@NonNull String localDbKey) {
        String dbName = String.format("%s.realm", localDbKey);

        byte[] key = Arrays.copyOf(localDbKey.getBytes(), 64);

        return new RealmConfiguration.Builder()
                .name(dbName)
                .schemaVersion(1)
                .migration(new Migration())
                .encryptionKey(key)
                .modules(Realm.getDefaultModule())
                .build();
    }
}
