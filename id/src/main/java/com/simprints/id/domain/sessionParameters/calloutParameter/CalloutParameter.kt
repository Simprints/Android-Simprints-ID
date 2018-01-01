package com.simprints.id.domain.sessionParameters.calloutParameter


/**
 * Abstraction of any parameter of a action that can be validated, then accessed.
 */
interface CalloutParameter<out T: Any> {

    val value: T

    fun validate()

}
