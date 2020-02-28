package com.simprints.id.data.db.session.local

import android.content.Context
import android.os.Build
import com.simprints.id.data.db.session.SessionRepositoryImpl
import com.simprints.id.data.db.session.domain.models.events.ArtificialTerminationEvent
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.session.domain.models.session.Device
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.local.SessionLocalDataSource.Query
import com.simprints.id.data.db.session.local.models.DbSession
import com.simprints.id.data.db.session.local.models.toDomain
import com.simprints.id.data.secure.LocalDbKey
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.exceptions.safe.secure.MissingLocalDatabaseKeyException
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.unexpected.RealmUninitialisedException
import com.simprints.id.tools.TimeHelper
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    override suspend fun create(appVersionName: String,
                                libSimprintsVersionName: String,
                                language: String,
                                deviceId: String) {
        withContext(Dispatchers.IO) {
            realm.executeTransaction { reamInTrans ->

                val currentSession = addQueryParams(Query(openSession = true)).findAll()
                currentSession.forEach {
                    val updatedSession = it.toDomain()
                    val artificialTerminationEvent = ArtificialTerminationEvent(timeHelper.now(), ArtificialTerminationEvent.Reason.NEW_SESSION)
                    updatedSession.events.add(artificialTerminationEvent)
                    updatedSession.closeIfRequired(timeHelper)

                    reamInTrans.insertOrUpdate(DbSession(updatedSession))
                }

                val count = addQueryParams(Query()).count().toInt()

                val session = SessionEvents(
                    SessionRepositoryImpl.PROJECT_ID_FOR_NOT_SIGNED_IN,
                    appVersionName,
                    libSimprintsVersionName,
                    language,
                    Device(
                        Build.VERSION.SDK_INT.toString(),
                        Build.MANUFACTURER + "_" + Build.MODEL,
                        deviceId),
                    timeHelper.now(),
                    DatabaseInfo(count))

                val dbSession = DbSession(session)
                realm.insert(dbSession)
            }
        }
    }

    override suspend fun load(query: Query): Flow<SessionEvents> =
        withContext(Dispatchers.IO) {
            addQueryParams(query).findAll()?.map { it.toDomain() }?.asFlow() ?: emptyFlow()
        }

    override suspend fun count(query: Query): Int =
        withContext(Dispatchers.IO) {
            addQueryParams(query).findAll()?.count() ?: 0
        }

    override suspend fun delete(query: Query) {
        withContext(Dispatchers.IO) {
            realm.executeTransaction {
                val sessions = addQueryParams(query).findAll()
                sessions?.deleteAllFromRealm()
            }
        }
    }

    override suspend fun updateCurrentSession(update: (SessionEvents) -> Unit) {
        withContext(Dispatchers.IO) {
            realm.refresh()
            realm.executeTransaction {
                val session = addQueryParams(Query(openSession = true)).findFirst() ?: throw NoSessionsFoundException()
                val domainSession = session.toDomain()
                update(domainSession)
                it.insertOrUpdate(DbSession(domainSession))
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

    private fun getRealmInstance(): Realm {
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

    override fun addEventInBackground(event: Event) {
        CoroutineScope(Dispatchers.IO).launch {
            updateCurrentSession {
                it.events.add(event)
            }
        }
    }

    override suspend fun insertOrUpdateSessionEvents(sessionEvents: SessionEvents) {
        withContext(Dispatchers.IO) {

            realm.executeTransaction {
                it.insertOrUpdate(DbSession(sessionEvents))
            }
        }
    }
}
