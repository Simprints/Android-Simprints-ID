package com.simprints.id.session.sessionParameters.readers.unexpectedParameters

import com.simprints.id.session.callout.Callout
import com.simprints.id.session.callout.CalloutAction
import com.simprints.libsimprints.Constants

class ExpectedParametersListerImpl: ExpectedParametersLister {

    private val commonParameterKeys = arrayOf(
        Constants.SIMPRINTS_API_KEY,
        Constants.SIMPRINTS_PROJECT_ID,
        Constants.SIMPRINTS_MODULE_ID,
        Constants.SIMPRINTS_USER_ID,
        Constants.SIMPRINTS_CALLING_PACKAGE,
        Constants.SIMPRINTS_METADATA,
        Constants.SIMPRINTS_RESULT_FORMAT
    )

    private val registrationParameterKeys = setOf(*commonParameterKeys)
    private val identificationParameterKeys = setOf(*commonParameterKeys)
    private val updateParameterKeys = setOf(*commonParameterKeys, Constants.SIMPRINTS_UPDATE_GUID)
    private val verificationParameterKeys = setOf(*commonParameterKeys, Constants.SIMPRINTS_VERIFY_GUID)

    override fun listKeysOfExpectedParametersIn(callout: Callout): Set<String> =
        when (callout.action) {
            CalloutAction.REGISTER -> registrationParameterKeys
            CalloutAction.IDENTIFY -> identificationParameterKeys
            CalloutAction.UPDATE -> updateParameterKeys
            CalloutAction.VERIFY -> verificationParameterKeys
            else -> emptySet()
        }
}
