package com.simprints.id.domain.sessionParameters.readers.unexpectedParameters

import com.simprints.id.domain.callout.Callout

interface ExpectedParametersLister {

    fun listKeysOfExpectedParametersIn(callout: Callout): Set<String>

}
