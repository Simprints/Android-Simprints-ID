package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.calloutParameter.CalloutExtraParameter
import com.simprints.libsimprints.Constants

class ResultFormatParameter(intent: Intent)
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_RESULT_FORMAT, "") {

    override fun validate() {
        // TODO
    }

}