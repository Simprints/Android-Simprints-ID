package com.simprints.infra.realm

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.REALM_DB
import com.simprints.infra.logging.Simber
import com.simprints.infra.realm.exceptions.RealmUninitialisedException
import com.simprints.infra.realm.models.MyObjectBox
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.keyprovider.LocalDbKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.objectbox.BoxStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObjectboxWrapperImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val securityManager: SecurityManager,
    private val authStore: AuthStore,
) : ObjectboxWrapper {
    private var boxStore: BoxStore

    init {

        val builder = MyObjectBox.builder().androidContext(appContext)

        if (BuildConfig.DB_ENCRYPTION) {
            //   builder.encryptionKey(encryptionKey.value) only available in objectbox  commercial version
        }

        boxStore = builder.build()
    }

    override suspend fun <R> readObjectBox(block: (BoxStore) -> R): R = block(boxStore)

    override suspend fun <R> writeObjectBox(block: (BoxStore) -> R) {
        boxStore.runInTx { block(boxStore) }
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
