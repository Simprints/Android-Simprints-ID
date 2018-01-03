package com.simprints.id.domain.sessionParameters.readers

import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.callout.CalloutAction

class ActionDependentReader<out T: Any> (private val readerSwitch: Map<CalloutAction, Reader<T>>,
                                         private val defaultValue: T)
    : Reader<T> {

    override fun readFrom(callout: Callout): T =
        readerSwitch[callout.action]?.readFrom(callout) ?: defaultValue

}
