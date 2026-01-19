package com.simprints.matcher

import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.common.FlowType
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.matching.MatchParams

object MatchContract {
    val DESTINATION = R.id.matcherFragment

    fun getParams(
        probeReference: BiometricReferenceCapture,
        bioSdk: ModalitySdkType,
        flowType: FlowType,
        enrolmentRecordQuery: EnrolmentRecordQuery,
        biometricDataSource: BiometricDataSource,
    ) = MatchParams(
        bioSdk = bioSdk,
        probeReference = probeReference,
        flowType = flowType,
        queryForCandidates = enrolmentRecordQuery,
        biometricDataSource = biometricDataSource,
    )
}
