package com.simprints.id.data.db.local.realm

import android.content.Context
import com.simprints.id.data.db.local.models.LocalDbKey
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.File

class EncryptionMigration(localDbKey: LocalDbKey, private val appContext: Context) {

    companion object {
        private const val LEGACY_APP_KEY_LENGTH: Int = 8
    }

    init {
        migrateLegacyDatabaseIfRequired(localDbKey)
    }

    private fun migrateLegacyDatabaseIfRequired(dbKey: LocalDbKey) {
        if (checkIfLegacyDatabaseNeedsToMigrate(dbKey))
            migrateLegacyRealm(dbKey)
    }

    private fun checkIfLegacyDatabaseNeedsToMigrate(dbKey: LocalDbKey): Boolean {
        if (dbKey.legacyApiKey.isEmpty())
            return false

        val legacyConfig = getLegacyConfig(dbKey.legacyApiKey, dbKey.legacyRealmKey, dbKey.projectId)
        val newConfig = RealmConfig.get(dbKey.projectId, dbKey.value, dbKey.projectId)

        return File(legacyConfig.path).exists() && !File(newConfig.path).exists()
    }

    private fun migrateLegacyRealm(dbKey: LocalDbKey) {
        val legacyConfig = getLegacyConfig(dbKey.legacyApiKey, dbKey.legacyRealmKey, dbKey.projectId)

        Realm.getInstance(legacyConfig).use {
            it.writeEncryptedCopyTo(File(appContext.filesDir, "${dbKey.projectId}.realm"), dbKey.value)
        }

        deleteRealm(legacyConfig)
    }

    private fun getLegacyConfig(legacyApiKey: String, legacyDatabaseKey: ByteArray, projectId: String): RealmConfiguration =
        RealmConfig.get(legacyApiKey.substring(0, LEGACY_APP_KEY_LENGTH), legacyDatabaseKey, projectId)

    private fun deleteRealm(config: RealmConfiguration) {
        Realm.deleteRealm(config)
        File(appContext.filesDir, "${config.path}.lock").delete()
    }
}
