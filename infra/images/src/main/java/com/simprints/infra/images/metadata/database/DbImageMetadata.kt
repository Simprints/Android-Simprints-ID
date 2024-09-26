package com.simprints.infra.images.metadata.database

import androidx.annotation.Keep
import androidx.room.Entity

@Entity(
    tableName = "DbImageMetadata",
    primaryKeys = ["imageId", "key"],
)
@Keep
internal data class DbImageMetadata(
    val imageId: String,
    val key: String,
    val value: String,
)
