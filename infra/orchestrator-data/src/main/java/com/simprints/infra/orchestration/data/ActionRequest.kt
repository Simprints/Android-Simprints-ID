package com.simprints.infra.orchestration.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.tokenization.TokenizableString
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@ExcludedFromGeneratedTestCoverageReports("This is a simple data class")
@Serializable
@Parcelize
sealed class ActionRequest : Parcelable {
    abstract val actionIdentifier: ActionRequestIdentifier
    abstract val projectId: String
    abstract val userId: TokenizableString
    abstract val metadata: String
    abstract val unknownExtras: Map<String, String?>

    fun getSubjectAgeIfAvailable(): Int? = when (this) {
        is EnrolActionRequest -> subjectAge
        is IdentifyActionRequest -> subjectAge
        is VerifyActionRequest -> subjectAge
        else -> null
    }

    @Keep
    @Serializable
    @SerialName("EnrolActionRequest")
    data class EnrolActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: TokenizableString,
        override val moduleId: TokenizableString,
        val biometricDataSource: String,
        val subjectAge: Int? = null,
        override val metadata: String,
        override val unknownExtras: Map<String, String?>,
    ) : ActionRequest(),
        FlowAction

    @Keep
    @Serializable
    @SerialName("IdentifyActionRequest")
    data class IdentifyActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: TokenizableString,
        override val moduleId: TokenizableString,
        val biometricDataSource: String,
        val subjectAge: Int? = null,
        override val metadata: String,
        override val unknownExtras: Map<String, String?>,
    ) : ActionRequest(),
        FlowAction

    @Keep
    @Serializable
    @SerialName("VerifyActionRequest")
    data class VerifyActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: TokenizableString,
        override val moduleId: TokenizableString,
        val biometricDataSource: String,
        val subjectAge: Int? = null,
        val verifyGuid: String,
        override val metadata: String,
        override val unknownExtras: Map<String, String?>,
    ) : ActionRequest(),
        FlowAction

    @Keep
    @Serializable
    @SerialName("ConfirmIdentityActionRequest")
    data class ConfirmIdentityActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: TokenizableString,
        val sessionId: String,
        val selectedGuid: String,
        override val metadata: String,
        override val unknownExtras: Map<String, String?>,
    ) : ActionRequest(),
        FollowUpAction

    @Keep
    @Serializable
    @SerialName("EnrolLastBiometricActionRequest")
    data class EnrolLastBiometricActionRequest(
        override val actionIdentifier: ActionRequestIdentifier,
        override val projectId: String,
        override val userId: TokenizableString,
        val moduleId: TokenizableString,
        val sessionId: String,
        override val metadata: String,
        override val unknownExtras: Map<String, String?>,
    ) : ActionRequest(),
        FollowUpAction

    interface FlowAction {
        val moduleId: TokenizableString
    }

    /**
     * Marker interface for requests that are always called as a follow up
     * to another request within the same session.
     */
    interface FollowUpAction
}
