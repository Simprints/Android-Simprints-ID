package com.simprints.infra.orchestration.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ActionRequest(
    open val actionIdentifier: ActionRequestIdentifier,
    open val projectId: String,
    open val userId: String,
    // Since maps are not parcelable, we use a list of pairs instead.
    open val unknownExtras: List<Pair<String, Any?>>,
) : Parcelable {

    @Parcelize
    data class EnrolActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: String,
        override val moduleId: String,
        val metadata: String,
        override val unknownExtras: List<Pair<String, Any?>>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FlowAction

    @Parcelize
    data class IdentifyActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: String,
        override val moduleId: String,
        val metadata: String,
        override val unknownExtras: List<Pair<String, Any?>>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FlowAction

    @Parcelize
    data class VerifyActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: String,
        override val moduleId: String,
        val metadata: String,
        val verifyGuid: String,
        override val unknownExtras: List<Pair<String, Any?>>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FlowAction

    @Parcelize
    data class ConfirmIdentityActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: String,
        val sessionId: String,
        val selectedGuid: String,
        override val unknownExtras: List<Pair<String, Any?>>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FollowUpAction

    @Parcelize
    data class EnrolLastBiometricActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: String,
        val moduleId: String,
        val metadata: String,
        val sessionId: String,
        override val unknownExtras: List<Pair<String, Any?>>,
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
