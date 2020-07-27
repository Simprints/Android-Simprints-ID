package com.simprints.id.data.db.event.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.local.models.DbEvent

@Dao
interface EventRoomDao {
    
    @Query("""
        select * from DbEvent where
        (:id IS NULL OR :id = id) AND
        (:type IS NULL OR :type = type) AND
        (:projectId IS NULL OR :projectId = projectId) AND
        (:subjectId IS NULL OR :subjectId = subjectId) AND
        (:attendantId IS NULL OR :attendantId = attendantId) AND
        (:sessionId IS NULL OR :sessionId = sessionId) AND
        (:deviceId IS NULL OR :deviceId = deviceId) AND
        (:createdAtLower IS NULL OR :createdAtLower >= createdAt) AND
        (:createdAtUpper IS NULL OR :createdAtUpper <= createdAt) AND
        (:endedAtLower IS NULL OR :endedAtLower >= endedAt) AND
        (:endedAtUpper IS NULL OR :endedAtUpper <= endedAt) order by createdAt desc
    """)
    suspend fun load(id: String? = null,
                     type: EventType? = null,
                     projectId: String? = null,
                     subjectId: String? = null,
                     attendantId: String? = null,
                     sessionId: String? = null,
                     deviceId: String? = null,
                     createdAtLower: Long? = null,
                     createdAtUpper: Long? = null,
                     endedAtLower: Long? = null,
                     endedAtUpper: Long? = null): List<DbEvent>

    @Query("""
        select count(*) from DbEvent where
        (:id IS NULL OR :id = id) AND
        (:type IS NULL OR :type = type) AND
        (:projectId IS NULL OR :projectId = projectId) AND
        (:subjectId IS NULL OR :subjectId = subjectId) AND
        (:attendantId IS NULL OR :attendantId = attendantId) AND
        (:sessionId IS NULL OR :sessionId = sessionId) AND
        (:deviceId IS NULL OR :deviceId = deviceId) AND
        (:createdAtLower IS NULL OR :createdAtLower >= createdAt) AND
        (:createdAtUpper IS NULL OR :createdAtUpper <= createdAt) AND
        (:endedAtLower IS NULL OR :endedAtLower >= endedAt) AND
        (:endedAtUpper IS NULL OR :endedAtUpper <= endedAt)
    """)
    suspend fun count(id: String? = null,
                      type: EventType? = null,
                      projectId: String? = null,
                      subjectId: String? = null,
                      attendantId: String? = null,
                      sessionId: String? = null,
                      deviceId: String? = null,
                      createdAtLower: Long? = null,
                      createdAtUpper: Long? = null,
                      endedAtLower: Long? = null,
                      endedAtUpper: Long? = null): Int

    @Query("""
        delete from DbEvent where
        (:id IS NULL OR :id = id) AND
        (:type IS NULL OR :type = type) AND
        (:projectId IS NULL OR :projectId = projectId) AND
        (:subjectId IS NULL OR :subjectId = subjectId) AND
        (:attendantId IS NULL OR :attendantId = attendantId) AND
        (:sessionId IS NULL OR :sessionId = sessionId) AND
        (:deviceId IS NULL OR :deviceId = deviceId) AND
        (:createdAtLower IS NULL OR :createdAtLower >= createdAt) AND
        (:createdAtUpper IS NULL OR :createdAtUpper <= createdAt) AND
        (:endedAtLower IS NULL OR :endedAtLower >= endedAt) AND
        (:endedAtUpper IS NULL OR :endedAtUpper <= endedAt)""")
    suspend fun delete(id: String? = null,
                       type: EventType? = null,
                       projectId: String? = null,
                       subjectId: String? = null,
                       attendantId: String? = null,
                       sessionId: String? = null,
                       deviceId: String? = null,
                       createdAtLower: Long? = null,
                       createdAtUpper: Long? = null,
                       endedAtLower: Long? = null,
                       endedAtUpper: Long? = null)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dbEvent: DbEvent)

}
