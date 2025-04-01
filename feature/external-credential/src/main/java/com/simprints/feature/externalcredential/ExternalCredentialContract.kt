package com.simprints.feature.externalcredential

import android.os.Bundle
import com.simprints.core.domain.common.FlowType
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialControllerFragmentArgs

object ExternalCredentialContract {
    val DESTINATION = R.id.externalCredentialControllerFragment

    fun getArgs(subjectId: String?, flowType: FlowType): Bundle =
        ExternalCredentialControllerFragmentArgs(ExternalCredentialParams(subjectId = subjectId, flowType = flowType)).toBundle()

}
