package com.simprints.id.data.db.local.realm

import com.simprints.id.data.db.local.realm.Migration.Companion.REALM_SCHEMA_VERSION
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

    private fun RealmConfiguration.Builder.addModulesIfNotNull(): RealmConfiguration.Builder =
        Realm.getDefaultModule()?.let { this.modules(it) }?: this
}
