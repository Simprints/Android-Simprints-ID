package com.simprints.id.data.db.local

import io.realm.Realm
import io.realm.RealmConfiguration

object RealmConfig {
    fun get(projectId: String, localDbKey: LocalDbKey): RealmConfiguration {
        val dbName = String.format("%s.realm", projectId)

        return RealmConfiguration.Builder()
            .name(dbName)
            .schemaVersion(1)
            .migration(Migration())
            .encryptionKey(localDbKey.value)
            .addModulesIfNotNull()
            .build()
    }

    private fun RealmConfiguration.Builder.addModulesIfNotNull(): RealmConfiguration.Builder {
        val defaultModule = Realm.getDefaultModule()
        return if (defaultModule != null)
            this.modules(defaultModule)
        else
            this
    }
}
