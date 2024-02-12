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

    @Query("select * from DbEvent where sessionId = :sessionId order by createdAt_unixMs desc")
    suspend fun loadFromSession(sessionId: String): List<DbEvent>

    @Query("select eventJson from DbEvent where sessionId = :sessionId order by createdAt_unixMs desc")
    suspend fun loadEventJsonFromSession(sessionId: String): List<String>

    @Query("select count(*) from DbEvent")
    fun observeCount(): Flow<Int>

    @Query("select count(*) from DbEvent where type = :type")
    fun observeCountFromType(type: EventType): Flow<Int>

    @Query("delete from DbEvent where sessionId = :sessionId")
    suspend fun deleteAllFromSession(sessionId: String)

    @Query("delete from DbEvent")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dbEvent: DbEvent)

}
