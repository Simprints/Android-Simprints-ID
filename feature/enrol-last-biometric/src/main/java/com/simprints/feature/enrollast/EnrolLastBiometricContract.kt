package com.simprints.feature.enrollast

import com.simprints.feature.enrollast.screen.EnrolLastBiometricFragmentArgs

object EnrolLastBiometricContract {

    val DESTINATION_ID = R.id.enrolLastBiometricFragment
    val RESULT_CLASS = EnrolLastBiometricResult::class.java

    const val ENROL_LAST_RESULT = "enrol_last_biometric_result"

    fun getArgs(
        projectId: String,
        userId: String,
        moduleId: String,
        steps: List<EnrolLastBiometricStepResult>,
    ) = EnrolLastBiometricFragmentArgs(EnrolLastBiometricParams(
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        steps = steps,
    )).toBundle()
}
