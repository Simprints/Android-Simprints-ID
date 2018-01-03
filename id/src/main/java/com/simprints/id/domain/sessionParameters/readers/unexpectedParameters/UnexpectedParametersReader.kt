package com.simprints.id.domain.sessionParameters.readers.unexpectedParameters

import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.callout.CalloutParameter
import com.simprints.id.domain.sessionParameters.readers.Reader


class UnexpectedParametersReader(private val expectedParametersLister: ExpectedParametersLister)
    : Reader<Set<CalloutParameter>> {

    override fun readFrom(callout: Callout): Set<CalloutParameter> =
        callout.parameters
            .filterNot { it.key in expectedParametersLister.listKeysOfExpectedParametersIn(callout) }
            .toSet()

}
