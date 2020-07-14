package com.simprints.id.data.db.event.local

import androidx.room.*
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import com.simprints.id.data.db.event.local.models.DbEvent

@Dao
interface EventRoomDao {

    @Query("select * from DbEvent where labels order by addedAt desc")
    suspend fun load(): List<DbEvent>

    @Query("select * from DbEvent where type=:type order by addedAt desc")
    suspend fun loadByType(type: EventPayloadType): List<DbEvent>

    @Query("select * from DbEvent where labels LIKE :sessionId")
    suspend fun loadBySessionId(sessionId: String): List<DbEvent>

    @Query("select count(id) from DbEvent")
    suspend fun count(): Int

    @Delete
    suspend fun delete(dbEvent: DbEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dbEvent: DbEvent)

//
//    @Query("select * from DbSubjectsDownSyncOperation")
//    suspend fun getDownSyncOperationsAll(): List<DbSubjectsDownSyncOperation>
//
//    @Query("select * from DbSubjectsDownSyncOperation")
//    fun getDownSyncOperation(): List<DbSubjectsDownSyncOperation>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertOrReplaceDownSyncOperation(dbSubjectsDownSyncOperation: DbSubjectsDownSyncOperation)
//
//    @Query("delete from DbSubjectsDownSyncOperation")
//    fun deleteAll()

}
