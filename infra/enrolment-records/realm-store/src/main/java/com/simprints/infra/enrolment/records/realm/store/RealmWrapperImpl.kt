package com.simprints.infra.enrolment.records.realm.store

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.simprints.core.DispatcherIO
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.enrolment.records.realm.store.config.RealmConfig
import com.simprints.infra.enrolment.records.realm.store.exceptions.RealmUninitialisedException
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.DB_CORRUPTION
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.REALM_DB
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.keyprovider.LocalDbKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealmWrapperImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val configFactory: RealmConfig,
    private val securityManager: SecurityManager,
    private val authStore: AuthStore,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : RealmWrapper {
    // Kotlin-realm claims to be thread-safe and there is no need to handle closing manually
    // https://www.mongodb.com/docs/realm/sdk/kotlin/realm-database/frozen-arch/#thread-safe-realms
    private lateinit var realm: Realm
    private lateinit var config: RealmConfiguration

    private fun getRealm(): Realm {
        if (!this::realm.isInitialized) {
            config = createAndSaveRealmConfig()
            realm = createRealm()
        }
        return realm
    }

    private fun createRealm(): Realm {
        Simber.d("[RealmWrapperImpl] getting new realm instance", tag = REALM_DB)
        return try {
            try {
                Realm.open(config)
            } catch (ex: IllegalStateException) {
                // On the schema update realm it is not being closed correctly and there
                // is no legitimate way to forcefully restart the realm instance,
                // so we need to catch this exception and try opening realm again.
                // If the exception repeats, it should be propagated up the call stack.
                if (!isFileCorruptException(ex)) {
                    Realm.open(config)
                } else {
                    throw ex
                }
            }
        } catch (ex: Exception) {
            if (isFileCorruptException(ex)) {
                // DB corruption detected; either DB file or key is corrupt
                // 1. Delete DB file in order to create a new one at next init
                Realm.deleteRealm(config)
                // 2. Recreate the DB key
                recreateLocalDbKey()
                // 3. Log exception after recreating the key so we get extra info
                Simber.e("Realm DB recreated due to corruption", ex, tag = DB_CORRUPTION)
                // 4. Update Realm config with the new key
                config = createAndSaveRealmConfig()
                // 5. Delete "last sync" info and start new sync
                resetDownSyncState()
                // 6. Retry operation with new file and key
                Realm.open(config)
            } else {
                throw ex
            }
        }
    }

    private fun isFileCorruptException(ex: Exception) = ex is IllegalStateException &&
        ex.message?.contains("RLM_ERR_INVALID_DATABASE") == true

    /**
     * Executes provided block ensuring a valid Realm instance is used and closed.
     */
    override suspend fun <R> readRealm(block: (Realm) -> R): R = withContext(dispatcher) { block(getRealm()) }

    /**
     * Executes provided block in a transaction ensuring a valid Realm instance is used and closed.
     */
    override suspend fun <R> writeRealm(block: (MutableRealm) -> R) {
        withContext(dispatcher) { getRealm().write(block) }
    }

    private fun createAndSaveRealmConfig(): RealmConfiguration {
        val localDbKey = getLocalDbKey()
        return configFactory.get(localDbKey.projectId, localDbKey.value)
    }

    private fun getLocalDbKey(): LocalDbKey = authStore.signedInProjectId.let {
        return if (it.isNotEmpty()) {
            try {
                securityManager.getLocalDbKeyOrThrow(it)
            } catch (ex: Exception) {
                Simber.e("Failed to fetch local DB key", ex, tag = REALM_DB)
                securityManager.recreateLocalDatabaseKey(it)
                securityManager.getLocalDbKeyOrThrow(it)
            }
        } else {
            throw RealmUninitialisedException("No signed in project id found")
        }
    }

    private fun recreateLocalDbKey() = authStore.signedInProjectId.let {
        if (it.isNotEmpty()) {
            securityManager.recreateLocalDatabaseKey(it)
        } else {
            throw RealmUninitialisedException("No signed in project id found")
        }
    }

    private fun resetDownSyncState() {
        // This is a workaround to avoid a circular module dependency
        val intent = Intent()
        intent.component = ComponentName(
            "com.simprints.id",
            "com.simprints.id.services.sync.events.down.EventDownSyncResetService",
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(intent)
            } else {
                appContext.startService(intent)
            }
        } catch (ex: Exception) {
            Simber.e("Unable to start sync reset service", ex, tag = REALM_DB)
        }
    }
}
