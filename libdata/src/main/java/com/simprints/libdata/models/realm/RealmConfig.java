package com.simprints.libdata.models.realm;

import android.support.annotation.NonNull;

import java.util.Arrays;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmConfig {
    public static RealmConfiguration get(@NonNull String apiKey) {
        String dbName = String.format("%s.realm", apiKey.substring(0, 8));

        byte[] key = Arrays.copyOf(apiKey.getBytes(), 64);

        return new RealmConfiguration.Builder()
                .name(dbName)
                .schemaVersion(1)
                .migration(new Migration())
                .encryptionKey(key)
                .modules(Realm.getDefaultModule())
                .build();
    }
}
