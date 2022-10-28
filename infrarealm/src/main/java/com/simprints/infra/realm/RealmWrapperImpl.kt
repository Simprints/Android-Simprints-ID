package com.simprints.infra.realm

import android.content.Context
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import com.simprints.infra.realm.config.RealmConfig
import com.simprints.infra.realm.exceptions.RealmUninitialisedException
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.keyprovider.LocalDbKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RealmWrapperImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val secureDataManager: SecurityManager,
    private val loginManager: LoginManager,
) :
    RealmWrapper {
    /**
     * Use realm instance in from IO threads
     * throws RealmUninitialisedException
     */
    override suspend fun <R> useRealmInstance(block: (Realm) -> R): R =
        withContext(Dispatchers.IO) {
            Simber.tag(LoggingConstants.CrashReportTag.REALM_DB.name)
                .d("[RealmWrapperImpl] getting new realm instance")
            Realm.getInstance(config).use(block)
        }

    private val config: RealmConfiguration by lazy {
        Realm.init(appContext)
        createAndSaveRealmConfig(getLocalDbKey())
    }

    private fun createAndSaveRealmConfig(localDbKey: LocalDbKey): RealmConfiguration =
        RealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)

    private fun getLocalDbKey(): LocalDbKey =
        loginManager.getSignedInProjectIdOrEmpty().let {
            return if (it.isNotEmpty()) {
                secureDataManager.getLocalDbKeyOrThrow(it)
            } else {
                throw RealmUninitialisedException("No signed in project id found")
            }
        }
}
