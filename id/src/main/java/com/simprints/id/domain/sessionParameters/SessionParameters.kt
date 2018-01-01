package com.simprints.id.domain.sessionParameters

import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.domain.sessionParameters.extractors.ParameterExtractor
import com.simprints.id.domain.sessionParameters.readers.*
import com.simprints.id.domain.sessionParameters.validators.GuidValidator
import com.simprints.id.domain.sessionParameters.validators.MetadataValidator
import com.simprints.id.domain.sessionParameters.validators.NoOpValidator
import com.simprints.id.domain.sessionParameters.validators.ValueValidator
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants

class SessionParameters(val calloutAction: CalloutAction,
                        val apiKey: String,
                        val moduleId: String,
                        val userId: String,
                        val patientId: String,
                        val callingPackage: String,
                        val metadata: String,
                        val resultFormat: String) {

    companion object {

        fun extractSessionParametersFrom(callout: Callout): SessionParameters {

            val actionReader = ActionReader()
            val actionValidator = ValueValidator(CalloutAction.validValues, ALERT_TYPE.INVALID_INTENT_ACTION)
            val actionExtractor = ParameterExtractor(actionReader, actionValidator)
            val action = actionExtractor.extractFrom(callout)

            val apiKeyReader = MandatoryParameterReader(Constants.SIMPRINTS_API_KEY,
                String::class, ALERT_TYPE.MISSING_API_KEY, ALERT_TYPE.INVALID_API_KEY)
            val apiKeyValidator = GuidValidator(ALERT_TYPE.INVALID_API_KEY)
            val apiKeyExtractor = ParameterExtractor(apiKeyReader, apiKeyValidator)
            val apiKey = apiKeyExtractor.extractFrom(callout)

            val moduleIdReader = MandatoryParameterReader(Constants.SIMPRINTS_MODULE_ID,
                String::class, ALERT_TYPE.MISSING_MODULE_ID, ALERT_TYPE.INVALID_MODULE_ID)
            val moduleIdValidator = NoOpValidator<String>()
            val moduleIdExtractor = ParameterExtractor(moduleIdReader, moduleIdValidator)
            val moduleId = moduleIdExtractor.extractFrom(callout)

            val userIdReader = MandatoryParameterReader(Constants.SIMPRINTS_USER_ID,
                String::class, ALERT_TYPE.MISSING_USER_ID, ALERT_TYPE.INVALID_USER_ID)
            val userIdValidator = NoOpValidator<String>()
            val userIdExtractor = ParameterExtractor(userIdReader, userIdValidator)
            val userId = userIdExtractor.extractFrom(callout)

            val verifyIdReader = MandatoryParameterReader(Constants.SIMPRINTS_VERIFY_GUID,
                String::class, ALERT_TYPE.MISSING_VERIFY_GUID, ALERT_TYPE.INVALID_VERIFY_GUID)
            val updateIdReader = MandatoryParameterReader(Constants.SIMPRINTS_UPDATE_GUID,
                String::class, ALERT_TYPE.MISSING_UPDATE_GUID, ALERT_TYPE.INVALID_UPDATE_GUID)
            val patientIdReader = PatientIdReader(verifyIdReader, updateIdReader)
            val patientIdValidator = GuidValidator(ALERT_TYPE.INVALID_VERIFY_GUID)
            val patientIdExtractor = ParameterExtractor(patientIdReader, patientIdValidator)
            val patientId = patientIdExtractor.extractFrom(callout)

            val callingPackageReader = OptionalParameterReader(Constants.SIMPRINTS_CALLING_PACKAGE,
                "", ALERT_TYPE.INVALID_CALLING_PACKAGE)
            val callingPackageValidator = NoOpValidator<String>()
            val callingPackageExtractor = ParameterExtractor(callingPackageReader,
                callingPackageValidator)
            val callingPackage = callingPackageExtractor.extractFrom(callout)

            val metadataReader = OptionalParameterReader(Constants.SIMPRINTS_METADATA,
                "", ALERT_TYPE.INVALID_CALLING_PACKAGE)
            val metadataValidator = MetadataValidator(ALERT_TYPE.INVALID_CALLING_PACKAGE)
            val metadataExtractor = ParameterExtractor(metadataReader, metadataValidator)
            val metadata = metadataExtractor.extractFrom(callout)

            val validResultFormats = listOf(Constants.SIMPRINTS_ODK_RESULT_FORMAT_V01, "")
            val resultFormatReader = OptionalParameterReader(Constants.SIMPRINTS_RESULT_FORMAT,
                "", ALERT_TYPE.INVALID_RESULT_FORMAT)
            val resultFormatValidator = ValueValidator(validResultFormats, ALERT_TYPE.INVALID_RESULT_FORMAT)
            val resultFormatExtractor = ParameterExtractor(resultFormatReader, resultFormatValidator)
            val resultFormat = resultFormatExtractor.extractFrom(callout)

            val validUnexpectedParametersValues = listOf(emptyMap<String, Any?>())
            val unexpectedParametersReader = UnexpectedParametersReader()
            val unexpectedParametersValidator = ValueValidator(validUnexpectedParametersValues,
                ALERT_TYPE.UNEXPECTED_PARAMETER)
            val unexpectedParametersExtractor = ParameterExtractor(unexpectedParametersReader,
                unexpectedParametersValidator)
            unexpectedParametersExtractor.extractFrom(callout)

            return SessionParameters(action, apiKey, moduleId, userId, patientId,
                callingPackage, metadata, resultFormat)
        }

    }


}
