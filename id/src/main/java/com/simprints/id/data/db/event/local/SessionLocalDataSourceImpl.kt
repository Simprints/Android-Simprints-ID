package com.simprints.id.data.db.event.local

import android.content.Context
import android.os.Build
import com.simprints.id.data.db.event.EventRepositoryImpl
import com.simprints.id.data.db.event.domain.events.ArtificialTerminationEvent
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel.ProjectIdLabel
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.events.EventPayloadType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.events.getSessionLabelIfExists
import com.simprints.id.data.db.event.domain.events.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.events.session.Device
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.validators.SessionEventValidator
import com.simprints.id.data.db.event.local.SessionLocalDataSource.EventQuery
import com.simprints.id.data.db.event.local.models.fromDomainToDb
import com.simprints.id.data.secure.LocalDbKey
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.exceptions.safe.session.SessionDataSourceException
import com.simprints.id.tools.TimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

open class SessionLocalDataSourceImpl(private val appContext: Context,
                                      private val secureDataManager: SecureLocalDbKeyProvider,
                                      private val timeHelper: TimeHelper,
                                      private val roomDao: EventRoomDao,
                                      private val sessionEventsValidators: Array<SessionEventValidator>) : SessionLocalDataSource {
    companion object {
        const val PROJECT_ID = "projectId"
        const val END_TIME = "relativeEndTime"
        const val START_TIME = "startTime"
        const val SESSION_ID = "id"
        const val TYPE = "type"

        const val SESSIONS_REALM_DB_FILE_NAME = "event_data"
    }

    private var localDbKey: LocalDbKey? = null

    override suspend fun create(appVersionName: String,
                                libSimprintsVersionName: String,
                                language: String,
                                deviceId: String): String =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                closeAnyOpenSession()
                val count = roomDao.count()
                val sessionCaptureEvent = SessionCaptureEvent(
                    timeHelper.now(),
                    UUID.randomUUID().toString(),
                    EventRepositoryImpl.PROJECT_ID_FOR_NOT_SIGNED_IN,
                    appVersionName,
                    libSimprintsVersionName,
                    language,
                    Device(
                        Build.VERSION.SDK_INT.toString(),
                        Build.MANUFACTURER + "_" + Build.MODEL,
                        deviceId),
                    DatabaseInfo(count))

                roomDao.insertOrUpdate(sessionCaptureEvent.fromDomainToDb())
                Timber.d("Session created ${sessionCaptureEvent.id}")
                sessionCaptureEvent.id
            }
        }

    override suspend fun getCurrentSessionCaptureEvent() =
        load(EventQuery(eventPayloadType = SESSION_CAPTURE, endTime = LongRange(0, 0))).first() as SessionCaptureEvent

    override suspend fun load(query: EventQuery): Flow<Event> =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                roomDao.load()
                    .map { it.fromDbToDomain() }
                    .filter { query.filter(it) }
                    .sortedByDescending { it.payload.createdAt }
                    .asFlow()
            }
        }


    override suspend fun count(query: EventQuery): Int =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                load(query).toList().size
            }
        }

    override suspend fun delete(query: EventQuery) {
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                load(query).collect {
                    roomDao.delete(it.fromDomainToDb())
                }
            }
        }
    }

    override suspend fun insertOrUpdate(event: Event) =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                val sessionIdLabel = event.getSessionLabelIfExists()
                if (sessionIdLabel != null) {
                    val eventsInTheSameSession = load(EventQuery(sessionId = sessionIdLabel.sessionId)).toList()
                    sessionEventsValidators.forEach {
                        it.validate(eventsInTheSameSession, event)
                    }
                }
                roomDao.insertOrUpdate(event.fromDomainToDb())
            }
        }

    private suspend fun closeAnyOpenSession() {
        wrapSuspendExceptionIfNeeded {
            val openSessionIds = load(EventQuery(eventPayloadType = SESSION_CAPTURE, endTime = LongRange(0, 0))).map { it.id }

            openSessionIds.onEach { sessionId ->
                val artificialTerminationEvent = ArtificialTerminationEvent(
                    timeHelper.now(),
                    ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION,
                    sessionId
                )
                insertOrUpdate(artificialTerminationEvent)
            }
        }
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

    fun EventQuery.filter(event: Event) =
        with(this) {
            projectId?.let { event.labels.any { it == ProjectIdLabel(projectId) } } ?: false ||
                sessionId?.let { event.labels.any { it == SessionIdLabel(sessionId) } } ?: false ||
                eventPayloadType?.let { event.payload.type == it } ?: false ||
                id?.let { event.id == it } ?: false ||
                startTime?.let { event.payload.createdAt in it } ?: false ||
                endTime?.let { event.payload.endedAt in it } ?: false
        }
}
