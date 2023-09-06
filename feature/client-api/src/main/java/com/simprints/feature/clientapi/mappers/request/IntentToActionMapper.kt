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
        val packageName = action.substringBeforeLast(".")
        val actionName = action.substringAfterLast(".")

        return when (packageName) {
            OdkConstants.PACKAGE_NAME -> mapOdkAction(packageName, actionName, extras)
            CommCareConstants.PACKAGE_NAME -> mapCommCareAction(packageName, actionName, extras)
            LibSimprintsConstants.PACKAGE_NAME -> mapLibSimprintsAction(packageName, actionName, extras)
            else -> throw InvalidRequestException("Unsupported package name", ClientApiError.INVALID_STATE_FOR_INTENT_ACTION)
        }
    }

    private suspend fun mapOdkAction(packageName: String, actionName: String, extras: Map<String, Any>) = when (actionName) {
        IntegrationConstants.ACTION_ENROL -> enrolBuilder(packageName, OdkEnrolRequestExtractor(extras, OdkConstants.acceptableExtras))
        IntegrationConstants.ACTION_VERIFY -> verifyBuilder(packageName, OdkVerifyRequestExtractor(extras, OdkConstants.acceptableExtras))
        IntegrationConstants.ACTION_IDENTIFY -> identifyBuilder(packageName, OdkIdentifyRequestExtractor(extras, OdkConstants.acceptableExtras))
        IntegrationConstants.ACTION_ENROL_LAST_BIOMETRICS -> enrolLastBiometricsBuilder(packageName, EnrolLastBiometricsRequestExtractor(extras))
        IntegrationConstants.ACTION_CONFIRM_IDENTITY -> confirmIdentifyBuilder(packageName, ConfirmIdentityRequestExtractor(extras))
        else -> throw InvalidRequestException("Invalid ODK action", ClientApiError.INVALID_STATE_FOR_INTENT_ACTION)
    }.build()

    private suspend fun mapCommCareAction(packageName: String, actionName: String, extras: Map<String, Any>) = when (actionName) {
        IntegrationConstants.ACTION_ENROL -> enrolBuilder(packageName, EnrolRequestExtractor(extras))
        IntegrationConstants.ACTION_VERIFY -> verifyBuilder(packageName, VerifyRequestExtractor(extras))
        IntegrationConstants.ACTION_IDENTIFY -> identifyBuilder(packageName, IdentifyRequestExtractor(extras))
        IntegrationConstants.ACTION_ENROL_LAST_BIOMETRICS -> enrolLastBiometricsBuilder(packageName, EnrolLastBiometricsRequestExtractor(extras))
        IntegrationConstants.ACTION_CONFIRM_IDENTITY -> confirmIdentifyBuilder(packageName, ConfirmIdentityRequestExtractor(extras))
        else -> throw InvalidRequestException("Invalid CommCare action", ClientApiError.INVALID_STATE_FOR_INTENT_ACTION)
    }.build()

    private suspend fun mapLibSimprintsAction(packageName: String, actionName: String, extras: Map<String, Any>) = when (actionName) {
        IntegrationConstants.ACTION_ENROL -> enrolBuilder(packageName, EnrolRequestExtractor(extras))
        IntegrationConstants.ACTION_VERIFY -> verifyBuilder(packageName, VerifyRequestExtractor(extras))
        IntegrationConstants.ACTION_IDENTIFY -> identifyBuilder(packageName, IdentifyRequestExtractor(extras))
        IntegrationConstants.ACTION_ENROL_LAST_BIOMETRICS -> enrolLastBiometricsBuilder(packageName, EnrolLastBiometricsRequestExtractor(extras))
        IntegrationConstants.ACTION_CONFIRM_IDENTITY -> confirmIdentifyBuilder(packageName, ConfirmIdentityRequestExtractor(extras))
        else -> throw InvalidRequestException("Invalid LibSimprints action", ClientApiError.INVALID_STATE_FOR_INTENT_ACTION)
    }.build()

    private fun enrolBuilder(packageName: String, extractor: EnrolRequestExtractor) = EnrolRequestBuilder(packageName, extractor, EnrolValidator(extractor))

    private fun verifyBuilder(packageName: String, extractor: VerifyRequestExtractor) = VerifyRequestBuilder(packageName, extractor, VerifyValidator(extractor))

    private fun identifyBuilder(packageName: String, extractor: IdentifyRequestExtractor) = IdentifyRequestBuilder(packageName, extractor, IdentifyValidator(extractor))

    private suspend fun enrolLastBiometricsBuilder(packageName: String, extractor: EnrolLastBiometricsRequestExtractor) = EnrolLastBiometricsRequestBuilder(
        packageName,
        extractor,
        EnrolLastBiometricsValidator(extractor, sessionManager.getCurrentSessionId(), sessionManager.isCurrentSessionAnIdentificationOrEnrolment())
    )

    private suspend fun confirmIdentifyBuilder(packageName: String, extractor: ConfirmIdentityRequestExtractor) = ConfirmIdentifyRequestBuilder(
        packageName,
        extractor,
        ConfirmIdentityValidator(extractor, sessionManager.getCurrentSessionId(), sessionManager.sessionHasIdentificationCallback(extractor.getSessionId()))
    )

}
