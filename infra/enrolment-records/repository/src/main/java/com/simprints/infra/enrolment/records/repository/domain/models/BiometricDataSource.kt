package com.simprints.infra.enrolment.records.repository.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams

@Keep
sealed class BiometricDataSource : StepParams {
    open fun callerPackageName(): String = ""

    open fun permissionName(): String? = null

    data object Simprints : BiometricDataSource()

    data class CommCare(
        private val callerPackageName: String,
    ) : BiometricDataSource() {
        override fun callerPackageName() = callerPackageName

        override fun permissionName() = "$callerPackageName.provider.cases.read"
    }

    companion object {
        fun fromString(
            value: String,
            callerPackageName: String,
        ) = when (value.uppercase()) {
            "COMMCARE" -> CommCare(callerPackageName)
            else -> Simprints
        }
    }
}
