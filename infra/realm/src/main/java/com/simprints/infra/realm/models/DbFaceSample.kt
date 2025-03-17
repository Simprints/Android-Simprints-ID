package com.simprints.infra.realm.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id
import java.util.UUID

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data model definition for Realm table")
@Entity
class DbFaceSample(
    @Id var id: Long = 0,
    var uuid: String = UUID.randomUUID().toString(),
    var subjectId: String,
    @HnswIndex(dimensions = 66) var template: FloatArray = floatArrayOf(),
    var format: String = "",
)
