package com.simprints.infra.events.event.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simprints.infra.events.event.local.models.DbEventScope

@Dao
internal interface SessionScopeRoomDao {

    @Query("select * from DbEventScope where end_unixMs IS NULL order by start_unixMs desc")
    suspend fun loadOpen(): List<DbEventScope>

    @Query("select * from DbEventScope where end_unixMs IS NOT NULL order by start_unixMs desc")
    suspend fun loadClosed(): List<DbEventScope>

    @Query("select count(*) from DbEventScope")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dbEvent: DbEventScope)

    @Query("delete from DbEventScope where id in (:ids)")
    suspend fun delete(ids: List<String>)

    @Query("delete from DbEventScope")
    suspend fun deleteAll()
}
