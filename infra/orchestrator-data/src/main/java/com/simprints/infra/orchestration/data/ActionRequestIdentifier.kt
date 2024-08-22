package com.simprints.infra.orchestration.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ActionRequestIdentifier(
    val actionName: String,
    val packageName: String,
    val callerPackageName: String,
    val contractVersion: Int,
) : Parcelable {

    companion object {

        fun fromIntentAction(
            action: String,
            callerPackageName: String = "",
            callerVersion: Int = 1,
        ) = ActionRequestIdentifier(
            actionName = action.substringAfterLast("."),
            packageName = action.substringBeforeLast("."),
            callerPackageName,
            callerVersion,
        )
    }
}
