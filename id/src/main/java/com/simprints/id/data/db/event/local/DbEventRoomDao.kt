package com.simprints.id.data.db.event.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.local.models.DbEvent

@Dao
interface DbEventRoomDao {

    @Query("""
        select * from DbEvent where
        (:id IS NULL OR :id = id) AND
        (:type IS NULL OR :type = type) AND
        (:projectId IS NULL OR :projectId = projectId) AND
        (:subjectId IS NULL OR :subjectId = subjectId) AND
        (:attendantId IS NULL OR :attendantId = attendantId) AND
        (:sessionId IS NULL OR :sessionId = sessionId) AND
        (:deviceId IS NULL OR :deviceId = deviceId) AND
        (:startTimeLower IS NULL OR :startTimeLower >= addedAt) AND
        (:startTimeUpper IS NULL OR :startTimeUpper >= addedAt) AND
        (:endTimeLower IS NULL OR :endTimeLower >= endedAt) AND
        (:endTimeUpper IS NULL OR :endTimeUpper >= endedAt) order by addedAt desc """)
    suspend fun load(id: String? = null,
                     type: EventType? = null,
                     projectId: String? = null,
                     subjectId: String? = null,
                     attendantId: String? = null,
                     sessionId: String? = null,
                     deviceId: String? = null,
                     startTimeLower: Long? = null,
                     startTimeUpper: Long? = null,
                     endTimeLower: Long? = null,
                     endTimeUpper: Long? = null): List<DbEvent>

    @Query("""
        select * from DbEvent where
        (:id IS NULL OR :id = id) AND
        (:type IS NULL OR :type = type) AND
        (:projectId IS NULL OR :projectId = projectId) AND
        (:subjectId IS NULL OR :subjectId = subjectId) AND
        (:attendantId IS NULL OR :attendantId = attendantId) AND
        (:sessionId IS NULL OR :sessionId = sessionId) AND
        (:deviceId IS NULL OR :deviceId = deviceId) AND
        (:startTimeLower IS NULL OR :startTimeLower >= addedAt) AND
        (:startTimeUpper IS NULL OR :startTimeUpper >= addedAt) AND
        (:endTimeLower IS NULL OR :endTimeLower >= endedAt) AND
        (:endTimeUpper IS NULL OR :endTimeUpper >= endedAt) order by addedAt desc """)
    suspend fun count(id: String? = null,
                      type: EventType? = null,
                      projectId: String? = null,
                      subjectId: String? = null,
                      attendantId: String? = null,
                      sessionId: String? = null,
                      deviceId: String? = null,
                      startTimeLower: Long? = null,
                      startTimeUpper: Long? = null,
                      endTimeLower: Long? = null,
                      endTimeUpper: Long? = null): Int

    @Query("""
        delete from DbEvent where
        (:id IS NULL OR :id = id) AND
        (:type IS NULL OR :type = type) AND
        (:projectId IS NULL OR :projectId = projectId) AND
        (:subjectId IS NULL OR :subjectId = subjectId) AND
        (:attendantId IS NULL OR :attendantId = attendantId) AND
        (:sessionId IS NULL OR :sessionId = sessionId) AND
        (:deviceId IS NULL OR :deviceId = deviceId) AND
        (:startTimeLower IS NULL OR :startTimeLower >= addedAt) AND
        (:startTimeUpper IS NULL OR :startTimeUpper >= addedAt) AND
        (:endTimeLower IS NULL OR :endTimeLower >= endedAt) AND
        (:endTimeUpper IS NULL OR :endTimeUpper >= endedAt)""")
    suspend fun delete(id: String? = null,
                       type: EventType? = null,
                       projectId: String? = null,
                       subjectId: String? = null,
                       attendantId: String? = null,
                       sessionId: String? = null,
                       deviceId: String? = null,
                       startTimeLower: Long? = null,
                       startTimeUpper: Long? = null,
                       endTimeLower: Long? = null,
                       endTimeUpper: Long? = null)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dbEvent: DbEvent)
}
