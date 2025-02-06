package com.simprints.infra.realm.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import java.util.UUID

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data model definition for Realm table")
@Entity
class DbFaceSample(
    @Id var id: Long = 0, // ObjectBox requires a Long id
    var uuid: String = UUID.randomUUID().toString(),
    var template: ByteArray = byteArrayOf(),
    var format: String = "",
) {
    lateinit var subject: ToOne<DbSubject>
}
