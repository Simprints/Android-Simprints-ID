package com.simprints.infra.orchestration.data

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.core.domain.tokenization.TokenizableString
import java.io.Serializable

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = ActionRequest.EnrolActionRequest::class, name = "EnrolActionRequest"),
    JsonSubTypes.Type(value = ActionRequest.IdentifyActionRequest::class, name = "IdentifyActionRequest"),
    JsonSubTypes.Type(value = ActionRequest.VerifyActionRequest::class, name = "VerifyActionRequest"),
    JsonSubTypes.Type(value = ActionRequest.ConfirmIdentityActionRequest::class, name = "ConfirmIdentityActionRequest"),
    JsonSubTypes.Type(value = ActionRequest.EnrolLastBiometricActionRequest::class, name = "EnrolLastBiometricActionRequest"),
)
sealed class ActionRequest(
    open val actionIdentifier: ActionRequestIdentifier,
    open val projectId: String,
    open val userId: TokenizableString,
    // Since maps are not parcelable, we use a list of pairs instead.
    open val unknownExtras: List<Pair<String, Any?>>,
) : Serializable {

    @Keep
    data class EnrolActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: TokenizableString,
        override val moduleId: TokenizableString,
        val metadata: String,
        override val unknownExtras: List<Pair<String, Any?>>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FlowAction

    @Keep
    data class IdentifyActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: TokenizableString,
        override val moduleId: TokenizableString,
        val metadata: String,
        override val unknownExtras: List<Pair<String, Any?>>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FlowAction

    @Keep
    data class VerifyActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: TokenizableString,
        override val moduleId: TokenizableString,
        val metadata: String,
        val verifyGuid: String,
        override val unknownExtras: List<Pair<String, Any?>>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FlowAction

    @Keep
    data class ConfirmIdentityActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: TokenizableString,
        val sessionId: String,
        val selectedGuid: String,
        override val unknownExtras: List<Pair<String, Any?>>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FollowUpAction

    @Keep
    data class EnrolLastBiometricActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: TokenizableString,
        val moduleId: TokenizableString,
        val metadata: String,
        val sessionId: String,
        override val unknownExtras: List<Pair<String, Any?>>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FollowUpAction

    interface FlowAction {
        val moduleId: TokenizableString
    }

    /**
     * Marker interface for requests that are always called as a follow up
     * to another request within the same session.
     */
    interface FollowUpAction
}
