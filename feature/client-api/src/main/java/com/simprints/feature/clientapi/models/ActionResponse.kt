package com.simprints.feature.clientapi.models

import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppMatchResult

internal sealed class ActionResponse(
    open val request: ActionRequest,
    open val sessionId: String,
    open val eventsJson: String?,
) {

    data class EnrolActionResponse(
        override val request: ActionRequest,
        override val sessionId: String,
        override val eventsJson: String?,

        val enrolledGuid: String,
        val subjectActions: String?
    ) : ActionResponse(request, sessionId, eventsJson)

    data class IdentifyActionResponse(
        override val request: ActionRequest,
        override val sessionId: String,
        override val eventsJson: String?,

        val identifications: List<IAppMatchResult>,
    ) : ActionResponse(request, sessionId, eventsJson)

    data class ConfirmActionResponse(
        override val request: ActionRequest,
        override val sessionId: String,
        override val eventsJson: String?,

        val confirmed: Boolean,
    ) : ActionResponse(request, sessionId, eventsJson)

    data class VerifyActionResponse(
        override val request: ActionRequest,
        override val sessionId: String,
        override val eventsJson: String?,

        val matchResult: IAppMatchResult,
    ) : ActionResponse(request, sessionId, eventsJson)

    data class ExitFormActionResponse(
        override val request: ActionRequest,
        override val sessionId: String,
        override val eventsJson: String?,

        val reason: String,
        val extraText: String,
    ) : ActionResponse(request, sessionId, eventsJson)

    data class ErrorActionResponse(
        override val request: ActionRequest,
        override val sessionId: String,
        override val eventsJson: String?,

        val reason: IAppErrorReason,
        val flowCompleted: Boolean,
    ) : ActionResponse(request, sessionId, eventsJson)

}
