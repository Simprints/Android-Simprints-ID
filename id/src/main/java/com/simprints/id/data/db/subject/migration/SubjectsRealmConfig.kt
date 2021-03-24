package com.simprints.id.data.db.subject.migration

import com.simprints.id.data.db.subject.migration.SubjectsRealmMigration
import com.simprints.id.data.db.subject.migration.SubjectsRealmMigration.Companion.REALM_SCHEMA_VERSION
import io.realm.RealmConfiguration
import com.simprints.id.BuildConfig

object SubjectsRealmConfig {

    fun get(databaseName: String, key: ByteArray, projectId: String): RealmConfiguration {
      val builder = RealmConfiguration
        .Builder()
        .name("$databaseName.realm")
        .schemaVersion(REALM_SCHEMA_VERSION)
        .migration(SubjectsRealmMigration(projectId))

        .modules(SubjectsRealmMigration.SubjectsModule())

      if (BuildConfig.DB_ENCRYPTION)
        builder.encryptionKey(key)

      return builder.build()
    }
}
