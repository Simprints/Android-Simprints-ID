package com.simprints.infra.enrolment.records.room.store.models

import androidx.room.Embedded
import androidx.room.Relation
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_ID_COLUMN

data class SubjectBiometrics(
    @Embedded val subject: DbSubject,
    @Relation(
        parentColumn = SUBJECT_ID_COLUMN,
        entityColumn = SUBJECT_ID_COLUMN,
    )
    val biometricTemplates: List<DbBiometricTemplate>,
    @Relation(
        parentColumn = SUBJECT_ID_COLUMN,
        entityColumn = SUBJECT_ID_COLUMN,
    )
    val externalCredentials: List<DbExternalCredential>,
)
