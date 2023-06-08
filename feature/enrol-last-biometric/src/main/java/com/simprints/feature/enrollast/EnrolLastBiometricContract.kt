package com.simprints.feature.enrollast

import com.simprints.feature.enrollast.screen.EnrolLastBiometricFragmentArgs

object EnrolLastBiometricContract {

    const val ENROL_LAST_RESULT = "enrol_last_biometric_result"

    fun getArgs(
        projectId: String,
        moduleId: String,
        userId: String,
    ) = EnrolLastBiometricFragmentArgs(EnrolLastBiometricParams(
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
    )).toBundle()
}
