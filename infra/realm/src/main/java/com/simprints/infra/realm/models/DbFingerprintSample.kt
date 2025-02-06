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
data class DbFingerprintSample(
    @Id var id: Long = 0,
    var uuid: String = UUID.randomUUID().toString(),
    var fingerIdentifier: Int = -1,
    var template: ByteArray = byteArrayOf(),
    var templateQualityScore: Int = -1,
    var format: String = "",
) {
    lateinit var subject: ToOne<DbSubject>
}
