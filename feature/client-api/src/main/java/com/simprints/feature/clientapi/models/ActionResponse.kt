package com.simprints.feature.clientapi.models

import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppMatchResult

internal sealed class ActionResponse(
    open val actionIdentifier: ActionRequestIdentifier,
    open val sessionId: String,
    open val eventsJson: String?,
) {

    data class EnrolActionResponse(
        override val actionIdentifier: ActionRequestIdentifier,
        override val sessionId: String,
        override val eventsJson: String?,

        val enrolledGuid: String,
        val subjectActions: String?
    ) : ActionResponse(actionIdentifier, sessionId, eventsJson)

    data class IdentifyActionResponse(
        override val actionIdentifier: ActionRequestIdentifier,
        override val sessionId: String,
        override val eventsJson: String?,

        val identifications: List<IAppMatchResult>,
    ) : ActionResponse(actionIdentifier, sessionId, eventsJson)

    data class ConfirmActionResponse(
        override val actionIdentifier: ActionRequestIdentifier,
        override val sessionId: String,
        override val eventsJson: String?,

        val confirmed: Boolean,
    ) : ActionResponse(actionIdentifier, sessionId, eventsJson)

    data class VerifyActionResponse(
        override val actionIdentifier: ActionRequestIdentifier,
        override val sessionId: String,
        override val eventsJson: String?,

        val matchResult: IAppMatchResult,
    ) : ActionResponse(actionIdentifier, sessionId, eventsJson)

    data class ExitFormActionResponse(
        override val actionIdentifier: ActionRequestIdentifier,
        override val sessionId: String,
        override val eventsJson: String?,

        val reason: String,
        val extraText: String,
    ) : ActionResponse(actionIdentifier, sessionId, eventsJson)

    data class ErrorActionResponse(
        override val actionIdentifier: ActionRequestIdentifier,
        override val sessionId: String,
        override val eventsJson: String?,

        val reason: IAppErrorReason,
        val flowCompleted: Boolean,
    ) : ActionResponse(actionIdentifier, sessionId, eventsJson)

}
