package com.simprints.infra.orchestration.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ActionRequestIdentifier(
    val actionName: String,
    val packageName: String,
    val callerPackageName: String,
    val contractVersion: Int,
    val timestampMs: Long,
) : Parcelable {
    override fun toString(): String = "Intent: $packageName.$actionName\nCaller: $callerPackageName (contract version: $contractVersion)"

    companion object {
        fun fromIntentAction(
            timestampMs: Long,
            action: String,
            callerPackageName: String = "",
            callerVersion: Int = 1,
        ) = ActionRequestIdentifier(
            actionName = action.substringAfterLast("."),
            packageName = action.substringBeforeLast("."),
            callerPackageName,
            callerVersion,
            timestampMs,
        )
    }
}
