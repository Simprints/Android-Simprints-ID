package com.simprints.infra.events.event.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.events.event.local.models.DbEventScope

@Dao
internal interface SessionScopeRoomDao {
    @Query("select * from DbEventScope order by start_unixMs desc")
    suspend fun loadAll(): List<DbEventScope>

    @Query("select * from DbEventScope where type = :type AND end_unixMs IS NULL order by start_unixMs desc")
    suspend fun loadOpen(type: EventScopeType): List<DbEventScope>

    @Query("select * from DbEventScope where type = :type AND end_unixMs IS NOT NULL order by start_unixMs desc limit :limit")
    suspend fun loadClosed(
        type: EventScopeType,
        limit: Int,
    ): List<DbEventScope>

    @Query("select * from DbEventScope where id = :scopeId order by start_unixMs desc limit 1")
    suspend fun loadScope(scopeId: String): DbEventScope?

    @Query("select count(*) from DbEventScope where type = :type")
    suspend fun count(type: EventScopeType): Int

    @Query("select count(*) from DbEventScope where type = :type AND end_unixMs IS NOT NULL")
    suspend fun countClosed(type: EventScopeType): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dbEvent: DbEventScope)

    @Query("delete from DbEventScope where id in (:ids)")
    suspend fun delete(ids: List<String>)

    @Query("delete from DbEventScope")
    suspend fun deleteAll()

    object EventScopeConstants {
        const val CLOSED_SCOPES_LIMIT = 50
    }
}
