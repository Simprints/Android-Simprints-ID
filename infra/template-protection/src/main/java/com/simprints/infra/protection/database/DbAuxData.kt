package com.simprints.infra.protection.database

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "DbAuxData",
)
@Keep
internal data class DbAuxData(
    @PrimaryKey var subjectId: String = "",
    var exponents: String,
    var coefficients: String,
)
