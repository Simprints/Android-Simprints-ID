package com.simprints.infra.enrolment.records.repository.domain.models

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
sealed class BiometricDataSource : Parcelable {
    open fun callerPackageName(): String = ""

    open fun permissionName(): String? = null

    @Parcelize
    data object Simprints : BiometricDataSource(), Parcelable

    @Parcelize
    data class CommCare(
        private val callerPackageName: String,
    ) : BiometricDataSource(),
        Parcelable {
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
