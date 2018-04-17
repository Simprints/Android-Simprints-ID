package com.simprints.id.session.sessionParameters.extractors

import com.simprints.id.session.callout.Callout

interface Extractor<out T: Any> {

    fun extractFrom(callout: Callout): T

}
