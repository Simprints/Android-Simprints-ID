package com.simprints.id.session.sessionParameters.readers

import com.simprints.id.session.callout.Callout
import com.simprints.id.session.callout.CalloutAction


class ActionReader : Reader<CalloutAction> {

    override fun readFrom(callout: Callout): CalloutAction =
        callout.action

}
