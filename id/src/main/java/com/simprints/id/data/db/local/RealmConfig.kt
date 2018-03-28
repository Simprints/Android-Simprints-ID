package com.simprints.id.data.db.local

import com.simprints.id.data.db.local.Migration.Companion.REALM_SCHEMA_VERSION
import io.realm.Realm
import io.realm.RealmConfiguration

object RealmConfig {

    fun get(databaseName: String, key: ByteArray): RealmConfiguration = RealmConfiguration
        .Builder()
        .name("$databaseName.realm")
        .schemaVersion(REALM_SCHEMA_VERSION)
        .migration(Migration())
        .encryptionKey(key)
        .addModulesIfNotNull()
        .build()

    private fun RealmConfiguration.Builder.addModulesIfNotNull(): RealmConfiguration.Builder {
        val defaultModule = Realm.getDefaultModule()
        return if (defaultModule != null)
            this.modules(defaultModule)
        else
            this
    }
}
