package com.simprints.id.domain.calloutValidation.calloutParameters

import android.content.Intent
import com.simprints.id.domain.calloutValidation.calloutParameter.CalloutExtraParameter
import com.simprints.id.domain.calloutValidation.calloutParameter.CalloutParameter
import com.simprints.id.domain.calloutValidation.calloutParameter.concrete.*

class CalloutParametersFactory {

    fun newMainCalloutParameters(intent: Intent): MainCalloutParameters {
        val typeParameter = TypeParameter(intent)
        val apiKeyParameter = ApiKeyParameter(intent)
        val moduleIdParameter = ModuleIdParameter(intent)
        val userIdParameter = UserIdParameter(intent)
        val updateIdParameter = UpdateIdParameter(intent, typeParameter)
        val verifyIdParameter = VerifyIdParameter(intent, typeParameter)
        val patientIdParameter = PatientIdParameter(typeParameter, updateIdParameter, verifyIdParameter)
        val callingPackageParameter = CallingPackageParameter(intent)
        val metadataParameter = MetadataParameter(intent)
        val resultFormatParameter = ResultFormatParameter(intent)

        val expectedParameters = arrayOf(
            typeParameter,
            apiKeyParameter,
            moduleIdParameter,
            userIdParameter,
            updateIdParameter,
            verifyIdParameter,
            patientIdParameter,
            callingPackageParameter,
            metadataParameter,
            resultFormatParameter
        )

        return MainCalloutParameters(typeParameter,
            apiKeyParameter,
            moduleIdParameter,
            userIdParameter,
            updateIdParameter,
            verifyIdParameter,
            patientIdParameter,
            callingPackageParameter,
            metadataParameter,
            resultFormatParameter,
            newCalloutParameters(intent, expectedParameters))
    }

    private fun newCalloutParameters(intent: Intent,
                                     expectedParameters: Array<CalloutParameter<*>>): CalloutParameters {
        val expectedExtraKeys = getExpectedExtraKeys(expectedParameters)
        val unexpectedExtrasParameter = UnexpectedExtrasParameter(intent, expectedExtraKeys)
        return CalloutParametersImp(expectedParameters, unexpectedExtrasParameter)
    }

    private fun getExpectedExtraKeys(expectedParameters: Array<CalloutParameter<*>>) =
        expectedParameters
            .filter { it is CalloutExtraParameter }
            .map { (it as CalloutExtraParameter).key }

}
