package com.simprints.id.data.db.common.realm

import com.simprints.id.data.db.common.realm.SubjectsRealmMigration.Companion.REALM_SCHEMA_VERSION
import io.realm.RealmConfiguration

object SubjectsRealmConfig {

    fun get(databaseName: String, key: ByteArray, projectId: String): RealmConfiguration = RealmConfiguration
        .Builder()
        .name("$databaseName.realm")
        .schemaVersion(REALM_SCHEMA_VERSION)
        .migration(SubjectsRealmMigration(projectId))
        .encryptionKey(key)
        .modules(SubjectsRealmMigration.SubjectsModule())
        .build()
}
