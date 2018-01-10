package com.simprints.id.domain.sessionParameters.readers

import com.simprints.id.domain.callout.Callout

class GeneratorReader<out T: Any>(private val generateValue: () -> T)
    : Reader<T> {

    override fun readFrom(callout: Callout): T =
        generateValue()

}
