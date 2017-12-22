package com.simprints.id.domain.calloutValidation.calloutParameter


/**
 * Abstraction of any parameter of a callout that can be validated, then accessed.
 */
interface CalloutParameter<out T: Any> {

    val value: T

    fun validate()

}
