package com.simprints.id.libdata.models.realm;

import android.support.annotation.NonNull;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@SuppressWarnings({"WeakerAccess"})
public class rl_ApiKey extends RealmObject {
    @PrimaryKey
    public String apiKey;
    public Boolean valid;

    public rl_ApiKey() {
    }

    public rl_ApiKey(String apiKey, Boolean valid) {
        this.apiKey = apiKey;
        this.valid = valid;
    }

    public static void save(@NonNull Realm realm, @NonNull final String apiKey, final boolean valid) {
        if (realm.isClosed())
            return;

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(new rl_ApiKey(apiKey, valid));
            }
        });
    }

    public static boolean check(@NonNull Realm realm, @NonNull String apiKey) {
        rl_ApiKey key = realm.where(rl_ApiKey.class).equalTo("apiKey", apiKey).findFirst();
        return key != null && key.valid != null && key.valid;
    }
}
