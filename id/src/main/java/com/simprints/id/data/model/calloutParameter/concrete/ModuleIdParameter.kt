package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.tools.exceptions.InvalidCalloutException
import com.simprints.id.data.model.calloutParameter.CalloutExtraParameter
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants

class ModuleIdParameter(intent: Intent)
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_MODULE_ID, "") {

    override fun validate() {
        if (isMissing) {
            throw InvalidCalloutException(ALERT_TYPE.MISSING_MODULE_ID)
        }
    }
}