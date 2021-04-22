package com.simprints.id.data.db.event.local

import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.local.models.fromDbToDomain
import com.simprints.id.data.db.event.local.models.fromDomainToDb
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

    override suspend fun loadAllFromSession(sessionId: String): Flow<Event> = withContext(readingDispatcher) {
        eventDao.loadFromSession(sessionId = sessionId).map { it.fromDbToDomain() }.asFlow()
    }

    override suspend fun loadAllFromProject(projectId: String): Flow<Event> = withContext(readingDispatcher) {
        eventDao.loadFromProject(projectId = projectId).map { it.fromDbToDomain() }.asFlow()
    }

    override suspend fun loadAllFromType(type: EventType): Flow<Event> = withContext(readingDispatcher) {
        eventDao.loadFromType(type = type).map { it.fromDbToDomain() }.asFlow()
    }

    override suspend fun count(projectId: String): Int = withContext(readingDispatcher) {
        eventDao.countFromProject(projectId = projectId)
    }

    override suspend fun count(projectId: String, type: EventType): Int = withContext(readingDispatcher) {
        eventDao.countFromProjectByType(type = type, projectId = projectId)
    }

    override suspend fun count(type: EventType): Int = withContext(readingDispatcher) {
        eventDao.countFromType(type = type)
    }

    override suspend fun insertOrUpdate(event: Event) = withContext(writingContext) {
        eventDao.insertOrUpdate(event.fromDomainToDb())
    }

    override suspend fun delete(id: String) = withContext(writingContext) {
        eventDao.delete(id = id)
    }

    override suspend fun deleteAllFromSession(sessionId: String) = withContext(writingContext) {
        eventDao.deleteAllFromSession(sessionId = sessionId)
    }

    override suspend fun deleteAll() = withContext(writingContext) {
        eventDao.deleteAll()
    }

}
