package com.simprints.feature.clientapi.models

internal sealed class ActionRequest(
    open val packageName: String,
    open val projectId: String,
    open val userId: String,
    open val unknownExtras: Map<String, Any?>,
) {

    data class EnrolActionRequest(
        override val packageName: String,
        override val projectId: String,
        override val userId: String,
        override val moduleId: String,
        val metadata: String,
        override val unknownExtras: Map<String, Any?>,
    ) : ActionRequest(packageName, projectId, userId, unknownExtras), FlowAction

    data class IdentifyActionRequest(
        override val packageName: String,
        override val projectId: String,
        override val userId: String,
        override val moduleId: String,
        val metadata: String,
        override val unknownExtras: Map<String, Any?>,
    ) : ActionRequest(packageName, projectId, userId, unknownExtras), FlowAction

    data class VerifyActionRequest(
        override val packageName: String,
        override val projectId: String,
        override val userId: String,
        override val moduleId: String,
        val metadata: String,
        val verifyGuid: String,
        override val unknownExtras: Map<String, Any?>,
    ) : ActionRequest(packageName, projectId, userId, unknownExtras), FlowAction

    data class ConfirmActionRequest(
        override val packageName: String,
        override val projectId: String,
        override val userId: String,
        val sessionId: String,
        val selectedGuid: String,
        override val unknownExtras: Map<String, Any?>,
    ) : ActionRequest(packageName, projectId, userId, unknownExtras), FollowUpAction

    data class EnrolLastBiometricActionRequest(
        override val packageName: String,
        override val projectId: String,
        override val userId: String,
        val moduleId: String,
        val metadata: String,
        val sessionId: String,
        override val unknownExtras: Map<String, Any?>,
    ) : ActionRequest(packageName, projectId, userId, unknownExtras), FollowUpAction

    interface FlowAction {
        val moduleId: String
    }

    /**
     * Marker interface for requests that are always called as a follow up
     * to another request within the same session.
     */
    interface FollowUpAction
}
