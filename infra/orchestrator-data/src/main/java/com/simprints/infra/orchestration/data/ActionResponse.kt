package com.simprints.infra.orchestration.data

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.infra.orchestration.data.responses.AppMatchResult

@ExcludedFromGeneratedTestCoverageReports("Data struct")
sealed class ActionResponse(
    open val actionIdentifier: ActionRequestIdentifier,
    open val sessionId: String,
) {
    @ExcludedFromGeneratedTestCoverageReports("Data struct")
    data class EnrolActionResponse(
        override val actionIdentifier: ActionRequestIdentifier,
        override val sessionId: String,
        val enrolledGuid: String,
        val subjectActions: String?,
    ) : ActionResponse(actionIdentifier, sessionId)

    @ExcludedFromGeneratedTestCoverageReports("Data struct")
    data class IdentifyActionResponse(
        override val actionIdentifier: ActionRequestIdentifier,
        override val sessionId: String,
        val identifications: List<AppMatchResult>,
    ) : ActionResponse(actionIdentifier, sessionId)

    @ExcludedFromGeneratedTestCoverageReports("Data struct")
    data class ConfirmActionResponse(
        override val actionIdentifier: ActionRequestIdentifier,
        override val sessionId: String,
        val confirmed: Boolean,
    ) : ActionResponse(actionIdentifier, sessionId)

    @ExcludedFromGeneratedTestCoverageReports("Data struct")
    data class VerifyActionResponse(
        override val actionIdentifier: ActionRequestIdentifier,
        override val sessionId: String,
        val matchResult: AppMatchResult,
    ) : ActionResponse(actionIdentifier, sessionId)

    @ExcludedFromGeneratedTestCoverageReports("Data struct")
    data class ExitFormActionResponse(
        override val actionIdentifier: ActionRequestIdentifier,
        override val sessionId: String,
        val reason: String,
        val extraText: String,
    ) : ActionResponse(actionIdentifier, sessionId)

    @ExcludedFromGeneratedTestCoverageReports("Data struct")
    data class ErrorActionResponse(
        override val actionIdentifier: ActionRequestIdentifier,
        override val sessionId: String,
        val reason: AppErrorReason,
        val flowCompleted: Boolean,
    ) : ActionResponse(actionIdentifier, sessionId)
}
