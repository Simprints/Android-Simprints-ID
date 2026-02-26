package com.simprints.infra.protection.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface AuxDataDao {
    @Query("SELECT * FROM DbAuxData WHERE subjectId = :subjectId")
    suspend fun get(subjectId: String): DbAuxData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(auxData: DbAuxData)

    @Query("DELETE FROM DbAuxData WHERE subjectId = :subjectId")
    suspend fun delete(subjectId: String)

    @Query("DELETE FROM DbAuxData")
    suspend fun deleteAll()
}
