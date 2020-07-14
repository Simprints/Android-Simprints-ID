package com.simprints.id.data.db.event.local

import android.content.Context
import android.os.Build
import com.simprints.id.data.db.event.EventRepositoryImpl
import com.simprints.id.data.db.event.domain.events.ArtificialTerminationEvent
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventPayloadType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.events.EventQuery
import com.simprints.id.data.db.event.domain.events.EventQuery.byDate
import com.simprints.id.data.db.event.domain.events.EventQuery.byType
import com.simprints.id.data.db.event.domain.events.getSessionLabelIfExists
import com.simprints.id.data.db.event.domain.events.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.events.session.Device
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent.SessionCapturePayload
import com.simprints.id.data.db.event.domain.validators.SessionEventValidator
import com.simprints.id.data.db.event.local.models.fromDomainToDb
import com.simprints.id.data.secure.LocalDbKey
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.exceptions.safe.session.SessionDataSourceException
import com.simprints.id.tools.TimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
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
                                deviceId: String) {
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
            }
        }
    }

    override suspend fun currentSessionId(): String? =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                val currentSessionCaptureEvent =
                    roomDao.loadByType(SESSION_CAPTURE)
                        .map { it.fromDbToDomain() }
                        .filterIsInstance<SessionCaptureEvent>()
                        .firstOrNull { (it.payload as SessionCapturePayload).endTime > 0 }

                currentSessionCaptureEvent?.id
            }
        }

    override suspend fun load(query: EventQuery): Flow<Event> =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                when (query) {
                    is byType -> {
                        roomDao.loadByType(query.type)
                    }
                    is byDate -> {
                        roomDao.load().filter { it.addedAt.time < query.startedBefore }
                    } //StopShip
                    else -> throw Throwable("s")
                }.map { it.fromDbToDomain() }.asFlow()
            }
        }


    override suspend fun count(query: EventQuery): Int =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                //StopShip: query
                roomDao.count()
            }
        }

    override suspend fun delete(query: EventQuery) {
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                //StopShip: query
                roomDao.count()
            }
        }
    }

    override suspend fun insertOrUpdate(event: Event) =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                val sessionId = event.getSessionLabelIfExists()
                if (sessionId != null) {
                    val eventsInTheSameSession = roomDao.loadBySessionId(sessionId.labelValue).map { it.fromDbToDomain() }
                    sessionEventsValidators.forEach {
                        it.validate(eventsInTheSameSession, event)
                    }
                }
                roomDao.insertOrUpdate(event.fromDomainToDb())
            }
        }

    private suspend fun closeAnyOpenSession() {
        wrapSuspendExceptionIfNeeded {
            val openSessionIds = roomDao.loadByType(SESSION_CAPTURE)
                .map { it.fromDbToDomain() }
                .filterIsInstance<SessionCaptureEvent>()
                .filter { (it.payload as SessionCapturePayload).endTime == 0L }
                .map { it.id }

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
