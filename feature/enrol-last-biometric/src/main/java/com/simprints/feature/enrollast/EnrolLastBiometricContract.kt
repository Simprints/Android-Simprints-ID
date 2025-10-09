package com.simprints.feature.enrollast

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential

object EnrolLastBiometricContract {
    val DESTINATION = R.id.enrolLastBiometricFragment

    fun getParams(
        projectId: String,
        userId: TokenizableString,
        moduleId: TokenizableString,
        steps: List<EnrolLastBiometricStepResult>,
        scannedCredential: ScannedCredential?,
    ) = EnrolLastBiometricParams(
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        steps = steps,
        scannedCredential = scannedCredential,
    )
}
