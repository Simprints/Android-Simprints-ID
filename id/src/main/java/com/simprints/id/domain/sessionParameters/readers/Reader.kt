package com.simprints.id.domain.sessionParameters.readers

import com.simprints.id.domain.callout.Callout


interface Reader<out T: Any> {

    fun readFrom(callout: Callout): T

}
