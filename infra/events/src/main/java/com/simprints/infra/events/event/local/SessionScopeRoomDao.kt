package com.simprints.infra.events.event.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simprints.infra.events.event.local.models.DbSessionScope

@Dao
internal interface SessionScopeRoomDao {

    @Query("select * from DbSessionScope order by createdAt desc")
    suspend fun loadAll(): List<DbSessionScope>

    @Query("select * from DbSessionScope where endedAt IS NULL order by createdAt desc")
    suspend fun loadOpen(): List<DbSessionScope>

    @Query("select count(*) from DbSessionScope")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dbEvent: DbSessionScope)

    @Query("delete from DbSessionScope where id in (:ids)")
    suspend fun delete(ids: List<String>)

    @Query("delete from DbSessionScope")
    suspend fun deleteAll()
}
