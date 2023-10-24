package com.simprints.infra.orchestration.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ActionRequestIdentifier(
    val actionName: String,
    val packageName: String,
): Parcelable {

    companion object {

        fun fromIntentAction(action: String): ActionRequestIdentifier {
            val packageName = action.substringBeforeLast(".")
            val actionName = action.substringAfterLast(".")
            return ActionRequestIdentifier(actionName, packageName)
        }
    }
}
