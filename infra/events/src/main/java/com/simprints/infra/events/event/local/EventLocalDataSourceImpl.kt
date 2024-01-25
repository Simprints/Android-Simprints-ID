package com.simprints.infra.events.event.local

import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteException
import com.simprints.core.DispatcherIO
import com.simprints.core.NonCancellableIO
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.session.SessionScope
import com.simprints.infra.events.event.local.models.DbEvent
import com.simprints.infra.events.event.local.models.fromDbToDomain
import com.simprints.infra.events.event.local.models.fromDomainToDb
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.DB_CORRUPTION
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

internal open class EventLocalDataSourceImpl @Inject constructor(
    private val eventDatabaseFactory: EventDatabaseFactory,
    private val jsonHelper: JsonHelper,
    @DispatcherIO private val readingDispatcher: CoroutineDispatcher,
    @NonCancellableIO private val writingContext: CoroutineContext,
) : EventLocalDataSource {

    private var eventDao: EventRoomDao = eventDatabaseFactory.build().eventDao

    private var scopeDao: SessionScopeRoomDao = eventDatabaseFactory.build().scopeDao

    private val mutex = Mutex()

    private suspend fun <R> useRoom(context: CoroutineContext, block: suspend () -> R): R =
        withContext(context) {
            try {
                block()
            } catch (ex: SQLiteException) {
                if (isFileCorruption(ex)) {
                    rebuildDatabase(ex)
                    // Retry operation with new file and key
                    block()
                } else {
                    throw ex
                }
            }
        }

    private suspend fun <R> useRoomFlow(context: CoroutineContext, block: () -> Flow<R>): Flow<R> =
        withContext(context) {
            try {
                block().catch { cause ->
                    if (isFileCorruption(cause)) {
                        rebuildDatabase(cause)
                        // Recreate flow and re-emit values with the new file and key
                        emitAll(block())
                    } else {
                        throw cause
                    }
                }
            } catch (ex: SQLiteException) {
                if (isFileCorruption(ex)) {
                    rebuildDatabase(ex)
                    // Recreate flow with the new file and key
                    block()
                } else {
                    throw ex
                }
            }
        }

    private fun isFileCorruption(ex: Throwable) =
        ex is SQLiteDatabaseCorruptException ||
            ex.let { it as? SQLiteException }?.message?.contains("file is not a database") == true

    private suspend fun rebuildDatabase(ex: Throwable) = mutex.withLock {
        //DB corruption detected; either DB file or key is corrupt
        //1. Delete DB file in order to create a new one at next init
        eventDatabaseFactory.deleteDatabase()
        //2. Recreate the DB key
        eventDatabaseFactory.recreateDatabaseKey()
        //3. Log exception after recreating the key so we get extra info
        Simber.tag(DB_CORRUPTION.name).e(ex)
        //4. Rebuild database
        eventDao = eventDatabaseFactory.build().eventDao
        scopeDao = eventDatabaseFactory.build().scopeDao
    }

    override suspend fun countSessions(): Int = useRoom(readingDispatcher) {
        scopeDao.count()
    }

    override suspend fun saveSessionScope(scope: SessionScope) = useRoom(writingContext) {
        scopeDao.insertOrUpdate(scope.fromDomainToDb(jsonHelper))
    }

    override suspend fun loadOpenedSessions(): List<SessionScope> = useRoom(readingDispatcher) {
        scopeDao.loadOpen().map { it.fromDbToDomain(jsonHelper) }
    }

    override suspend fun loadClosedSessions(projectId: String): List<SessionScope> = useRoom(readingDispatcher) {
        scopeDao.loadClosed(projectId).map { it.fromDbToDomain(jsonHelper) }
    }

    override suspend fun deleteSession(sessionId: String) = useRoom(writingContext) {
        scopeDao.delete(listOf(sessionId))
    }

    override suspend fun deleteSessions(sessionIds: List<String>) = useRoom(writingContext) {
        scopeDao.delete(sessionIds)
    }

    override suspend fun loadAll(): Flow<Event> =
        useRoom(readingDispatcher) {
            eventDao.loadAll().map { it.fromDbToDomain() }.asFlow()
        }

    override suspend fun loadAllEventJsonFromSession(sessionId: String): List<String> =
        useRoom(readingDispatcher) {
            eventDao.loadEventJsonFromSession(sessionId)
        }

    override suspend fun loadAllFromSession(sessionId: String): List<Event> =
        useRoom(readingDispatcher) {
            eventDao.loadFromSession(sessionId = sessionId).map { it.fromDbToDomain() }
        }

    override suspend fun loadAllFromProject(projectId: String): List<Event> =
        useRoom(readingDispatcher) {
            eventDao.loadFromProject(projectId).map(DbEvent::fromDbToDomain)
        }

    override suspend fun loadAllClosedSessionIds(projectId: String): List<String> =
        useRoom(readingDispatcher) {
            eventDao.loadAllClosedSessionIds(projectId)
        }

    override suspend fun count(projectId: String): Int =
        useRoom(readingDispatcher) {
            eventDao.countFromProject(projectId = projectId)
        }

    override suspend fun count(type: EventType): Int =
        useRoom(readingDispatcher) {
            eventDao.countFromType(type = type)
        }

    override suspend fun observeCount(projectId: String): Flow<Int> =
        useRoomFlow(readingDispatcher) {
            eventDao.observeCount(projectId = projectId)
        }

    override suspend fun observeCount(projectId: String, type: EventType): Flow<Int> =
        useRoomFlow(readingDispatcher) {
            eventDao.observeCountFromType(projectId = projectId, type = type)
        }

    override suspend fun insertOrUpdate(event: Event) =
        useRoom(writingContext) {
            eventDao.insertOrUpdate(event.fromDomainToDb())
        }

    override suspend fun delete(ids: List<String>) =
        useRoom(writingContext) {
            eventDao.delete(ids)
        }

    override suspend fun deleteAllFromSession(sessionId: String) =
        useRoom(writingContext) {
            eventDao.deleteAllFromSession(sessionId = sessionId)
        }

    override suspend fun deleteAll() =
        useRoom(writingContext) {
            eventDao.deleteAll()
        }
}
