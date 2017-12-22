package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.calloutParameter.CalloutExtraParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants


class ModuleIdParameter(intent: Intent, defaultValue: String = "")
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_MODULE_ID, defaultValue) {

    override fun validate() {
        validateValueIsNotMissing()
    }

    private fun validateValueIsNotMissing() {
        if (isMissing) {
            throw InvalidCalloutError(ALERT_TYPE.MISSING_MODULE_ID)
        }
    }

}
