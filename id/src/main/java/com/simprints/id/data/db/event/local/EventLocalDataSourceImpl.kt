package com.simprints.id.data.db.event.local

import android.content.Context
import com.simprints.id.data.db.event.EventRepositoryImpl
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.validators.SessionEventValidator
import com.simprints.id.data.db.event.local.EventLocalDataSource.EventQuery
import com.simprints.id.data.db.event.local.models.fromDbToDomain
import com.simprints.id.data.db.event.local.models.fromDomainToDb
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.exceptions.safe.session.SessionDataSourceException
import com.simprints.id.tools.TimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber

open class EventLocalDataSourceImpl(private val appContext: Context,
                                    private val secureDataManager: SecureLocalDbKeyProvider,
                                    private val loginInfoManager: LoginInfoManager,
                                    private val deviceId: String,
                                    private val timeHelper: TimeHelper,
                                    private val roomDao: DbEventRoomDao,
                                    private val sessionEventsValidators: Array<SessionEventValidator>) : EventLocalDataSource {

    override suspend fun create(event: SessionCaptureEvent) {
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                closeAnyOpenSession()
                roomDao.insertOrUpdate(event.fromDomainToDb())
                Timber.d("Session created ${event.id}")
                event.id
            }
        }
    }

    override suspend fun getCurrentSessionCaptureEvent() =
        load(EventQuery(type = SESSION_CAPTURE, endTime = LongRange(0, 0))).first() as SessionCaptureEvent

    override suspend fun load(query: EventQuery): Flow<Event> =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                with(query) {
                    roomDao.load(
                        id, type, projectId, subjectId, attendantId, sessionId, deviceId,
                        startTime?.first, startTime?.endInclusive, endTime?.first, endTime?.endInclusive)
                        .map { it.fromDbToDomain() }
                        .asFlow()
                }
            }
        }


    override suspend fun count(query: EventQuery): Int =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                with(query) {
                    roomDao.count(
                        id, type, projectId, subjectId, attendantId, sessionId, deviceId,
                        startTime?.first, startTime?.endInclusive, endTime?.first, endTime?.endInclusive)
                }
            }
        }

    override suspend fun delete(query: EventQuery) {
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                with(query) {
                    roomDao.delete(
                        id, type, projectId, subjectId, attendantId, sessionId, deviceId,
                        startTime?.first, startTime?.endInclusive, endTime?.first, endTime?.endInclusive)
                }
            }
        }
    }

    override suspend fun insertOrUpdate(event: Event) =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                event.labels = getLabelsForAnyEvent()
                roomDao.insertOrUpdate(event.fromDomainToDb())
            }
        }

    override suspend fun insertOrUpdateInCurrentSession(event: Event) {
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                val currentSession = getCurrentSessionCaptureEvent()
                val currentSessionsEvents = load(EventQuery(sessionId = currentSession.id)).toList()
                sessionEventsValidators.forEach {
                    it.validate(currentSessionsEvents, event)
                }

                event.labels = getLabelsForAnyEvent().copy(sessionId = currentSession.id)
                roomDao.insertOrUpdate(event.fromDomainToDb())
            }
        }
    }

    private suspend fun closeAnyOpenSession() {
        wrapSuspendExceptionIfNeeded {
            val openSessions = load(EventQuery(type = SESSION_CAPTURE, endTime = LongRange(0, 0)))

            openSessions.onEach { session ->
                val artificialTerminationEvent = ArtificialTerminationEvent(
                    timeHelper.now(),
                    NEW_SESSION
                )
                artificialTerminationEvent.labels = getLabelsForAnyEvent().copy(sessionId = session.id)
                roomDao.insertOrUpdate(artificialTerminationEvent.fromDomainToDb())

                session.payload.endedAt = timeHelper.now()
                roomDao.insertOrUpdate(session.fromDomainToDb())
            }
        }
    }

    private fun getLabelsForAnyEvent(): EventLabels {
        //STOPSHIP: enforce the right labels in each event
        var projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
        if (projectId.isNullOrEmpty()) {
            projectId = EventRepositoryImpl.PROJECT_ID_FOR_NOT_SIGNED_IN
        }
        return EventLabels(deviceId = deviceId, projectId = projectId)
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
