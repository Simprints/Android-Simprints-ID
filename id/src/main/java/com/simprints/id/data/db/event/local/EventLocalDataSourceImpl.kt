package com.simprints.id.data.db.event.local

import com.simprints.id.data.db.event.EventRepositoryImpl
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.validators.EventValidator
import com.simprints.id.data.db.event.local.models.DbEventQuery
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
        load(DbEventQuery(type = SESSION_CAPTURE, endTime = LongRange(0, 0))).first() as SessionCaptureEvent

    override suspend fun load(dbQuery: DbEventQuery): Flow<Event> =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                with(dbQuery) {
                    roomDao.load(
                        id = id,
                        type = type,
                        projectId = projectId,
                        subjectId = subjectId,
                        attendantId = attendantId,
                        sessionId = sessionId,
                        deviceId = deviceId,
                        createdAtLower = startTime?.first,
                        createdAtUpper = startTime?.last,
                        endedAtLower = endTime?.first,
                        endedAtUpper = endTime?.last)
                        .map { it.fromDbToDomain() }
                        .asFlow()
                }
            }
        }


    override suspend fun count(dbQuery: DbEventQuery): Int =
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                with(dbQuery) {
                    roomDao.count(
                        id = id,
                        type = type,
                        projectId = projectId,
                        subjectId = subjectId,
                        attendantId = attendantId,
                        sessionId = sessionId,
                        deviceId = deviceId,
                        createdAtLower = startTime?.first,
                        createdAtUpper = startTime?.endInclusive,
                        endedAtLower = endTime?.first,
                        endedAtUpper = endTime?.last)
                }
            }
        }

    override suspend fun delete(dbQuery: DbEventQuery) {
        wrapSuspendExceptionIfNeeded {
            withContext(Dispatchers.IO) {
                with(dbQuery) {
                    roomDao.delete(
                        id = id,
                        type = type,
                        projectId = projectId,
                        subjectId = subjectId,
                        attendantId = attendantId,
                        sessionId = sessionId,
                        deviceId = deviceId,
                        createdAtLower = startTime?.first,
                        createdAtUpper = startTime?.endInclusive,
                        endedAtLower = endTime?.first,
                        endedAtUpper = endTime?.last)
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
                val currentSessionsEvents = load(DbEventQuery(sessionId = currentSession.id)).toList()
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
            val openSessions = load(DbEventQuery(type = SESSION_CAPTURE, endTime = LongRange(0, 0))).map { it as SessionCaptureEvent }

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
        if (projectId.isEmpty()) {
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
