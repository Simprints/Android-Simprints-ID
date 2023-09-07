package com.simprints.feature.clientapi.mappers.request

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
import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.models.ActionRequestIdentifier
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.feature.clientapi.models.CommCareConstants
import com.simprints.feature.clientapi.models.IntegrationConstants
import com.simprints.feature.clientapi.models.LibSimprintsConstants
import com.simprints.feature.clientapi.models.OdkConstants
import com.simprints.feature.clientapi.session.ClientSessionManager
import javax.inject.Inject

internal class IntentToActionMapper @Inject constructor(
    private val sessionManager: ClientSessionManager,
) {

    suspend operator fun invoke(action: String, extras: Map<String, Any>): ActionRequest {
        val actionIdentifier = ActionRequestIdentifier.fromIntentAction(action)

        return when (actionIdentifier.packageName) {
            OdkConstants.PACKAGE_NAME -> mapOdkAction(actionIdentifier, extras)
            CommCareConstants.PACKAGE_NAME -> mapCommCareAction(actionIdentifier, extras)
            LibSimprintsConstants.PACKAGE_NAME -> mapLibSimprintsAction(actionIdentifier, extras)
            else -> throw InvalidRequestException("Unsupported package name", ClientApiError.INVALID_STATE_FOR_INTENT_ACTION)
        }
    }

    private suspend fun mapOdkAction(actionIdentifier: ActionRequestIdentifier, extras: Map<String, Any>) = when (actionIdentifier.actionName) {
        IntegrationConstants.ACTION_ENROL -> enrolBuilder(actionIdentifier, OdkEnrolRequestExtractor(extras, OdkConstants.acceptableExtras))
        IntegrationConstants.ACTION_VERIFY -> verifyBuilder(actionIdentifier, OdkVerifyRequestExtractor(extras, OdkConstants.acceptableExtras))
        IntegrationConstants.ACTION_IDENTIFY -> identifyBuilder(actionIdentifier, OdkIdentifyRequestExtractor(extras, OdkConstants.acceptableExtras))
        IntegrationConstants.ACTION_ENROL_LAST_BIOMETRICS -> enrolLastBiometricsBuilder(actionIdentifier, EnrolLastBiometricsRequestExtractor(extras))
        IntegrationConstants.ACTION_CONFIRM_IDENTITY -> confirmIdentifyBuilder(actionIdentifier, ConfirmIdentityRequestExtractor(extras))
        else -> throw InvalidRequestException("Invalid ODK action", ClientApiError.INVALID_STATE_FOR_INTENT_ACTION)
    }.build()

    private suspend fun mapCommCareAction(actionIdentifier: ActionRequestIdentifier, extras: Map<String, Any>) = when (actionIdentifier.actionName) {
        IntegrationConstants.ACTION_ENROL -> enrolBuilder(actionIdentifier, EnrolRequestExtractor(extras))
        IntegrationConstants.ACTION_VERIFY -> verifyBuilder(actionIdentifier, VerifyRequestExtractor(extras))
        IntegrationConstants.ACTION_IDENTIFY -> identifyBuilder(actionIdentifier, IdentifyRequestExtractor(extras))
        IntegrationConstants.ACTION_ENROL_LAST_BIOMETRICS -> enrolLastBiometricsBuilder(actionIdentifier, EnrolLastBiometricsRequestExtractor(extras))
        IntegrationConstants.ACTION_CONFIRM_IDENTITY -> confirmIdentifyBuilder(actionIdentifier, ConfirmIdentityRequestExtractor(extras))
        else -> throw InvalidRequestException("Invalid CommCare action", ClientApiError.INVALID_STATE_FOR_INTENT_ACTION)
    }.build()

    private suspend fun mapLibSimprintsAction(actionIdentifier: ActionRequestIdentifier, extras: Map<String, Any>) = when (actionIdentifier.actionName) {
        IntegrationConstants.ACTION_ENROL -> enrolBuilder(actionIdentifier, EnrolRequestExtractor(extras))
        IntegrationConstants.ACTION_VERIFY -> verifyBuilder(actionIdentifier, VerifyRequestExtractor(extras))
        IntegrationConstants.ACTION_IDENTIFY -> identifyBuilder(actionIdentifier, IdentifyRequestExtractor(extras))
        IntegrationConstants.ACTION_ENROL_LAST_BIOMETRICS -> enrolLastBiometricsBuilder(actionIdentifier, EnrolLastBiometricsRequestExtractor(extras))
        IntegrationConstants.ACTION_CONFIRM_IDENTITY -> confirmIdentifyBuilder(actionIdentifier, ConfirmIdentityRequestExtractor(extras))
        else -> throw InvalidRequestException("Invalid LibSimprints action", ClientApiError.INVALID_STATE_FOR_INTENT_ACTION)
    }.build()

    private fun enrolBuilder(actionIdentifier: ActionRequestIdentifier, extractor: EnrolRequestExtractor) = EnrolRequestBuilder(actionIdentifier, extractor, EnrolValidator(extractor))

    private fun verifyBuilder(actionIdentifier: ActionRequestIdentifier, extractor: VerifyRequestExtractor) = VerifyRequestBuilder(actionIdentifier, extractor, VerifyValidator(extractor))

    private fun identifyBuilder(actionIdentifier: ActionRequestIdentifier, extractor: IdentifyRequestExtractor) = IdentifyRequestBuilder(actionIdentifier, extractor, IdentifyValidator(extractor))

    private suspend fun enrolLastBiometricsBuilder(actionIdentifier: ActionRequestIdentifier, extractor: EnrolLastBiometricsRequestExtractor) = EnrolLastBiometricsRequestBuilder(
        actionIdentifier,
        extractor,
        EnrolLastBiometricsValidator(extractor, sessionManager.getCurrentSessionId(), sessionManager.isCurrentSessionAnIdentificationOrEnrolment())
    )

    private suspend fun confirmIdentifyBuilder(actionIdentifier: ActionRequestIdentifier, extractor: ConfirmIdentityRequestExtractor) = ConfirmIdentifyRequestBuilder(
        actionIdentifier,
        extractor,
        ConfirmIdentityValidator(extractor, sessionManager.getCurrentSessionId(), sessionManager.sessionHasIdentificationCallback(extractor.getSessionId()))
    )

}
