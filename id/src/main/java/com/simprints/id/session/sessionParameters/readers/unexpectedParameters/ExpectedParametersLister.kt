package com.simprints.id.session.sessionParameters.readers.unexpectedParameters

import com.simprints.id.session.callout.Callout

interface ExpectedParametersLister {

    fun listKeysOfExpectedParametersIn(callout: Callout): Set<String>

}
