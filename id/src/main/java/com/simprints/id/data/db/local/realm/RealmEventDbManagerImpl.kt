package com.simprints.id.data.db.local.realm

import android.content.Context
import com.simprints.id.data.analytics.SessionEvents
import com.simprints.id.data.db.local.LocalEventDbManager
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.exceptions.unsafe.RealmUninitialisedError
import io.reactivex.Completable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration

class RealmEventDbManagerImpl(private val appContext: Context) : LocalEventDbManager {

    private var realmConfig: RealmConfiguration? = null
    private var localDbKey: LocalDbKey? = null
        set(value) {
            field = value
            value?.let { createAndSaveRealmConfig(value) }
        }

    init {
        Realm.init(appContext)
    }

    private fun createAndSaveRealmConfig(localDbKey: LocalDbKey): Single<RealmConfiguration> =
        Single.just(RealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)
            .also { realmConfig = it })

    override fun initDb(localDbKey: LocalDbKey) {
        this.localDbKey = localDbKey
        EncryptionMigration(localDbKey, appContext)
        getRealmInstance().map { realm -> realm.use { } }.toCompletable()
    }

    private fun getRealmConfig(): Single<RealmConfiguration> = realmConfig?.let {
        Single.just(it)
    } ?: throw RealmUninitialisedError("No valid realm Config")

    private fun getRealmInstance(): Single<Realm> = getRealmConfig()
        .flatMap {
            Single.just(Realm.getInstance(it))
        }

    override fun insertOrUpdateSessionEvents(sessionEvents: SessionEvents): Completable =
            getRealmInstance().map { realm ->
                realm.executeTransaction {
                    it.insertOrUpdate(sessionEvents)
                }
            }.toCompletable()
}
