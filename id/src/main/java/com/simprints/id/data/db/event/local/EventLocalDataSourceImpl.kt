package com.simprints.id.data.db.event.local

import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.local.models.fromDbToDomain
import com.simprints.id.data.db.event.local.models.fromDomainToDb
import com.simprints.id.exceptions.safe.session.SessionDataSourceException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.withContext
import timber.log.Timber

open class EventLocalDataSourceImpl(private val eventDatabaseFactory: EventDatabaseFactory,
                                    private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : EventLocalDataSource {

    private val roomDao by lazy {
        eventDatabaseFactory.build().eventDao
    }

    override suspend fun loadAll(): Flow<Event> =
        wrapSuspendExceptionIfNeeded {
            withContext(dispatcher) {
                roomDao.load().map { it.fromDbToDomain() }.asFlow()
            }
        }

    override suspend fun loadAllFromSession(sessionId: String): Flow<Event> =
        wrapSuspendExceptionIfNeeded {
            withContext(dispatcher) {
                roomDao.load(sessionId = sessionId).map { it.fromDbToDomain() }.asFlow()
            }
        }

    override suspend fun loadAllFromProject(projectId: String): Flow<Event> =
        wrapSuspendExceptionIfNeeded {
            withContext(dispatcher) {
                roomDao.load(projectId = projectId).map { it.fromDbToDomain() }.asFlow()
            }
        }

    override suspend fun loadAllFromType(type: EventType): Flow<Event> =
        wrapSuspendExceptionIfNeeded {
            withContext(dispatcher) {
                roomDao.load(type = type).map { it.fromDbToDomain() }.asFlow()
            }
        }

    override suspend fun count(projectId: String): Int =
        wrapSuspendExceptionIfNeeded {
            withContext(dispatcher) {
                roomDao.count(projectId = projectId)
            }
        }

    override suspend fun count(projectId: String, type: EventType): Int =
        wrapSuspendExceptionIfNeeded {
            withContext(dispatcher) {
                roomDao.count(type = type, projectId = projectId)
            }
        }

    override suspend fun count(type: EventType): Int =
        wrapSuspendExceptionIfNeeded {
            withContext(dispatcher) {
                roomDao.count(type = type)
            }
        }

    override suspend fun insertOrUpdate(event: Event) =
        wrapSuspendExceptionIfNeeded {
            withContext(dispatcher) {
                roomDao.insertOrUpdate(event.fromDomainToDb())
            }
        }

    override suspend fun delete(id: String) {
        wrapSuspendExceptionIfNeeded {
            withContext(dispatcher) {
                roomDao.delete(id = id)
            }
        }
    }

    override suspend fun deleteAllFromSession(sessionId: String) {
        wrapSuspendExceptionIfNeeded {
            withContext(dispatcher) {
                roomDao.delete(sessionId = sessionId)
            }
        }
    }

    override suspend fun deleteAll() {
        wrapSuspendExceptionIfNeeded {
            withContext(dispatcher) {
                roomDao.delete()
            }
        }
    }

    private suspend fun <T> wrapSuspendExceptionIfNeeded(block: suspend () -> T): T =
        try {
            block()
        } catch (t: Throwable) {
            Timber.d(t)

            //TODO this is an endless loop ATM
            /**
             * Jira: CORE-416. When the app closes calls to cancel the coroutines are thrown by
             * SessionDataSourceException which is an RTE. Cancellation in coroutines is cooperative
             * but in this case we don't need to propagate a JabCancellationException further.
             * TODO: This check for a CancellationException can be removed after saving events is moved to the WorkManager.
             */
            if (t is CancellationException)
                block()
            else
                throw if (t is SessionDataSourceException) {
                    t
                } else {
                    SessionDataSourceException(t)
                }
        }

}
