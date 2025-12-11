package com.simprints.infra.matching

import androidx.annotation.Keep
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.ModalitySdkType
import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery

@Keep
data class MatchParams(
    val bioSdk: ModalitySdkType,
    val probeReference: BiometricReferenceCapture,
    val flowType: FlowType,
    val queryForCandidates: SubjectQuery,
    val biometricDataSource: BiometricDataSource,
) : StepParams
