package com.simprints.feature.enrollast

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.enrollast.screen.EnrolLastBiometricFragmentArgs

object EnrolLastBiometricContract {
    val DESTINATION = R.id.enrolLastBiometricFragment

    fun getArgs(
        projectId: String,
        userId: TokenizableString,
        moduleId: TokenizableString,
        steps: List<EnrolLastBiometricStepResult>,
    ) = EnrolLastBiometricFragmentArgs(
        EnrolLastBiometricParams(
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            steps = steps,
        ),
    ).toBundle()
}
