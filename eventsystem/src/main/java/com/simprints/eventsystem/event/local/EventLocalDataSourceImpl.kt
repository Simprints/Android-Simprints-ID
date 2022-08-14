package com.simprints.eventsystem.event.local

import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.local.models.fromDbToDomain
import com.simprints.eventsystem.event.local.models.fromDomainToDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

open class EventLocalDataSourceImpl(
    private val eventDatabaseFactory: EventDatabaseFactory,
    private val readingDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val writingContext: CoroutineContext = readingDispatcher + NonCancellable
) : EventLocalDataSource {

    private val eventDao by lazy {
        eventDatabaseFactory.build().eventDao
    }

    override suspend fun loadAll(): Flow<Event> = withContext(readingDispatcher) {
        eventDao.loadAll().map { it.fromDbToDomain() }.asFlow()
    }

    override suspend fun loadAllEventJsonFromSession(sessionId: String): List<String> =
        withContext(readingDispatcher) {
            eventDao.loadEventJsonFromSession(sessionId)
        }

    override suspend fun loadAllFromSession(sessionId: String): List<Event> =
        withContext(readingDispatcher) {
            eventDao.loadFromSession(sessionId = sessionId).map { it.fromDbToDomain() }
        }

    override suspend fun loadOpenedSessions(): Flow<Event> =
        withContext(readingDispatcher) {
            eventDao.loadOpenedSessions().map { it.fromDbToDomain() }.asFlow()
        }

    override suspend fun loadAllClosedSessionIds(projectId: String): List<String> =
        withContext(readingDispatcher) {
            eventDao.loadAllClosedSessionIds(projectId)
        }

    override suspend fun count(projectId: String): Int = withContext(readingDispatcher) {
        eventDao.countFromProject(projectId = projectId)
    }

    override suspend fun count(projectId: String, type: EventType): Int =
        withContext(readingDispatcher) {
            eventDao.countFromProjectByType(type = type, projectId = projectId)
        }

    override suspend fun count(type: EventType): Int = withContext(readingDispatcher) {
        eventDao.countFromType(type = type)
    }

    override suspend fun insertOrUpdate(event: Event) = withContext(writingContext) {
        eventDao.insertOrUpdate(event.fromDomainToDb())
    }

    override suspend fun loadOldSubjectCreationEvents(projectId: String): List<Event> =
        withContext(writingContext) {
            eventDao.loadOldSubjectCreationEvents(projectId).map { it.fromDbToDomain() }
        }

    override suspend fun delete(ids: List<String>) = withContext(writingContext) {
        eventDao.delete(ids)
    }

    override suspend fun deleteAllFromSession(sessionId: String) = withContext(writingContext) {
        eventDao.deleteAllFromSession(sessionId = sessionId)
    }

    override suspend fun deleteAll() = withContext(writingContext) {
        eventDao.deleteAll()
    }

}
