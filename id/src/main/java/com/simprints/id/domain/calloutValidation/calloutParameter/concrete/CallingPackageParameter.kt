package com.simprints.id.domain.calloutValidation.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.domain.calloutValidation.calloutParameter.CalloutExtraParameter
import com.simprints.libsimprints.Constants


class CallingPackageParameter(intent: Intent, defaultValue: String = "")
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_CALLING_PACKAGE, defaultValue) {

    override fun validate() {

    }

}
