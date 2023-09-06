package com.simprints.feature.clientapi.models

internal sealed class ActionRequest(
    open val actionIdentifier: ActionRequestIdentifier,
    open val projectId: String,
    open val userId: String,
    open val unknownExtras: Map<String, Any?>,
) {

    data class EnrolActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: String,
        override val moduleId: String,
        val metadata: String,
        override val unknownExtras: Map<String, Any?>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FlowAction

    data class IdentifyActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: String,
        override val moduleId: String,
        val metadata: String,
        override val unknownExtras: Map<String, Any?>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FlowAction

    data class VerifyActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: String,
        override val moduleId: String,
        val metadata: String,
        val verifyGuid: String,
        override val unknownExtras: Map<String, Any?>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FlowAction

    data class ConfirmActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: String,
        val sessionId: String,
        val selectedGuid: String,
        override val unknownExtras: Map<String, Any?>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FollowUpAction

    data class EnrolLastBiometricActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: String,
        val moduleId: String,
        val metadata: String,
        val sessionId: String,
        override val unknownExtras: Map<String, Any?>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FollowUpAction

    interface FlowAction {
        val moduleId: String
    }

    /**
     * Marker interface for requests that are always called as a follow up
     * to another request within the same session.
     */
    interface FollowUpAction
}
