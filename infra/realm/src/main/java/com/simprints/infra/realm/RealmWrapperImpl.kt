package com.simprints.infra.realm

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.DB_CORRUPTION
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.REALM_DB
import com.simprints.infra.logging.Simber
import com.simprints.infra.realm.config.RealmConfig
import com.simprints.infra.realm.exceptions.RealmUninitialisedException
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.keyprovider.LocalDbKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.exceptions.RealmException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealmWrapperImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val configFactory: RealmConfig,
    private val securityManager: SecurityManager,
    private val authStore: AuthStore,
) : RealmWrapper {

    private lateinit var config: RealmConfiguration

    private fun initRealm() {
        if (!this::config.isInitialized) {
            config = createAndSaveRealmConfig()
        }
    }

    private fun getRealm(): Realm {
        initRealm()
        Simber.tag(REALM_DB.name).d("[RealmWrapperImpl] getting new realm instance")
        return try {
            try {
                Realm.open(config)
            } catch (ex: IllegalStateException) {
                // On the schema update realm it is not being closed correctly and there
                // is no legitimate way to forcefully restart the realm instance,
                // so we need to catch this exception and try opening realm again.
                // If the exception repeats, it should be propagated up the call stack.
                Realm.open(config)
            }
        } catch (ex: RealmException) {
            //DB corruption detected; either DB file or key is corrupt
            //1. Delete DB file in order to create a new one at next init
            Realm.deleteRealm(config)
            //2. Recreate the DB key
            recreateLocalDbKey()
            //3. Log exception after recreating the key so we get extra info
            Simber.tag(DB_CORRUPTION.name).e(ex)
            //4. Update Realm config with the new key
            config = createAndSaveRealmConfig()
            //5. Delete "last sync" info and start new sync
            resetDownSyncState()
            //6. Retry operation with new file and key
            Realm.open(config)
        }
    }

    /**
     * Executes provided block ensuring a valid Realm instance is used and closed.
     */
    override suspend fun <R> readRealm(block: (Realm) -> R): R {
        val realm = getRealm()
        val result = block(realm)
        realm.close()
        return result
    }

    /**
     * Executes provided block in a transaction ensuring a valid Realm instance is used and closed.
     */
    override suspend fun <R> writeRealm(block: (MutableRealm) -> R) {
        val realm = getRealm()
        realm.write(block)
        realm.close()
    }

    private fun createAndSaveRealmConfig(): RealmConfiguration {
        val localDbKey = getLocalDbKey()
        return configFactory.get(localDbKey.projectId, localDbKey.value)
    }

    private fun getLocalDbKey(): LocalDbKey =
        authStore.signedInProjectId.let {
            return if (it.isNotEmpty()) {
                securityManager.getLocalDbKeyOrThrow(it)
            } else {
                throw RealmUninitialisedException("No signed in project id found")
            }
        }

    private fun recreateLocalDbKey() =
        authStore.signedInProjectId.let {
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
            "com.simprints.id.services.sync.events.down.EventDownSyncResetService"
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(intent)
            } else {
                appContext.startService(intent)
            }
        } catch (ex: Exception) {
            Simber.e(ex)
        }
    }
}
