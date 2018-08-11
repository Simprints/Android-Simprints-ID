package com.simprints.id.data.analytics.events.realm

import android.content.Context
import com.simprints.id.data.analytics.events.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.events.models.SessionEvents
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.EncryptionMigration
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.exceptions.unsafe.RealmUninitialisedError
import io.reactivex.Completable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration

class RealmSessionEventsDbManagerImpl(private val appContext: Context) : SessionEventsLocalDbManager {

    companion object {
        const val PROJECT_ID = "projectId"
        const val END_TIME = "relativeEndTime"
        const val START_TIME = "startTime"
    }

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
                it.insertOrUpdate(RlSessionEvents(sessionEvents))
            }
        }.toCompletable()

    override fun loadSessions(projectId: String): Single<ArrayList<SessionEvents>> =
        getRealmInstance().map {
            val query = it.where(RlSessionEvents::class.java).apply {
                this.equalTo(RealmSessionEventsDbManagerImpl.PROJECT_ID, projectId)
            }
            ArrayList(it.copyFromRealm(query.findAll(), 4).map { SessionEvents(it) })
        }

    override fun loadLastOpenSession(projectId: String): Single<SessionEvents> =
        getRealmInstance().map {
            val query = it.where(RlSessionEvents::class.java).apply {
                this.equalTo(RealmSessionEventsDbManagerImpl.PROJECT_ID, projectId)
                this.equalTo(RealmSessionEventsDbManagerImpl.END_TIME, 0L)
                this.sort(RealmSessionEventsDbManagerImpl.START_TIME, io.realm.Sort.DESCENDING)
            }
            val rlSessionEvents = it.copyFromRealm(query.findFirst(), 4)
            SessionEvents(rlSessionEvents!!)
        }

    override fun deleteSessions(projectId: String): Completable {
        getRealmInstance().map {realm ->
            realm.executeTransaction {
                val query = it.where(RlSessionEvents::class.java).apply {
                    this.equalTo(RealmSessionEventsDbManagerImpl.PROJECT_ID, projectId)
                }
                query.findAll().deleteAllFromRealm()
            }
        }
        return Completable.complete()
    }
}
