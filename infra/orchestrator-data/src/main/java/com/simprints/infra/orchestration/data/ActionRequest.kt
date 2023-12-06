package com.simprints.infra.orchestration.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import kotlinx.parcelize.Parcelize

sealed class ActionRequest(
    open val actionIdentifier: ActionRequestIdentifier,
    open val projectId: String,
    open val userId: TokenizableString,
    // Since maps are not parcelable, we use a list of pairs instead.
    open val unknownExtras: List<Pair<String, Any?>>,
) : Parcelable {

    @Keep
    @Parcelize
    data class EnrolActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: TokenizableString,
        override val moduleId: TokenizableString,
        val metadata: String,
        override val unknownExtras: List<Pair<String, Any?>>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FlowAction

    @Keep
    @Parcelize
    data class IdentifyActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: TokenizableString,
        override val moduleId: TokenizableString,
        val metadata: String,
        override val unknownExtras: List<Pair<String, Any?>>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FlowAction

    @Keep
    @Parcelize
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
    @Parcelize
    data class ConfirmIdentityActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: TokenizableString,
        val sessionId: String,
        val selectedGuid: String,
        override val unknownExtras: List<Pair<String, Any?>>,
    ) : ActionRequest(actionIdentifier, projectId, userId, unknownExtras), FollowUpAction

    @Keep
    @Parcelize
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
