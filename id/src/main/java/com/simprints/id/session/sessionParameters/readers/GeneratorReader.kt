package com.simprints.id.session.sessionParameters.readers

import com.simprints.id.session.callout.Callout

class GeneratorReader<out T: Any>(private val generateValue: () -> T)
    : Reader<T> {

    override fun readFrom(callout: Callout): T =
        generateValue()

}
