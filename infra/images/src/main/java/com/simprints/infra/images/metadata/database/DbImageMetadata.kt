package com.simprints.infra.images.metadata.database

import androidx.room.Entity
import com.google.errorprone.annotations.Keep

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
