package com.simprints.id.data.analytics.eventData.realm

import com.simprints.id.data.analytics.eventData.realm.SessionRealmMigration.Companion.REALM_SCHEMA_VERSION
import io.realm.RealmConfiguration

object SessionRealmConfig {

    fun get(databaseName: String, key: ByteArray): RealmConfiguration = RealmConfiguration
        .Builder()
        .name("$databaseName.realm")
        .schemaVersion(REALM_SCHEMA_VERSION)
        .migration(SessionRealmMigration())
        .encryptionKey(key)
        .modules(SessionRealmMigration.SessionModule())
        .build()

}
