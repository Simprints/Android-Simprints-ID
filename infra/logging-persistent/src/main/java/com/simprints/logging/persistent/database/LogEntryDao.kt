package com.simprints.logging.persistent.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface LogEntryDao {
    @Query("SELECT * FROM DbLogEntry WHERE type = :type ORDER BY timestampMs DESC")
    suspend fun getByType(type: String): List<DbLogEntry>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun save(metadata: DbLogEntry)

    @Query("DELETE FROM DbLogEntry WHERE expiresAtMs < :currentMs")
    suspend fun prune(currentMs: Long)

    @Query("DELETE FROM DbLogEntry")
    suspend fun deleteAll()
}
