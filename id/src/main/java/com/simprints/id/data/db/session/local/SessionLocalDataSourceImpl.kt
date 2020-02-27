package com.simprints.id.data.db.session.local

import android.content.Context
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.local.SessionLocalDataSource.Query
import com.simprints.id.data.db.session.local.models.DbEvent
import com.simprints.id.data.db.session.local.models.DbSession
import com.simprints.id.data.db.session.local.models.toDomain
import com.simprints.id.data.secure.LocalDbKey
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.exceptions.safe.secure.MissingLocalDatabaseKeyException
import com.simprints.id.exceptions.unexpected.RealmUninitialisedException
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.await
import com.simprints.id.tools.extensions.transactAwait
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow

open class SessionLocalDataSourceImpl(private val appContext: Context,
                                      private val secureDataManager: SecureLocalDbKeyProvider,
                                      private val timeHelper: TimeHelper) : SessionLocalDataSource {
    companion object {
        const val PROJECT_ID = "projectId"
        const val END_TIME = "relativeEndTime"
        const val START_TIME = "startTime"
        const val SESSION_ID = "id"

        const val SESSIONS_REALM_DB_FILE_NAME = "event_data"
    }

    private var realmConfig: RealmConfiguration? = null
    private var localDbKey: LocalDbKey? = null
    private val realm: Realm
        get() = getRealmInstance()

    override suspend fun create(sessionEvents: SessionEvents) {
        realm.transactAwait {
            it.insert(DbSession(sessionEvents))
        }
    }

    override suspend fun addEvent(sessionId: String, events: List<Event>) {
        realm.transactAwait {
            val session = addQueryParams(Query(id = sessionId)).findFirst()
            session?.realmEvents?.addAll(events.map { DbEvent(it) })
        }
    }

    override suspend fun load(query: Query): Flow<SessionEvents> =
        addQueryParams(query).await()?.map { it.toDomain() }?.asFlow() ?: emptyFlow()

    override suspend fun count(query: Query): Int =
        addQueryParams(query).await()?.count() ?: 0

    override suspend fun delete(query: Query) {
        realm.transactAwait {
            val sessions = addQueryParams(query).findAll()
            sessions?.deleteAllFromRealm()
        }
    }

    override suspend fun closeSession(sessionId: String) {
        realm.transactAwait {
            val session = addQueryParams(Query(id = sessionId)).findFirst()
            session?.let {
                val isSessionClose = it.relativeEndTime > 0
                if (!isSessionClose) {
                    it.relativeEndTime = it.timeRelativeToStartTime(timeHelper.now())
                }
            }
        }
    }


    private fun addQueryParams(query: Query): RealmQuery<DbSession> =
        realm.where(DbSession::class.java).apply {
            addQueryParamForProjectId(query.projectId, this)
            addQueryParamForOpenSession(query.openSession, this)
            addQueryParamForSessionId(query.id, this)
            addQueryParamForStartTime(query.startedBefore, this)
        }

    private fun addQueryParamForProjectId(projectId: String?, query: RealmQuery<DbSession>) {
        projectId?.let {
            query.equalTo(PROJECT_ID, projectId)
        }
    }

    private fun addQueryParamForSessionId(sessionId: String?, query: RealmQuery<DbSession>) {
        sessionId?.let {
            query.equalTo(SESSION_ID, sessionId)
        }
    }

    private fun addQueryParamForStartTime(startedBefore: Long?, query: RealmQuery<DbSession>) {
        startedBefore?.let {
            query.greaterThan(START_TIME, startedBefore).not()
        }
    }

    private fun addQueryParamForOpenSession(openSession: Boolean?, query: RealmQuery<DbSession>) {
        openSession?.let {
            if (it) {
                query.equalTo(END_TIME, 0L)
            } else {
                query.greaterThan(END_TIME, 0L)
            }
        }
    }

    fun getRealmInstance(): Realm {
        initDbIfRequired()
        return realmConfig?.let {
            Realm.getInstance(it)
        } ?: throw RealmUninitialisedException("No valid realm Config")
    }

    private fun initDbIfRequired() {
        if (this.localDbKey == null || realmConfig == null) {
            Realm.init(appContext)
            val localKey = generateDbKeyIfRequired()
            realmConfig = SessionRealmConfig.get(localKey.projectId, localKey.value)
            this.localDbKey = localKey
        }
    }

    private fun generateDbKeyIfRequired(): LocalDbKey {
        try {
            secureDataManager.getLocalDbKeyOrThrow(SESSIONS_REALM_DB_FILE_NAME)
        } catch (e: MissingLocalDatabaseKeyException) {
            secureDataManager.setLocalDatabaseKey(SESSIONS_REALM_DB_FILE_NAME)
        }
        return secureDataManager.getLocalDbKeyOrThrow(SESSIONS_REALM_DB_FILE_NAME)
    }

    override suspend fun insertOrUpdateSessionEvents(sessionEvents: SessionEvents) {
        realm.transactAwait {
            it.insertOrUpdate(DbSession(sessionEvents))
        }
    }
}
