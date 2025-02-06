package com.simprints.infra.realm.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.relation.ToMany
import java.util.UUID

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data model definition for Realm table")
@Entity
class DbSubject(
    @Id var id: Long = 0,
    @Index var subjectUuid: String = UUID.randomUUID().toString(),
    @Index var projectId: String = "",
    @Index var attendantId: String = "",
    @Index var moduleId: String = "",
    var createdAt: Long? = null, // Store timestamp instead of RealmInstant
    var updatedAt: Long? = null,
    var toSync: Boolean = false,
    var isAttendantIdTokenized: Boolean = false,
    var isModuleIdTokenized: Boolean = false,
) {
    @Backlink(to = "subject")
    var fingerprintSamples: ToMany<DbFingerprintSample> = ToMany(this, DbSubject_.fingerprintSamples)

    @Backlink(to = "subject")
    var faceSamples: ToMany<DbFaceSample> = ToMany(this, DbSubject_.faceSamples)

    constructor() : this(
        id = 0,
        subjectUuid = UUID.randomUUID().toString(),
        projectId = "",
        attendantId = "",
        moduleId = "",
        createdAt = null,
        updatedAt = null,
        toSync = false,
        isAttendantIdTokenized = false,
        isModuleIdTokenized = false,
    )
}
