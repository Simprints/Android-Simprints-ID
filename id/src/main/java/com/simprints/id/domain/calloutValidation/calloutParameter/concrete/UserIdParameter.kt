package com.simprints.id.domain.calloutValidation.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.domain.calloutValidation.calloutParameter.CalloutExtraParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants


class UserIdParameter(intent: Intent, defaultValue: String = "")
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_USER_ID, defaultValue) {

    override fun validate() {
        validateValueIsNotMissing()
    }

    private fun validateValueIsNotMissing() {
        if (isMissing) {
            throw InvalidCalloutError(ALERT_TYPE.MISSING_USER_ID)
        }
    }

}
