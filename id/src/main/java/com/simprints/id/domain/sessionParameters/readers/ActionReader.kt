package com.simprints.id.domain.sessionParameters.readers

import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.callout.CalloutAction


class ActionReader : Reader<CalloutAction> {

    override fun readFrom(callout: Callout): CalloutAction =
        callout.action

}
