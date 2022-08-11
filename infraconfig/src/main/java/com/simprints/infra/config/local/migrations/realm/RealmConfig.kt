package com.simprints.infra.config.local.migrations.realm

import com.simprints.infra.config.BuildConfig
import io.realm.RealmConfiguration
import io.realm.annotations.RealmModule

object RealmConfig {

    private const val REALM_SCHEMA_VERSION = 11L

    @RealmModule(
        classes = [
            DbProject::class
        ]
    )
    class Module

    fun get(databaseName: String, key: ByteArray): RealmConfiguration {
        val builder = RealmConfiguration
            .Builder()
            .name("$databaseName.realm")
            .schemaVersion(REALM_SCHEMA_VERSION)
            .modules(Module())

        if (BuildConfig.DB_ENCRYPTION)
            builder.encryptionKey(key)

        return builder.build()
    }
}
