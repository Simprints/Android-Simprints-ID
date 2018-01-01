package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.libsimprints.Constants


class CallingPackageParameter(calloutParameters: CalloutParameters, defaultValue: String = "")
    : CalloutExtraParameter<String>(calloutParameters, Constants.SIMPRINTS_CALLING_PACKAGE, defaultValue) {

    override fun validate() {

    }

}
