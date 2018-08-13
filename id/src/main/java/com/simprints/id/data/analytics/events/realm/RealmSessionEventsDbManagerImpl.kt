package com.simprints.id.data.analytics.events.realm

import android.content.Context
import com.simprints.id.data.analytics.events.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.events.models.SessionEvents
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.EncryptionMigration
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.exceptions.unsafe.RealmUninitialisedError
import io.reactivex.Completable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration

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
                projectId?.let {
                    this.equalTo(RealmSessionEventsDbManagerImpl.PROJECT_ID, projectId)
                }

                openSession?.let { openSession ->
                    if (openSession) {
                        this.equalTo(RealmSessionEventsDbManagerImpl.END_TIME, 0L)
                    } else {
                        this.greaterThan(RealmSessionEventsDbManagerImpl.END_TIME, 0L)
                    }
                }

                this.sort(RealmSessionEventsDbManagerImpl.START_TIME, io.realm.Sort.DESCENDING)
            }
            ArrayList(it.copyFromRealm(query.findAll(), 4).map { SessionEvents(it) })
        }

    override fun deleteSessions(projectId: String?): Completable =
        getRealmInstance().flatMapCompletable { realm ->
            realm.executeTransaction {
                val query = it.where(RlSessionEvents::class.java).apply {
                    projectId?.let {
                        this.equalTo(RealmSessionEventsDbManagerImpl.PROJECT_ID, projectId)
                    }
                }
                query.findAll().deleteAllFromRealm()
            }
            Completable.complete()
        }
}
