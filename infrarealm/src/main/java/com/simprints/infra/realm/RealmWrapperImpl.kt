package com.simprints.infra.realm

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.*
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import com.simprints.infra.realm.config.RealmConfig
import com.simprints.infra.realm.exceptions.RealmUninitialisedException
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.keyprovider.LocalDbKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.exceptions.RealmFileException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealmWrapperImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val securityManager: SecurityManager,
    private val loginManager: LoginManager
) : RealmWrapper {

    private var counter = 0

    private lateinit var config: RealmConfiguration

    private fun initRealm() {
        if (!this::config.isInitialized) {
            Realm.init(appContext)
            config = createAndSaveRealmConfig()
        }
    }

    /**
     * Use realm instance in from IO threads
     * throws RealmUninitialisedException
     */
    override suspend fun <R> useRealmInstance(block: (Realm) -> R): R =
        withContext(Dispatchers.IO) {
            counter++
            initRealm()
            Simber.tag(REALM_DB.name).d("[RealmWrapperImpl] getting new realm instance")
            try {
                if (counter == 1) {
                    throw RealmFileException(RealmFileException.Kind.ACCESS_ERROR, "123")
                }
                Realm.getInstance(config).use(block)
            } catch (ex: RealmFileException) {
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
                Realm.getInstance(config).use(block)
            }
        }

    private fun createAndSaveRealmConfig(): RealmConfiguration {
        val localDbKey = getLocalDbKey()
        return RealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)
    }

    private fun getLocalDbKey(): LocalDbKey =
        loginManager.getSignedInProjectIdOrEmpty().let {
            return if (it.isNotEmpty()) {
                securityManager.getLocalDbKeyOrThrow(it)
            } else {
                throw RealmUninitialisedException("No signed in project id found")
            }
        }

    private fun recreateLocalDbKey() =
        loginManager.getSignedInProjectIdOrEmpty().let {
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
            "com.simprints.infra.events.events_sync.down.temp.ResetDownSyncService"
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
