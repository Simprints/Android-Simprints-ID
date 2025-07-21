package com.simprints.feature.enrollast

import com.simprints.core.domain.tokenization.TokenizableString

object EnrolLastBiometricContract {
    val DESTINATION = R.id.enrolLastBiometricFragment

    fun getParams(
        projectId: String,
        userId: TokenizableString,
        moduleId: TokenizableString,
        steps: List<EnrolLastBiometricStepResult>,
    ) = EnrolLastBiometricParams(
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        steps = steps,
    )
}
