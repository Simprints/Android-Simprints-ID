package com.simprints.id.data.db.event.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.local.models.DbEvent

@Dao
interface EventRoomDao {

    @Query("select * from DbEvent order by createdAt desc")
    suspend fun loadAll(): List<DbEvent>

    @Query("select * from DbEvent where sessionId = :sessionId order by createdAt desc")
    suspend fun loadFromSession(sessionId: String): List<DbEvent>

    @Query("select * from DbEvent where projectId = :projectId order by createdAt desc")
    suspend fun loadFromProject(projectId: String): List<DbEvent>

    @Query("select * from DbEvent where type = :type order by createdAt desc")
    suspend fun loadFromType(type: EventType?): List<DbEvent>

    @Query("select count(*) from DbEvent where projectId = :projectId")
    suspend fun countFromProject(projectId: String): Int

    @Query("select count(*) from DbEvent where type = :type AND projectId = :projectId")
    suspend fun countFromProjectByType(type: EventType, projectId: String): Int

    @Query("select count(*) from DbEvent where type = :type")
    suspend fun countFromType(type: EventType): Int

    @Query("delete from DbEvent where id = :id")
    suspend fun delete(id: String)

    @Query("delete from DbEvent where sessionId = :sessionId")
    suspend fun deleteAllFromSession(sessionId: String)

    @Query("delete from DbEvent")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dbEvent: DbEvent)

}
