package com.simprints.id.data.analytics.eventData.realm

import android.content.Context
import com.simprints.id.data.analytics.eventData.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventData.models.session.SessionEvents
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.EncryptionMigration
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.exceptions.unsafe.RealmUninitialisedError
import io.reactivex.Completable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmQuery

class RealmSessionEventsDbManagerImpl(private val appContext: Context,
                                      private val secureDataManager: SecureDataManager) : SessionEventsLocalDbManager {

    companion object {
        const val PROJECT_ID = "projectId"
        const val END_TIME = "relativeEndTime"
        const val START_TIME = "startTime"
    }

    private var realmConfig: RealmConfiguration? = null
    private var localDbKey: LocalDbKey? = null

    private fun initDbIfRequired(): Completable {
        try {
            if (this.localDbKey == null) {
                Realm.init(appContext)
                val localKey = generateDbKeyIfRequired().also { this.localDbKey = it }
                EncryptionMigration(localKey, appContext)
                createAndSaveRealmConfig(localKey)
            }
        } catch (e: Exception) {
            return Completable.error(e)
        }

        return Completable.complete()
    }

    override fun insertOrUpdateSessionEvents(sessionEvents: SessionEvents): Completable =
        getRealmInstance().flatMapCompletable { realm ->
            realm.executeTransaction {
                it.insertOrUpdate(RlSessionEvents(sessionEvents))
            }
            Completable.complete()
        }

    override fun loadSessions(projectId: String?, openSession: Boolean?): Single<ArrayList<SessionEvents>> =
        getRealmInstance().map {
            val query = it.where(RlSessionEvents::class.java).apply {
                addQueryParamForProjectId(projectId, this)
                addQueryParamForOpenSession(openSession, this)

                this.sort(RealmSessionEventsDbManagerImpl.START_TIME, io.realm.Sort.DESCENDING)
            }
            ArrayList(it.copyFromRealm(query.findAll(), 4).map { SessionEvents(it) })
        }

    override fun deleteSessions(projectId: String?, openSession: Boolean?): Completable =
        getRealmInstance().flatMapCompletable { realm ->
            realm.executeTransaction {
                val query = it.where(RlSessionEvents::class.java).apply {
                    addQueryParamForProjectId(projectId, this)
                    addQueryParamForOpenSession(openSession, this)
                }
                query.findAll().deleteAllFromRealm()
            }
            Completable.complete()
        }

    private fun generateDbKeyIfRequired(): LocalDbKey {
        try {
            secureDataManager.getLocalDbKeyOrThrow("event_data")
        } catch (e: Exception) {
            secureDataManager.setLocalDatabaseKey("event_data", null)
        }
        return secureDataManager.getLocalDbKeyOrThrow("event_data")
    }

    private fun createAndSaveRealmConfig(localDbKey: LocalDbKey): Single<RealmConfiguration> =
        Single.just(RealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)
            .also { realmConfig = it })

    private fun getRealmConfig(): Single<RealmConfiguration> = realmConfig?.let {
        Single.just(it)
    } ?: throw RealmUninitialisedError("No valid realm Config")

    private fun getRealmInstance(): Single<Realm> = initDbIfRequired().andThen(getRealmConfig()
        .flatMap {
            Single.just(Realm.getInstance(it))
        })

    private fun addQueryParamForProjectId(projectId: String?, query: RealmQuery<RlSessionEvents>) {
        projectId?.let {
            query.equalTo(RealmSessionEventsDbManagerImpl.PROJECT_ID, projectId)
        }
    }

    private fun addQueryParamForOpenSession(openSession: Boolean?, query: RealmQuery<RlSessionEvents>) {
        openSession?.let {
            if (it) {
                query.equalTo(RealmSessionEventsDbManagerImpl.END_TIME, 0L)
            } else {
                query.greaterThan(RealmSessionEventsDbManagerImpl.END_TIME, 0L)
            }
        }
    }
}
