package com.simprints.id.domain.sessionParameters.extractors

import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.callout.CalloutAction

class ActionDependentExtractor<out T: Any> (private val switch: Map<CalloutAction, Extractor<T>>,
                                            private val defaultValue: T)
    : Extractor<T> {

    override fun extractFrom(callout: Callout): T =
        switch[callout.action]?.extractFrom(callout) ?: defaultValue

}
