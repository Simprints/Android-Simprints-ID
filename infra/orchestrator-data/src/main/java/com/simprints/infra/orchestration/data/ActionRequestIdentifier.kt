package com.simprints.infra.orchestration.data

data class ActionRequestIdentifier(
    val actionName: String,
    val packageName: String,
) {

    companion object {

        fun fromIntentAction(action: String): ActionRequestIdentifier {
            val packageName = action.substringBeforeLast(".")
            val actionName = action.substringAfterLast(".")
            return ActionRequestIdentifier(actionName, packageName)
        }
    }
}
