package com.simprints.id.data.db.event.local

import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.local.models.DbLocalEventQuery
import com.simprints.id.data.db.event.local.models.fromDbToDomain
import com.simprints.id.data.db.event.local.models.fromDomainToDb
import com.simprints.id.exceptions.safe.session.SessionDataSourceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.withContext
import timber.log.Timber

open class EventLocalDataSourceImpl(private val eventDatabaseFactory: EventDatabaseFactory) : EventLocalDataSource {

    private val roomDao by lazy {
        eventDatabaseFactory.build().eventDao
    }


    override suspend fun load(dbQuery: DbLocalEventQuery): Flow<Event> =
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


    override suspend fun count(dbQuery: DbLocalEventQuery): Int =
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

    override suspend fun delete(dbQuery: DbLocalEventQuery) {
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
                roomDao.insertOrUpdate(event.fromDomainToDb())
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
}
