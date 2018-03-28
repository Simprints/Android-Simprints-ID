package com.simprints.id.data.db.local

import com.simprints.id.data.db.local.Migration.Companion.REALM_SCHEMA_VERSION
import io.realm.Realm
import io.realm.RealmConfiguration

object RealmConfig {

    fun get(projectId: String, localDbKey: LocalDbKey): RealmConfiguration = RealmConfiguration
        .Builder()
        .name("$projectId.realm")
        .schemaVersion(REALM_SCHEMA_VERSION)
        .migration(Migration())
        .encryptionKey(localDbKey.value)
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
