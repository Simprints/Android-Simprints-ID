package com.simprints.id.data.analytics.eventData.controllers.local

import com.simprints.id.data.analytics.eventData.controllers.local.SessionRealmMigration.Companion.REALM_SCHEMA_VERSION
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
