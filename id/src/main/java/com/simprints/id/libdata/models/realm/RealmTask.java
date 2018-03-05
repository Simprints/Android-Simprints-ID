package com.simprints.id.libdata.models.realm;

import io.realm.Realm;

public interface RealmTask {
    void run(Realm realm);

    void callback();
}
