package com.simprints.id.data.db.event.local

import androidx.room.*
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.local.models.DbEvent

@Dao
interface DbEventRoomDao {

    @Query("select * from DbEvent order by addedAt desc")
    suspend fun load(): List<DbEvent>

    @Query("select * from DbEvent where type=:type order by addedAt desc")
    suspend fun loadByType(type: EventType): List<DbEvent>

    @Query("select * from DbEvent where sessionId LIKE :sessionId")
    suspend fun loadBySessionId(sessionId: String): List<DbEvent>

    @Query("select count(id) from DbEvent")
    suspend fun count(): Int

    @Delete
    suspend fun delete(dbEvent: DbEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dbEvent: DbEvent)

}
