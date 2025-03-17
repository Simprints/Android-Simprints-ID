package com.simprints.infra.realm.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import java.util.UUID

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data model definition for Realm table")
@Entity
class DbSubject(
    @Id var id: Long = 0,
    @Index var subjectUuid: String,
    @Index var projectId: String = "",
    @Index var attendantId: String = "",
    @Index var moduleId: String = "",
    var createdAt: Long? = null, // Store timestamp instead of RealmInstant
    var updatedAt: Long? = null,
    var toSync: Boolean = false,
    var isAttendantIdTokenized: Boolean = false,
    var isModuleIdTokenized: Boolean = false,
) {
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
