package com.simprints.id.domain.calloutValidation.calloutParameters

import android.content.Intent
import com.simprints.id.domain.calloutValidation.calloutParameter.concrete.*
import com.simprints.libsimprints.Constants

class MainCalloutParameters(intent: Intent?) : CalloutParametersImp() {

    companion object {

        private val expectedKeys = setOf(
                Constants.SIMPRINTS_API_KEY,
                Constants.SIMPRINTS_MODULE_ID,
                Constants.SIMPRINTS_USER_ID,
                Constants.SIMPRINTS_UPDATE_GUID,
                Constants.SIMPRINTS_VERIFY_GUID,
                Constants.SIMPRINTS_CALLING_PACKAGE,
                Constants.SIMPRINTS_METADATA,
                Constants.SIMPRINTS_RESULT_FORMAT)

    }

    private val intentToParse = intent ?: Intent()
    val typeParameter = TypeParameter(intentToParse)
    val apiKeyParameter = ApiKeyParameter(intentToParse)
    val moduleIdParameter = ModuleIdParameter(intentToParse)
    val userIdParameter = UserIdParameter(intentToParse)
    val patientIdParameter = PatientIdParameter(typeParameter,
        UpdateIdParameter(intentToParse, typeParameter),
        VerifyIdParameter(intentToParse, typeParameter))
    val callingPackageParameter = CallingPackageParameter(intentToParse)
    val metadataParameter = MetadataParameter(intentToParse)
    val resultFormatParameter = ResultFormatParameter(intentToParse)
    val unexpectedParameters = UnexpectedExtrasParameter(intentToParse, expectedKeys)

    override val parameters = listOf(
            typeParameter,
            apiKeyParameter,
            moduleIdParameter,
            userIdParameter,
            patientIdParameter,
            callingPackageParameter,
            metadataParameter,
            resultFormatParameter,
            unexpectedParameters
    )

}
