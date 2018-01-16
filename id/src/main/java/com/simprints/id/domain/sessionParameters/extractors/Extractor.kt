package com.simprints.id.domain.sessionParameters.extractors

import com.simprints.id.domain.callout.Callout

interface Extractor<out T: Any> {

    fun extractFrom(callout: Callout): T

}
