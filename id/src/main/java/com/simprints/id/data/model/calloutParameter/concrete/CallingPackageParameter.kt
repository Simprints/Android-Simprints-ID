package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.calloutParameter.CalloutExtraParameter
import com.simprints.libsimprints.Constants

class CallingPackageParameter(intent: Intent)
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_CALLING_PACKAGE, "") {

    override fun validate() {

    }

}