package com.simprints.id.data.model.calloutParameter


/**
 * Abstraction of any parameter of a callout that can be validated, then accessed.
 */
interface CalloutParameter<out T: Any> {

    val value: T

    fun validate()

}
