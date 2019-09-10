package com.simprints.id.data.db.local.realm

import com.simprints.id.data.db.local.realm.PeopleRealmMigration.Companion.REALM_SCHEMA_VERSION
import io.realm.RealmConfiguration

object PeopleRealmConfig {

    fun get(databaseName: String, key: ByteArray, projectId: String): RealmConfiguration = RealmConfiguration
        .Builder()
        .name("$databaseName.realm")
        .schemaVersion(REALM_SCHEMA_VERSION)
        .migration(PeopleRealmMigration(projectId))
        .encryptionKey(key)
        .modules(PeopleRealmMigration.PeopleModule())
        .build()
}
