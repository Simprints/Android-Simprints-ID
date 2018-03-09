package com.simprints.id.data.db.local

import java.util.Arrays

import io.realm.Realm
import io.realm.RealmConfiguration

object RealmConfig {
    fun get(projectId: String, localDbKey: String): RealmConfiguration {
        val dbName = String.format("%s.realm", projectId)

        val key = Arrays.copyOf(localDbKey.toByteArray(), 64)

        return RealmConfiguration.Builder()
            .name(dbName)
            .schemaVersion(1)
            .migration(Migration())
            .encryptionKey(key)
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
