package com.simprints.feature.enrollast

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.enrollast.screen.EnrolLastBiometricFragmentArgs

object EnrolLastBiometricContract {

    const val ENROL_LAST_RESULT = "enrol_last_biometric_result"

    fun getArgs(
        projectId: String,
        userId: TokenizableString,
        moduleId: TokenizableString,
        steps: List<EnrolLastBiometricStepResult>,
    ) = EnrolLastBiometricFragmentArgs(EnrolLastBiometricParams(
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        steps = steps,
    )).toBundle()
}
