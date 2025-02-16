package com.simprints.infra.enrolment.records.store.commcare.model

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.subject.BiometricReference

@Keep
data class BiometricReferenceWithId(
    val subjectId: String,
    val attendantId: String,
    val moduleId: String,
    val projectId: String,
    val biometricReferences: List<BiometricReference>
)
