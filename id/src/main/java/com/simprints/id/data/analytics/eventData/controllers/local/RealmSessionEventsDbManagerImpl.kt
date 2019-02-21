package com.simprints.id.data.analytics.eventData.controllers.local

import android.content.Context
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.data.analytics.eventData.models.local.RlSession
import com.simprints.id.data.analytics.eventData.models.local.toDomainSession
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.exceptions.safe.secure.MissingLocalDatabaseKeyException
import com.simprints.id.exceptions.unexpected.RealmUninitialisedException
import com.simprints.id.exceptions.unexpected.SessionNotFoundException
import io.reactivex.Completable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmQuery
import io.realm.Sort
import timber.log.Timber

open class RealmSessionEventsDbManagerImpl(private val appContext: Context,
                                           private val secureDataManager: SecureDataManager) : SessionEventsLocalDbManager {

    companion object {
        const val PROJECT_ID = "projectId"
        const val END_TIME = "relativeEndTime"
        const val START_TIME = "startTime"
        const val SESSION_ID = "id"

        const val SESSIONS_REALM_DB_FILE_NAME = "event_data"
    }

    private var realmConfig: RealmConfiguration? = null
    private var localDbKey: LocalDbKey? = null

    fun getRealmInstance(): Single<Realm> =
        initDbIfRequired().toSingle {
            realmConfig?.let {
                Realm.getInstance(it)
            } ?: throw RealmUninitialisedException("No valid realm Config")
        }

    private fun initDbIfRequired(): Completable {
        try {
            if (this.localDbKey == null || realmConfig == null) {
                Realm.init(appContext)
                val localKey = generateDbKeyIfRequired().also { this.localDbKey = it }
                createAndSaveRealmConfig(localKey)
            }
        } catch (e: Exception) {
            return Completable.error(e)
        }

        return Completable.complete()
    }

    override fun insertOrUpdateSessionEvents(sessionEvents: SessionEvents): Completable =
        getRealmInstance().flatMapCompletable {
            it.use { realm ->
                realm.executeTransaction { realmInTrans ->
                    realmInTrans.insertOrUpdate(RlSession(sessionEvents))
                }
                Completable.complete()
            }
        }

    override fun loadSessions(projectId: String?, openSession: Boolean?): Single<ArrayList<SessionEvents>> =
        useRealmInstance {
            val query = it.where(RlSession::class.java).apply {
                addQueryParamForProjectId(projectId, this)
                addQueryParamForOpenSession(openSession, this)

                this.sort(RealmSessionEventsDbManagerImpl.START_TIME, Sort.DESCENDING)
            }
            ArrayList(it.copyFromRealm(query.findAll()).map { session -> session.toDomainSession() })
        }

    /** @throws SessionNotFoundException */
    override fun loadSessionById(sessionId: String): Single<SessionEvents> =
        useRealmInstance {
            val query = it.where(RlSession::class.java).apply {
                equalTo(RealmSessionEventsDbManagerImpl.SESSION_ID, sessionId)
            }
            query.findFirst()?.toDomainSession() ?: throw SessionNotFoundException()
        }

    override fun getSessionCount(projectId: String?): Single<Int> =
        useRealmInstance {
            it.where(RlSession::class.java).apply {
                addQueryParamForProjectId(projectId, this)
            }.count().toInt()
        }

    override fun deleteSessions(projectId: String?,
                                sessionId: String?,
                                openSession: Boolean?,
                                startedBefore: Long?): Completable =
        getRealmInstance().flatMapCompletable { realm ->
            realm.use {
                it.executeTransaction { realmInTrans ->
                    val sessions = realmInTrans.where(RlSession::class.java).apply {
                        addQueryParamForProjectId(projectId, this)
                        addQueryParamForOpenSession(openSession, this)
                        addQueryParamForSessionId(sessionId, this)
                        addQueryParamForStartTime(startedBefore, this)
                    }.findAll()

                    sessions.forEach { session ->
                        Timber.d("Deleting session: ${session.id}")
                        deleteSessionInfo(session)
                    }
                    sessions.deleteAllFromRealm()
                }
                Completable.complete()
            }
        }

    private fun deleteSessionInfo(session: RlSession) {
        session.databaseInfo?.deleteFromRealm()
        session.device?.deleteFromRealm()
        session.location?.deleteFromRealm()
        session.realmEvents.deleteAllFromRealm()
    }

    private fun generateDbKeyIfRequired(): LocalDbKey {
        try {
            secureDataManager.getLocalDbKeyOrThrow(SESSIONS_REALM_DB_FILE_NAME)
        } catch (e: MissingLocalDatabaseKeyException) {
            secureDataManager.setLocalDatabaseKey(SESSIONS_REALM_DB_FILE_NAME, null)
        }
        return secureDataManager.getLocalDbKeyOrThrow(SESSIONS_REALM_DB_FILE_NAME)
    }

    private fun createAndSaveRealmConfig(localDbKey: LocalDbKey): Single<RealmConfiguration> =
        Single.just(SessionRealmConfig.get(localDbKey.projectId, localDbKey.value)
            .also { realmConfig = it })

    private fun addQueryParamForProjectId(projectId: String?, query: RealmQuery<RlSession>) {
        projectId?.let {
            query.equalTo(RealmSessionEventsDbManagerImpl.PROJECT_ID, projectId)
        }
    }

    private fun addQueryParamForSessionId(sessionId: String?, query: RealmQuery<RlSession>) {
        sessionId?.let {
            query.equalTo(RealmSessionEventsDbManagerImpl.SESSION_ID, sessionId)
        }
    }

    private fun addQueryParamForStartTime(startedBefore: Long?, query: RealmQuery<RlSession>) {
        startedBefore?.let {
            query.greaterThan(RealmSessionEventsDbManagerImpl.START_TIME, startedBefore).not()
        }
    }

    private fun addQueryParamForOpenSession(openSession: Boolean?, query: RealmQuery<RlSession>) {
        openSession?.let {
            if (it) {
                query.equalTo(RealmSessionEventsDbManagerImpl.END_TIME, 0L)
            } else {
                query.greaterThan(RealmSessionEventsDbManagerImpl.END_TIME, 0L)
            }
        }
    }

    private fun <R> useRealmInstance(block: (Realm) -> R): Single<R> =
        getRealmInstance()
            .map { realm ->
                realm.use(block)
            }
}
