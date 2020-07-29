package com.simprints.id.data.db.event.local

import com.simprints.id.data.db.event.EventRepositoryImpl
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.validators.EventValidator
import com.simprints.id.data.db.event.local.EventLocalDataSource.EventQuery
import com.simprints.id.data.db.event.local.models.fromDbToDomain
import com.simprints.id.data.db.event.local.models.fromDomainToDb
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.session.SessionDataSourceException
import com.simprints.id.tools.TimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber

open class EventLocalDataSourceImpl(private val eventDatabaseFactory: EventDatabaseFactory,
                                    private val loginInfoManager: LoginInfoManager,
                                    private val deviceId: String,
                                    private val timeHelper: TimeHelper,
                                    private val eventsValidators: Array<EventValidator>) : EventLocalDataSource {

    private val roomDao by lazy {
        eventDatabaseFactory.build().eventDao
    }

    override suspend fun create(event: SessionCaptureEvent) {
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                closeAnyOpenSession()
                roomDao.insertOrUpdate(event.fromDomainToDb())
            }.also {
                Timber.d("Session created ${event.id}")
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
                        startTime?.first, startTime?.last, endTime?.first, endTime?.last)
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
                        startTime?.first, startTime?.endInclusive, endTime?.first, endTime?.last)
                }
            }
        }

    override suspend fun delete(query: EventQuery) {
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                with(query) {
                    roomDao.delete(
                        id, type, projectId, subjectId, attendantId, sessionId, deviceId,
                        startTime?.first, startTime?.endInclusive, endTime?.first, endTime?.last)
                }
            }
        }
    }

    override suspend fun insertOrUpdate(event: Event) =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {

                // For any new event, deviceId and projectId labels are added
                event.labels = event.labels.appendLabelsForAllEvents()
                roomDao.insertOrUpdate(event.fromDomainToDb())
            }
        }

    override suspend fun insertOrUpdateInCurrentSession(event: Event) {
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                val currentSession = getCurrentSessionCaptureEvent()
                val currentSessionsEvents = load(EventQuery(sessionId = currentSession.id)).toList()
                eventsValidators.forEach {
                    it.validate(currentSessionsEvents, event)
                }

                // a sessionId label is added along with device and projectId labels
                event.labels = event.labels.appendLabelsForAllEvents().appendSessionId(currentSession.id)
                roomDao.insertOrUpdate(event.fromDomainToDb())
            }
        }
    }

    private suspend fun closeAnyOpenSession() {
        wrapSuspendExceptionIfNeeded {
            val openSessions = load(EventQuery(type = SESSION_CAPTURE, endTime = LongRange(0, 0))).map { it as SessionCaptureEvent }

            openSessions.collect { session ->
                val artificialTerminationEvent = ArtificialTerminationEvent(
                    timeHelper.now(),
                    NEW_SESSION
                )
                artificialTerminationEvent.labels = artificialTerminationEvent.labels.appendLabelsForAllEvents().appendSessionId(session.id)
                roomDao.insertOrUpdate(artificialTerminationEvent.fromDomainToDb())

                session.payload.endedAt = timeHelper.now()
                roomDao.insertOrUpdate(session.fromDomainToDb())
            }
        }
    }

    private fun EventLabels.appendLabelsForAllEvents() =
        this.appendProjectIdLabel().appendDeviceIdLabel()

    private fun EventLabels.appendProjectIdLabel(): EventLabels {
        var projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
        if (projectId.isNullOrEmpty()) {
            projectId = EventRepositoryImpl.PROJECT_ID_FOR_NOT_SIGNED_IN
        }
        return this.copy(projectId = projectId)
    }

    private fun EventLabels.appendDeviceIdLabel(): EventLabels = this.copy(deviceId = this@EventLocalDataSourceImpl.deviceId)
    private fun EventLabels.appendSessionId(sessionId: String): EventLabels = this.copy(sessionId = sessionId)

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
