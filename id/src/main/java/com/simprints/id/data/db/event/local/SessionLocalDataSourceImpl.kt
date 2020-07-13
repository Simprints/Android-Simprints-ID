package com.simprints.id.data.db.event.local

import android.content.Context
import android.os.Build
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.id.data.db.event.SessionRepositoryImpl
import com.simprints.id.data.db.event.domain.events.*
import com.simprints.id.data.db.event.domain.events.Event.EventLabel.SessionId
import com.simprints.id.data.db.event.domain.events.EventQuery.SessionCaptureEventQuery
import com.simprints.id.data.db.event.domain.events.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.events.session.Device
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.validators.SessionEventValidator
import com.simprints.id.data.db.event.local.models.DbEvent
import com.simprints.id.data.db.event.local.models.DbSession
import com.simprints.id.data.db.event.local.models.toDomain
import com.simprints.id.data.secure.LocalDbKey
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.exceptions.safe.secure.MissingLocalDatabaseKeyException
import com.simprints.id.exceptions.safe.session.SessionDataSourceException
import com.simprints.id.exceptions.unexpected.RealmUninitialisedException
import com.simprints.id.tools.TimeHelper
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmQuery
import io.realm.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

open class SessionLocalDataSourceImpl(private val appContext: Context,
                                      private val secureDataManager: SecureLocalDbKeyProvider,
                                      private val timeHelper: TimeHelper,
                                      private val realmConfigBuilder: SessionRealmConfigBuilder,
                                      private val sessionEventsValidators: Array<SessionEventValidator>) : SessionLocalDataSource {
    companion object {
        const val PROJECT_ID = "projectId"
        const val END_TIME = "relativeEndTime"
        const val START_TIME = "startTime"
        const val SESSION_ID = "id"
        const val TYPE = "type"

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
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                realm.refresh()
                closeAnyOpenSession()

                realm.executeTransaction { reamInTrans ->

                    val count = addQueryParams(reamInTrans, SessionCaptureEventQuery()).count().toInt()
                    val sessionCaptureEvent = SessionCaptureEvent(
                        timeHelper.now(),
                        UUID.randomUUID().toString(),
                        SessionRepositoryImpl.PROJECT_ID_FOR_NOT_SIGNED_IN,
                        appVersionName,
                        libSimprintsVersionName,
                        language,
                        Device(
                            Build.VERSION.SDK_INT.toString(),
                            Build.MANUFACTURER + "_" + Build.MODEL,
                            deviceId),
                        DatabaseInfo(count))

                    reamInTrans.insert(DbEvent(sessionCaptureEvent))
                    Timber.d("Session created ${sessionCaptureEvent.id}")
                }
            }
        }
    }

    override suspend fun currentSessionId(): String? {
        val sessionCaptureEvents = load(SessionCaptureEventQuery(openSession = true)).toList()
        val currentSessionCaptureEvent = sessionCaptureEvents.firstOrNull() as? SessionCaptureEvent
        if (sessionCaptureEvents.size > 1) {
            Timber.d("More than 1 session open!")
        }

        return currentSessionCaptureEvent?.id
    }

    override suspend fun load(query: EventQuery): Flow<Event> =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                realm.refresh()
                addQueryParams(realm, query).findAll()?.map { it.fromDbToDomain() }?.asFlow() ?: emptyFlow()
            }
        }

    override suspend fun count(query: EventQuery): Int =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                realm.refresh()
                addQueryParams(realm, query).findAll()?.count() ?: 0
            }
        }

    override suspend fun delete(query: EventQuery) {
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                realm.refresh()
                realm.executeTransaction {
                    val events = addQueryParams(it, query).findAll()
                    events?.deleteAllFromRealm()
                }
            }
        }
    }

    override suspend fun insertOrUpdate(event: Event) {
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                realm.refresh()
                if (event.getSessionLabelIfExists() != null) {
                    val eventsInTheSameSession = load(SessionCaptureEventQuery(sessionId = event.getSessionLabelIfExists()?.labelValue))

                    sessionEventsValidators.forEach { it.validate(e) }
                }
                realm.executeTransaction {

                    it.insertOrUpdate(DbEvent(event))
                }
            }
        }
    }

    private fun addQueryParams(realm: Realm, query: EventQuery): RealmQuery<DbEvent> =
        realm.where(DbEvent::class.java).apply {
            when (query) {
                is SessionCaptureEventQuery -> addQueryParamsForSessionCaptureEvent(this, query)
            }.safeSealedWhens
        }.sort(START_TIME, Sort.DESCENDING)

    private fun addQueryParamsForSessionCaptureEvent(realmQuery: RealmQuery<DbEvent>, query: SessionCaptureEventQuery) {
        addQueryParamForSessionId(query.sessionId, realmQuery)
        addQueryParamForSessionId(query.sessionId, realmQuery)
        addQueryParamForProjectId(query.projectId, realmQuery)
        addQueryParamForOpenSession(query.openSession, realmQuery)
        addQueryParamForStartTime(query.startedBefore, realmQuery)
    }

    private fun addQueryParamForEventType(eventType: String?, query: RealmQuery<DbEvent>) {
        eventType?.let {
            query.equalTo(TYPE, it)
        }
    }

    private fun addQueryParamForProjectId(projectId: String?, query: RealmQuery<DbEvent>) {
        projectId?.let {
            query.equalTo(PROJECT_ID, it)
        }
    }

    private fun addQueryParamForSessionId(sessionId: String?, query: RealmQuery<DbEvent>) {
        sessionId?.let {
            query.equalTo(SESSION_ID, it)
        }
    }

    private fun addQueryParamForStartTime(startedBefore: Long?, query: RealmQuery<DbEvent>) {
        startedBefore?.let {
            query.greaterThan(START_TIME, it).not()
        }
    }

    private fun addQueryParamForOpenSession(openSession: Boolean?, query: RealmQuery<DbEvent>) {
        openSession?.let {
            if (it) {
                query.equalTo(END_TIME, 0L)
            } else {
                query.greaterThan(END_TIME, 0L)
            }
        }
    }

    private suspend fun closeAnyOpenSession() {
        wrapSuspendExceptionIfNeeded {
            val openSessionEvents = load(SessionCaptureEventQuery(openSession = true))
            openSessionEvents.onEach { sessionEvent ->
                val artificialTerminationEvent = ArtificialTerminationEvent(
                    timeHelper.now(),
                    ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION,
                    sessionEvent.id
                )
                insertOrUpdate(artificialTerminationEvent)
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
            realmConfig = realmConfigBuilder.build(localKey.projectId, localKey.value)
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

    private fun <T> wrapExceptionIfNeeded(block: () -> T): T =
        try {
            block()
        } catch (t: Throwable) {
            Timber.d(t)
            throw SessionDataSourceException(t)
        }

    private suspend fun <T> wrapSuspendExceptionIfNeeded(block: suspend () -> T): T =
        try {
            block()
        } catch (t: Throwable) {
            Timber.d(t)
            throw if (t is SessionDataSourceException) {
                t
            } else {
                SessionDataSourceException(t)
            }
        }
}
