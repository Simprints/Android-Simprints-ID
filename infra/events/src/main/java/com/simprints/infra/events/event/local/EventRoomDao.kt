package com.simprints.infra.events.event.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.local.models.DbEvent
import kotlinx.coroutines.flow.Flow

@Dao
internal interface EventRoomDao {
    @Query("select * from DbEvent order by createdAt_unixMs desc")
    suspend fun loadAll(): List<DbEvent>

    @Query("select * from DbEvent where scopeId = :scopeId order by createdAt_unixMs desc")
    suspend fun loadFromScope(scopeId: String): List<DbEvent>

    @Query("select eventJson from DbEvent where scopeId = :scopeId order by createdAt_unixMs desc")
    suspend fun loadEventJsonFromScope(scopeId: String): List<String>

    @Query("select count(*) from DbEvent")
    fun observeCount(): Flow<Int>

    @Query("select count(*) from DbEvent where type = :type")
    fun observeCountFromType(type: EventType): Flow<Int>

    @Query(
        """
        select count(*) from DbEvent 
        left join DbEventScope on DbEvent.scopeId = DbEventScope.id 
        where DbEventScope.end_unixMs is not null
        """,
    )
    fun observeCountInClosedScopes(): Flow<Int>

    @Query("delete from DbEvent where scopeId = :scopeId")
    suspend fun deleteAllFromScope(scopeId: String)

    @Query("delete from DbEvent where scopeId in (:scopeIds)")
    suspend fun deleteAllFromScopes(scopeIds: List<String>)

    @Query("delete from DbEvent")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dbEvent: DbEvent)
}
