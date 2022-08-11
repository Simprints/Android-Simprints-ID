package com.simprints.infra.config.local.migrations.realm

import android.content.Context
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import com.simprints.infra.security.keyprovider.LocalDbKey
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProvider
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RealmWrapperImpl @Inject constructor(
    private val appContext: Context,
    private val secureDataManager: SecureLocalDbKeyProvider,
    private val loginManager: LoginManager,
) {
    /**
     * Use realm instance in from IO threads
     * throws RealmUninitialisedException
     */
    suspend fun <R> useRealmInstance(block: (Realm) -> R): R =
        withContext(Dispatchers.IO) {
            Simber.i("Use realm")
            val realm = Realm.getInstance(config)
            realm.use(block)
        }

    private val config: RealmConfiguration by lazy {
        Realm.init(appContext)
        createAndSaveRealmConfig(getLocalDbKey())
    }

    private fun createAndSaveRealmConfig(localDbKey: LocalDbKey): RealmConfiguration =
        RealmConfig.get(localDbKey.projectId, localDbKey.value)

    private fun getLocalDbKey(): LocalDbKey =
        loginManager.getSignedInProjectIdOrEmpty().let {
            secureDataManager.getLocalDbKeyOrThrow(it)
        }
}
