package com.simprints.id.domain.sessionParameters.readers

import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants


class UnexpectedParametersReader: Reader<Map<String, Any?>> {

    private val commonExpectedParameterKeys = arrayOf(
        Constants.SIMPRINTS_API_KEY,
        Constants.SIMPRINTS_MODULE_ID,
        Constants.SIMPRINTS_USER_ID,
        Constants.SIMPRINTS_CALLING_PACKAGE,
        Constants.SIMPRINTS_METADATA,
        Constants.SIMPRINTS_RESULT_FORMAT
    )

    override fun readFrom(callout: Callout): Map<String, Any?> =
        callout.parameters
            .filterNot { it.key in expectedParameterKeys(callout.action) }
            .map { it.key to it.value }
            .toMap()

    private fun expectedParameterKeys(action: CalloutAction) =
        when (action) {
            CalloutAction.REGISTER,
            CalloutAction.IDENTIFY -> listOf(*commonExpectedParameterKeys)
            CalloutAction.UPDATE -> listOf(*commonExpectedParameterKeys, Constants.SIMPRINTS_UPDATE_GUID)
            CalloutAction.VERIFY -> listOf(*commonExpectedParameterKeys, Constants.SIMPRINTS_VERIFY_GUID)
            else -> throw InvalidCalloutError(ALERT_TYPE.INVALID_INTENT_ACTION)
        }

}
