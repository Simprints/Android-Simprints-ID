
package com.simprints.fingerprint.infra.imagedistortionconfig.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface ImageDistortionConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: DbImageDistortionConfig)

    @Query("SELECT * FROM DbImageDistortionConfig WHERE scannerId = :scannerId")
    suspend fun getConfigByScannerId(scannerId: String): DbImageDistortionConfig?

    @Query("SELECT * FROM DbImageDistortionConfig WHERE isUploaded = 0")
    suspend fun getPendingUploads(): List<DbImageDistortionConfig>

    @Query("UPDATE DbImageDistortionConfig SET isUploaded = 1 WHERE id = :configId")
    suspend fun markAsUploaded(configId: Int)
}
