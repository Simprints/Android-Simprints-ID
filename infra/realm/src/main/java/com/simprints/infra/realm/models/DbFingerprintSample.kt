package com.simprints.infra.realm.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data model definition for Realm table")
class DbFingerprintSample : RealmObject {
    @PrimaryKey
    var id: String = ""
    var fingerIdentifier: Int = -1
    var template: ByteArray = byteArrayOf()
    var templateQualityScore: Int = -1
    var format: String = ""
}
