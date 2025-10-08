package com.simprints.feature.externalcredential

import com.simprints.core.domain.common.FlowType
import com.simprints.feature.externalcredential.model.ExternalCredentialParams

object ExternalCredentialContract {
    val DESTINATION = R.id.externalCredentialControllerFragment

    fun getParams(
        subjectId: String?,
        flowType: FlowType,
    ) = ExternalCredentialParams(subjectId = subjectId, flowType = flowType)
}
