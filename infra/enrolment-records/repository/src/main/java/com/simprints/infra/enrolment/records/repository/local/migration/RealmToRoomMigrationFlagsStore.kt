package com.simprints.infra.enrolment.records.repository.local.migration

import androidx.core.content.edit
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.REALM_DB_MIGRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

// Should be completely removed when all users are migrated to Room
@Singleton
class RealmToRoomMigrationFlagsStore @Inject constructor(
    securityManager: SecurityManager,
    private val configRepo: ConfigRepository,
) {
    private val prefs = securityManager.buildEncryptedSharedPreferences(PREF_FILE_NAME)

    private val mutex = Mutex()

    companion object {
        private const val PREF_FILE_NAME = "2ac059c3-8014-40f1-sssd-4219f2de4a1d"
        const val KEY_MIGRATION_STATUS = "migration_status_v2025_2_0"
        const val KEY_MIGRATION_RETRIES = "migration_retries_v2025_2_0"
        const val KEY_DOWN_SYNC_STATUS = "down_sync_status_v2025_2_0"
    }

    suspend fun isMigrationInProgress() = getCurrentStatus() == MigrationStatus.IN_PROGRESS

    suspend fun isMigrationCompleted() = getCurrentStatus() == MigrationStatus.COMPLETED

    suspend fun getCurrentStatus(): MigrationStatus = mutex.withLock {
        MigrationStatus.valueOf(prefs.getString(KEY_MIGRATION_STATUS, MigrationStatus.NOT_STARTED.name)!!)
    }

    suspend fun updateStatus(status: MigrationStatus) = mutex.withLock {
        prefs.edit {
            putString(KEY_MIGRATION_STATUS, status.name)
        }
        Simber.i("[RealmToRoomMigrationFlagsStore] Migration status updated to: $status", tag = REALM_DB_MIGRATION)
    }

    suspend fun incrementRetryCount() = mutex.withLock {
        val currentRetries = prefs.getInt(KEY_MIGRATION_RETRIES, 0)
        prefs.edit {
            putInt(KEY_MIGRATION_RETRIES, currentRetries + 1)
        }
    }

    suspend fun resetRetryCount() = mutex.withLock {
        prefs.edit {
            remove(KEY_MIGRATION_RETRIES)
        }
    }

    suspend fun canRetry(): Boolean = mutex.withLock {
        prefs.getInt(KEY_MIGRATION_RETRIES, 0) <
            configRepo.getProjectConfiguration().experimental().recordsDbMigrationFromRealmMaxRetries
    }

    suspend fun isMigrationGloballyEnabled(): Boolean = mutex.withLock {
        configRepo.getProjectConfiguration().experimental().recordsDbMigrationFromRealmEnabled
    }

    suspend fun isDownSyncInProgress(): Boolean = mutex.withLock {
        prefs.getBoolean(KEY_DOWN_SYNC_STATUS, false)
    }

    suspend fun setDownSyncInProgress(isInProgress: Boolean) = mutex.withLock {
        prefs.edit {
            putBoolean(KEY_DOWN_SYNC_STATUS, isInProgress)
        }
        Simber.i("[RealmToRoomMigrationFlagsStore] Down sync status updated to: $isInProgress", tag = REALM_DB_MIGRATION)
    }

    /**
     * Returns a string representation of the current migration-related key-value pairs.
     */
    suspend fun getStoreStateAsString(): String = mutex.withLock {
        val status = prefs.getString(KEY_MIGRATION_STATUS, MigrationStatus.NOT_STARTED.name)
        val retries = prefs.getInt(KEY_MIGRATION_RETRIES, 0)
        val downSync = prefs.getBoolean(KEY_DOWN_SYNC_STATUS, false)

        """
        $KEY_MIGRATION_STATUS: $status
        $KEY_MIGRATION_RETRIES: $retries
        $KEY_DOWN_SYNC_STATUS: $downSync
        """.trimIndent()
    }

    /**
     * Clears all migration-related keys from the store.
     */
    fun clearMigrationFlags() {
        prefs.edit { clear() }
        Simber.i("[RealmToRoomMigrationFlagsStore] Migration flags cleared", tag = REALM_DB_MIGRATION)
    }
}
