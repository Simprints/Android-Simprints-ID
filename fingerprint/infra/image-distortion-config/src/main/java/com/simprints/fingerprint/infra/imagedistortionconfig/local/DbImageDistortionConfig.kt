package com.simprints.fingerprint.infra.imagedistortionconfig.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "DbImageDistortionConfig")
internal data class DbImageDistortionConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val scannerId: String,
    val serialNumber: String,
    val configFile: ByteArray,
    val isUploaded: Boolean,
)
