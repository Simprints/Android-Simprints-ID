package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.calloutParameter.CalloutExtraParameter
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.libsimprints.Constants

class UserIdParameter(intent: Intent)
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_USER_ID, "") {

    override fun validate() {
        if (isMissing) {
            throw InvalidCalloutError(ALERT_TYPE.MISSING_USER_ID)
        }
    }
}