package com.simprints.id.data.db.subject.migration

import com.simprints.id.data.db.subject.migration.SubjectsRealmMigration
import com.simprints.id.data.db.subject.migration.SubjectsRealmMigration.Companion.REALM_SCHEMA_VERSION
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
