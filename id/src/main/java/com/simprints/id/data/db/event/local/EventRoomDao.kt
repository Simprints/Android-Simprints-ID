package com.simprints.id.data.db.event.local

import androidx.room.*
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import com.simprints.id.data.db.event.local.models.DbEvent

@Dao
interface EventRoomDao {

    @Query("select * from DbEvent order by addedAt desc")
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

//    @RawQuery
//    suspend fun load(query: SupportSQLiteQuery): List<DbEvent>
//
//    @RawQuery
//    suspend fun count(query: SupportSQLiteQuery): Int
}

//fun EventQuery.toSupportSQLiteQuery() {
//    String.format("select * from DbEvent where labels order by addedAt desc")
//
//    val conditions = mutableListOf<String>()
//    if(id.isNullOrBlank()) {
//        conditions += "id=$id AND"
//    }
//
//    if(eventPayloadType != null) {
//        conditions += "type=$eventPayloadType AND"
//    }
//
//    if(projectId != null) {
//        conditions += "labels LIKE '%$projectId%'"
//    }
//
//    if(sessionId != null) {
//        conditions += "labels LIKE '%$sessionId%'"
//    }
//
//    if(startTime != null) {
//        conditions += "sta LIKE '%$sessionId%'"
//    }
//
//    if(endTime != null) {
//        conditions += "labels LIKE '%$sessionId%'"
//    }
//
//}
