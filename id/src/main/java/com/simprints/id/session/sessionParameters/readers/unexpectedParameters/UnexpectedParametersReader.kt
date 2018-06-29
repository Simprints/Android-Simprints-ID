package com.simprints.id.session.sessionParameters.readers.unexpectedParameters

import com.simprints.id.session.callout.Callout
import com.simprints.id.session.callout.CalloutParameter
import com.simprints.id.session.sessionParameters.readers.Reader


class UnexpectedParametersReader(private val expectedParametersLister: ExpectedParametersLister)
    : Reader<Set<CalloutParameter>> {

    override fun readFrom(callout: Callout): Set<CalloutParameter> =
        callout.parameters
            .filterNot { it.key in expectedParametersLister.listKeysOfExpectedParametersIn(callout) }
            .toSet()

}
