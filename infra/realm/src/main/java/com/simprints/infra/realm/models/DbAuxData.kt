package com.simprints.infra.realm.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data model definition for Realm table")
class DbAuxData : RealmObject {

    @PrimaryKey
    var subjectId: String = ""
    var exponents: RealmList<Int> = realmListOf()
    var coefficients: RealmList<Int> = realmListOf()
}
