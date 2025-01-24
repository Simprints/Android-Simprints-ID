package com.simprints.feature.clientapi.mappers.request

import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.builders.ConfirmIdentifyRequestBuilder
import com.simprints.feature.clientapi.mappers.request.builders.EnrolLastBiometricsRequestBuilder
import com.simprints.feature.clientapi.mappers.request.builders.EnrolRequestBuilder
import com.simprints.feature.clientapi.mappers.request.builders.IdentifyRequestBuilder
import com.simprints.feature.clientapi.mappers.request.builders.VerifyRequestBuilder
import com.simprints.feature.clientapi.mappers.request.extractors.ConfirmIdentityRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.EnrolLastBiometricsRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.EnrolRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.IdentifyRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.VerifyRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.odk.OdkEnrolRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.odk.OdkIdentifyRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.odk.OdkVerifyRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.ConfirmIdentityValidator
import com.simprints.feature.clientapi.mappers.request.validators.EnrolLastBiometricsValidator
import com.simprints.feature.clientapi.mappers.request.validators.EnrolValidator
import com.simprints.feature.clientapi.mappers.request.validators.IdentifyValidator
import com.simprints.feature.clientapi.mappers.request.validators.VerifyValidator
import com.simprints.feature.clientapi.models.ClientApiConstants
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.feature.clientapi.models.CommCareConstants
import com.simprints.feature.clientapi.models.LibSimprintsConstants
import com.simprints.feature.clientapi.models.OdkConstants
import com.simprints.feature.clientapi.usecases.GetCurrentSessionIdUseCase
import com.simprints.feature.clientapi.usecases.SessionHasIdentificationCallbackUseCase
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.orchestration.data.ActionConstants
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import com.simprints.libsimprints.Constants
import javax.inject.Inject

internal class IntentToActionMapper @Inject constructor(
    private val getCurrentSessionId: GetCurrentSessionIdUseCase,
    private val sessionHasIdentificationCallback: SessionHasIdentificationCallbackUseCase,
    private val tokenizationProcessor: TokenizationProcessor,
    private val timeHelper: TimeHelper,
) {
    suspend operator fun invoke(
        action: String,
        extras: Map<String, Any>,
        project: Project?,
    ): ActionRequest {
        val actionIdentifier = ActionRequestIdentifier.fromIntentAction(
            timestampMs = timeHelper.now().ms,
            action = action,
            callerPackageName = extras[ClientApiConstants.CALLER_PACKAGE_NAME]?.let { it as? String }.orEmpty(),
            callerVersion = extras[Constants.SIMPRINTS_LIB_VERSION]?.let { it as? Int } ?: 1,
        )

        return when (actionIdentifier.packageName) {
            OdkConstants.PACKAGE_NAME -> mapOdkAction(actionIdentifier, extras, project)
            CommCareConstants.PACKAGE_NAME -> mapCommCareAction(actionIdentifier, extras, project)
            LibSimprintsConstants.PACKAGE_NAME -> mapLibSimprintsAction(actionIdentifier, extras, project)
            else -> throw InvalidRequestException(
                "Unsupported package name",
                ClientApiError.INVALID_STATE_FOR_INTENT_ACTION,
            )
        }
    }

    private suspend fun mapOdkAction(
        actionIdentifier: ActionRequestIdentifier,
        extras: Map<String, Any>,
        project: Project?,
    ): ActionRequest {
        val build = when (actionIdentifier.actionName) {
            ActionConstants.ACTION_ENROL -> enrolBuilder(
                actionIdentifier = actionIdentifier,
                extractor = OdkEnrolRequestExtractor(extras, OdkConstants.acceptableExtras),
                project = project,
            )

            ActionConstants.ACTION_VERIFY -> verifyBuilder(
                actionIdentifier = actionIdentifier,
                extractor = OdkVerifyRequestExtractor(extras, OdkConstants.acceptableExtras),
                project = project,
            )

            ActionConstants.ACTION_IDENTIFY -> identifyBuilder(
                actionIdentifier = actionIdentifier,
                extractor = OdkIdentifyRequestExtractor(extras, OdkConstants.acceptableExtras),
                project = project,
            )

            ActionConstants.ACTION_ENROL_LAST_BIOMETRICS -> enrolLastBiometricsBuilder(
                actionIdentifier = actionIdentifier,
                extractor = EnrolLastBiometricsRequestExtractor(extras),
                project = project,
            )

            ActionConstants.ACTION_CONFIRM_IDENTITY -> confirmIdentifyBuilder(
                actionIdentifier = actionIdentifier,
                extractor = ConfirmIdentityRequestExtractor(extras),
                project = project,
            )

            else -> throw InvalidRequestException(
                "Invalid ODK action",
                ClientApiError.INVALID_STATE_FOR_INTENT_ACTION,
            )
        }.build()
        return build
    }

    private suspend fun mapCommCareAction(
        actionIdentifier: ActionRequestIdentifier,
        extras: Map<String, Any>,
        project: Project?,
    ) = when (actionIdentifier.actionName) {
        ActionConstants.ACTION_ENROL -> enrolBuilder(
            actionIdentifier = actionIdentifier,
            extractor = EnrolRequestExtractor(extras),
            project = project,
        )

        ActionConstants.ACTION_VERIFY -> verifyBuilder(
            actionIdentifier = actionIdentifier,
            extractor = VerifyRequestExtractor(extras),
            project = project,
        )

        ActionConstants.ACTION_IDENTIFY -> identifyBuilder(
            actionIdentifier = actionIdentifier,
            extractor = IdentifyRequestExtractor(extras),
            project = project,
        )
        ActionConstants.ACTION_ENROL_LAST_BIOMETRICS -> enrolLastBiometricsBuilder(
            actionIdentifier = actionIdentifier,
            extractor = EnrolLastBiometricsRequestExtractor(ensureExtrasHaveSessionId(extras)),
            project = project,
        )

        ActionConstants.ACTION_CONFIRM_IDENTITY -> confirmIdentifyBuilder(
            actionIdentifier = actionIdentifier,
            extractor = ConfirmIdentityRequestExtractor(ensureExtrasHaveSessionId(extras)),
            project = project,
        )

        else -> throw InvalidRequestException(
            "Invalid CommCare action",
            ClientApiError.INVALID_STATE_FOR_INTENT_ACTION,
        )
    }.build()

    // CommCare is not able to provide session ID so we assume that the last available session ID is correct
    private suspend fun ensureExtrasHaveSessionId(map: Map<String, Any>): Map<String, Any> =
        if (map[Constants.SIMPRINTS_SESSION_ID].let { it as? String }.isNullOrBlank()) {
            map.toMutableMap().also { it.put(Constants.SIMPRINTS_SESSION_ID, getCurrentSessionId()) }
        } else {
            map
        }

    private suspend fun mapLibSimprintsAction(
        actionIdentifier: ActionRequestIdentifier,
        extras: Map<String, Any>,
        project: Project?,
    ) = when (actionIdentifier.actionName) {
        ActionConstants.ACTION_ENROL -> enrolBuilder(
            actionIdentifier = actionIdentifier,
            extractor = EnrolRequestExtractor(extras),
            project = project,
        )

        ActionConstants.ACTION_VERIFY -> verifyBuilder(
            actionIdentifier = actionIdentifier,
            extractor = VerifyRequestExtractor(extras),
            project = project,
        )

        ActionConstants.ACTION_IDENTIFY -> identifyBuilder(
            actionIdentifier = actionIdentifier,
            extractor = IdentifyRequestExtractor(extras),
            project = project,
        )

        ActionConstants.ACTION_ENROL_LAST_BIOMETRICS -> enrolLastBiometricsBuilder(
            actionIdentifier = actionIdentifier,
            extractor = EnrolLastBiometricsRequestExtractor(extras),
            project = project,
        )

        ActionConstants.ACTION_CONFIRM_IDENTITY -> confirmIdentifyBuilder(
            actionIdentifier = actionIdentifier,
            extractor = ConfirmIdentityRequestExtractor(extras),
            project = project,
        )

        else -> throw InvalidRequestException(
            "Invalid LibSimprints action",
            ClientApiError.INVALID_STATE_FOR_INTENT_ACTION,
        )
    }.build()

    private fun enrolBuilder(
        actionIdentifier: ActionRequestIdentifier,
        extractor: EnrolRequestExtractor,
        project: Project?,
    ) = EnrolRequestBuilder(
        actionIdentifier = actionIdentifier,
        extractor = extractor,
        project = project,
        tokenizationProcessor = tokenizationProcessor,
        validator = EnrolValidator(extractor),
    )

    private fun verifyBuilder(
        actionIdentifier: ActionRequestIdentifier,
        extractor: VerifyRequestExtractor,
        project: Project?,
    ) = VerifyRequestBuilder(
        actionIdentifier = actionIdentifier,
        extractor = extractor,
        project = project,
        tokenizationProcessor = tokenizationProcessor,
        validator = VerifyValidator(extractor),
    )

    private fun identifyBuilder(
        actionIdentifier: ActionRequestIdentifier,
        extractor: IdentifyRequestExtractor,
        project: Project?,
    ) = IdentifyRequestBuilder(
        actionIdentifier = actionIdentifier,
        extractor = extractor,
        project = project,
        tokenizationProcessor = tokenizationProcessor,
        validator = IdentifyValidator(extractor),
    )

    private suspend fun enrolLastBiometricsBuilder(
        actionIdentifier: ActionRequestIdentifier,
        extractor: EnrolLastBiometricsRequestExtractor,
        project: Project?,
    ) = EnrolLastBiometricsRequestBuilder(
        actionIdentifier = actionIdentifier,
        extractor = extractor,
        project = project,
        tokenizationProcessor = tokenizationProcessor,
        validator = EnrolLastBiometricsValidator(
            extractor = extractor,
            currentSessionId = getCurrentSessionId(),
            sessionHasIdentificationCallback = sessionHasIdentificationCallback(extractor.getSessionId()),
        ),
    )

    private suspend fun confirmIdentifyBuilder(
        actionIdentifier: ActionRequestIdentifier,
        extractor: ConfirmIdentityRequestExtractor,
        project: Project?,
    ) = ConfirmIdentifyRequestBuilder(
        actionIdentifier = actionIdentifier,
        extractor = extractor,
        project = project,
        tokenizationProcessor = tokenizationProcessor,
        validator = ConfirmIdentityValidator(
            extractor = extractor,
            currentSessionId = getCurrentSessionId(),
            sessionHasIdentificationCallback = sessionHasIdentificationCallback(extractor.getSessionId()),
        ),
    )
}
