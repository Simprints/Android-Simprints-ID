package com.simprints.infra.config.local.migrations.realm

import com.simprints.infra.config.BuildConfig
import com.simprints.infra.logging.Simber
import io.realm.RealmConfiguration
import io.realm.annotations.RealmModule

object RealmConfig {

    @RealmModule(
        classes = [
            DbProject::class
        ]
    )
    class ProjectModule

    fun get(databaseName: String, key: ByteArray): RealmConfiguration {
        Simber.i("database name $databaseName")
        val builder = RealmConfiguration
            .Builder()
            .name("$databaseName.realm")
            .schemaVersion(11L)
            .addModule(ProjectModule())


        if (BuildConfig.DB_ENCRYPTION)
            builder.encryptionKey(key)

        return builder.build()
    }
}
