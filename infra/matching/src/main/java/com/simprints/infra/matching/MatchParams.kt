package com.simprints.infra.matching

import androidx.annotation.Keep
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery

@Keep
data class MatchParams(
    val bioSdk: ModalitySdkType,
    val probeReference: BiometricReferenceCapture,
    val flowType: FlowType,
    val queryForCandidates: EnrolmentRecordQuery,
    val biometricDataSource: BiometricDataSource,
) : StepParams
