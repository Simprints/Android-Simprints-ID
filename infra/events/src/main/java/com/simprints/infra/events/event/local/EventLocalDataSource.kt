package com.simprints.infra.events.event.local

import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteException
import com.simprints.core.DispatcherIO
import com.simprints.core.NonCancellableIO
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
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
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
internal open class EventLocalDataSource @Inject constructor(
    private val eventDatabaseFactory: EventDatabaseFactory,
    private val jsonHelper: JsonHelper,
    @DispatcherIO private val readingDispatcher: CoroutineDispatcher,
    @NonCancellableIO private val writingContext: CoroutineContext,
) {
    private var eventDao: EventRoomDao = eventDatabaseFactory.get().eventDao

    private var scopeDao: SessionScopeRoomDao = eventDatabaseFactory.get().scopeDao

    private val mutex = Mutex()

    private suspend fun <R> useRoom(
        context: CoroutineContext,
        block: suspend () -> R,
    ): R = withContext(context) {
        try {
            block()
        } catch (ex: SQLiteException) {
            if (isFileCorruption(ex)) {
                recreateDatabase(ex)
                // Retry operation with new file and key
                block()
            } else {
                throw ex
            }
        }
    }

    private suspend fun <R> useRoomFlow(
        context: CoroutineContext,
        block: () -> Flow<R>,
    ): Flow<R> = withContext(context) {
        try {
            block().catch { cause ->
                if (isFileCorruption(cause)) {
                    recreateDatabase(cause)
                    // Recreate flow and re-emit values with the new file and key
                    emitAll(block())
                } else {
                    throw cause
                }
            }
        } catch (ex: SQLiteException) {
            if (isFileCorruption(ex)) {
                recreateDatabase(ex)
                // Recreate flow with the new file and key
                block()
            } else {
                throw ex
            }
        }
    }

    private fun isFileCorruption(ex: Throwable) = ex is SQLiteDatabaseCorruptException ||
        ex.let { it as? SQLiteException }?.message?.contains("file is not a database") == true

    private suspend fun recreateDatabase(ex: Throwable) = mutex.withLock {
        eventDatabaseFactory.recreateDatabase()
        Simber.e("Recreated event DB due to error", ex, tag = DB_CORRUPTION)
        eventDao = eventDatabaseFactory.get().eventDao
        scopeDao = eventDatabaseFactory.get().scopeDao
    }

    suspend fun saveEventScope(scope: EventScope) = useRoom(writingContext) {
        scopeDao.insertOrUpdate(scope.fromDomainToDb(jsonHelper))
    }

    suspend fun countEventScopes(type: EventScopeType): Int = useRoom(readingDispatcher) {
        scopeDao.count(type)
    }

    suspend fun countClosedEventScopes(type: EventScopeType): Int = useRoom(readingDispatcher) {
        scopeDao.countClosed(type)
    }

    suspend fun loadAllScopes(): List<EventScope> = useRoom(readingDispatcher) {
        scopeDao.loadAll().map { it.fromDbToDomain(jsonHelper) }
    }

    suspend fun loadOpenedScopes(type: EventScopeType): List<EventScope> = useRoom(readingDispatcher) {
        scopeDao.loadOpen(type).map { it.fromDbToDomain(jsonHelper) }
    }

    suspend fun loadClosedScopes(
        type: EventScopeType,
        limit: Int,
    ): List<EventScope> = useRoom(readingDispatcher) {
        scopeDao.loadClosed(type, limit).map { it.fromDbToDomain(jsonHelper) }
    }

    suspend fun loadEventScope(scopeId: String): EventScope? = useRoom(writingContext) {
        scopeDao.loadScope(scopeId)?.fromDbToDomain(jsonHelper)
    }

    suspend fun deleteEventScope(scopeId: String) = useRoom(writingContext) {
        scopeDao.delete(listOf(scopeId))
    }

    suspend fun deleteEventScopes(scopeIds: List<String>) = useRoom(writingContext) {
        scopeIds.chunked(SQLITE_VARIABLE_LIMIT).forEach { chunk ->
            scopeDao.delete(chunk)
        }
    }

    suspend fun saveEvent(event: Event) = useRoom(writingContext) {
        eventDao.insertOrUpdate(event.fromDomainToDb())
    }

    suspend fun observeEventCount(): Flow<Int> = useRoomFlow(readingDispatcher) {
        eventDao.observeCount()
    }

    suspend fun observeEventCount(type: EventType): Flow<Int> = useRoomFlow(readingDispatcher) {
        eventDao.observeCountFromType(type = type)
    }

    suspend fun loadAllEvents(): Flow<Event> = useRoom(readingDispatcher) {
        eventDao.loadAll().map { it.fromDbToDomain() }.asFlow()
    }

    suspend fun loadEventJsonInScope(scopeId: String): List<String> = useRoom(readingDispatcher) {
        eventDao.loadEventJsonFromScope(scopeId)
    }

    suspend fun loadEventsInScope(scopeId: String): List<Event> = useRoom(readingDispatcher) {
        eventDao.loadFromScope(scopeId = scopeId).map { it.fromDbToDomain() }
    }

    suspend fun deleteEventsInScope(scopeId: String) = useRoom(writingContext) {
        eventDao.deleteAllFromScope(scopeId = scopeId)
    }

    suspend fun deleteEventsInScopes(scopeIds: List<String>) = useRoom(writingContext) {
        scopeIds.chunked(SQLITE_VARIABLE_LIMIT).forEach { chunk ->
            eventDao.deleteAllFromScopes(scopeIds = chunk)
        }
    }

    suspend fun deleteAll() = useRoom(writingContext) {
        scopeDao.deleteAll()
        eventDao.deleteAll()
    }

    companion object {
        // Actual limit is 999, but it is better to leave some wiggle room
        private const val SQLITE_VARIABLE_LIMIT = 900
    }
}
