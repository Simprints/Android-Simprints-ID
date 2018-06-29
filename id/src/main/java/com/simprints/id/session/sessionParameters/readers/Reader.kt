package com.simprints.id.session.sessionParameters.readers

import com.simprints.id.session.callout.Callout


interface Reader<out T: Any> {

    fun readFrom(callout: Callout): T

}
