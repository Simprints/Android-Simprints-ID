package com.simprints.id.data.db.session.local

import io.realm.RealmConfiguration

class SessionRealmConfigBuilderImpl  {

    fun get(databaseName: String, key: ByteArray): RealmConfiguration = RealmConfiguration
        .Builder()
        .name("$databaseName.realm")
        .schemaVersion(SessionRealmMigration.REALM_SCHEMA_VERSION)
        .deleteRealmIfMigrationNeeded()
        .encryptionKey(key)
        .modules(SessionRealmMigration.SessionModule())
        .build()
}
