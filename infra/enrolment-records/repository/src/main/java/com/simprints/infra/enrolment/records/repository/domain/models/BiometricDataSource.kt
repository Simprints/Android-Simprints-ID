package com.simprints.infra.enrolment.records.repository.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.permission.CommCarePermissions
import com.simprints.core.domain.step.StepParams

@Keep
sealed class BiometricDataSource : StepParams {
    open fun callerPackageName(): String = ""

    open fun permissionName(): String? = null

    data object Simprints : BiometricDataSource()

    data class CommCare(
        val callerPackageName: String,
    ) : BiometricDataSource() {
        override fun callerPackageName() = callerPackageName

        override fun permissionName() = CommCarePermissions.buildPermissionForPackage(callerPackageName)
    }

    companion object {
        const val COMMCARE = "COMMCARE"

        fun fromString(
            value: String,
            callerPackageName: String,
        ) = when (value.uppercase()) {
            COMMCARE -> CommCare(callerPackageName)
            else -> Simprints
        }
    }
}
