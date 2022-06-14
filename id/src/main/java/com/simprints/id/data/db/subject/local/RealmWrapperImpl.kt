package com.simprints.id.data.db.subject.local

import android.content.Context
import com.simprints.core.security.LocalDbKey
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.data.db.subject.migration.SubjectsRealmConfig
import com.simprints.id.exceptions.unexpected.RealmUninitialisedException
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.withContext

class RealmWrapperImpl(
    private val appContext: Context,
    private val localKey: LocalDbKey?,
    private val dispatcher: DispatcherProvider,
) :
    RealmWrapper {
    /**
     * Use realm instance in from IO threads
     *
     */
    override suspend fun <R> useRealmInstance(block: (Realm) -> R): R =
        withContext(dispatcher.io()) { Realm.getInstance(config).use(block) }

    private val config: RealmConfiguration by lazy {
        if (localKey == null) {
            throw RealmUninitialisedException("No signed in project id found")
        }
        Realm.init(appContext)
        createAndSaveRealmConfig(localKey)
    }

    private fun createAndSaveRealmConfig(localDbKey: LocalDbKey): RealmConfiguration =
        SubjectsRealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)
}
