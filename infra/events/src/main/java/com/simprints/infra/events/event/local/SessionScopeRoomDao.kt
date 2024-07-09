package com.simprints.infra.events.event.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.events.event.local.SessionScopeRoomDao.EventScopeConstants.CLOSED_SCOPES_LIMIT
import com.simprints.infra.events.event.local.models.DbEventScope

@Dao
internal interface SessionScopeRoomDao {

    @Query("select * from DbEventScope where type = :type AND end_unixMs IS NULL order by start_unixMs desc")
    suspend fun loadOpen(type: EventScopeType): List<DbEventScope>

    // Work around for loading too many scopes which cause upsync workers get stuck
    // To limit the number of scopes loaded, we only load the last 50 closed scopes
    @Query("select * from DbEventScope where type = :type AND end_unixMs IS NOT NULL order by start_unixMs desc limit $CLOSED_SCOPES_LIMIT")
    suspend fun loadClosed(type: EventScopeType): List<DbEventScope>

    @Query("select * from DbEventScope where type = :type AND end_unixMs IS NOT NULL order by start_unixMs desc limit :limit")
    suspend fun loadClosed(type: EventScopeType, limit: Int): List<DbEventScope>

    @Query("select * from DbEventScope where id = :scopeId order by start_unixMs desc limit 1")
    suspend fun loadScope(scopeId: String): DbEventScope?

    @Query("select count(*) from DbEventScope where type = :type")
    suspend fun count(type: EventScopeType): Int

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
