package com.simprints.infra.images.metadata.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface ImageMetadataDao {
    @Query("SELECT * FROM DbImageMetadata WHERE imageId = :imageId")
    suspend fun get(imageId: String): List<DbImageMetadata>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(metadata: List<DbImageMetadata>)

    @Query("DELETE FROM DbImageMetadata WHERE imageId = :imageId")
    suspend fun delete(imageId: String)

    @Query("DELETE FROM DbImageMetadata")
    suspend fun deleteAll()
}
