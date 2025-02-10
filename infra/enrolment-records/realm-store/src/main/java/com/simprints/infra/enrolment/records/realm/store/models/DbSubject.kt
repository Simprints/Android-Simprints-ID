package com.simprints.infra.enrolment.records.realm.store.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data model definition for Realm table")
class DbSubject : RealmObject {
    @PrimaryKey
    var subjectId: RealmUUID = RealmUUID.random()
    var projectId: String = ""
    var attendantId: String = ""
    var moduleId: String = ""
    var createdAt: RealmInstant? = null
    var updatedAt: RealmInstant? = null

    var fingerprintSamples: RealmList<DbFingerprintSample> = realmListOf()
    var faceSamples: RealmList<DbFaceSample> = realmListOf()

    var isAttendantIdTokenized: Boolean = false
    var isModuleIdTokenized: Boolean = false
}
