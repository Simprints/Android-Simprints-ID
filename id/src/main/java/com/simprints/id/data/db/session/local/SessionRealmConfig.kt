package com.simprints.id.data.db.session.local

import com.simprints.id.data.db.session.local.SessionRealmMigration.Companion.REALM_SCHEMA_VERSION
import io.realm.RealmConfiguration

object SessionRealmConfig {

    fun get(databaseName: String, key: ByteArray): RealmConfiguration = RealmConfiguration
        .Builder()
        .name("$databaseName.realm")
        .schemaVersion(REALM_SCHEMA_VERSION)
        .deleteRealmIfMigrationNeeded()
        .encryptionKey(key)
        .modules(SessionRealmMigration.SessionModule())
        .build()
}
