package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.calloutParameter.CalloutExtraParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants

class ApiKeyParameter(intent: Intent)
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_API_KEY, "") {

    override fun validate() {
        val apiKey = value
        if (isMissing) {
            throw InvalidCalloutError(ALERT_TYPE.MISSING_API_KEY)
        }
        if (apiKey.length < 8) {
            throw InvalidCalloutError(ALERT_TYPE.INVALID_API_KEY)
        }
    }

}