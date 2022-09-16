package com.simprints.infra.realm.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class DbSubject(
    @PrimaryKey
    @Required
    var subjectId: UUID = UUID.randomUUID(),

    @Required
    var projectId: String = "",

    @Required
    var attendantId: String = "",

    @Required
    var moduleId: String = "",

    var createdAt: Date? = null,

    var updatedAt: Date? = null,

    @Deprecated("See SubjectToEventDbMigrationManagerImpl doc")
    var toSync: Boolean = false,

    var fingerprintSamples: RealmList<DbFingerprintSample> = RealmList(),

    var faceSamples: RealmList<DbFaceSample> = RealmList()

) : RealmObject()
