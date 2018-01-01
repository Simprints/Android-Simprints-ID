package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.domain.sessionParameters.calloutParameter.CalloutParameter


/**
 * Abstraction of any action parameter that is parsed from the extras of the intent.
 */
@Suppress("UNCHECKED_CAST", "USELESS_IS_CHECK")
abstract class CalloutExtraParameter<out T: Any>(calloutParameters: CalloutParameters,
                                                 val key: String,
                                                 defaultValue: T)
    : CalloutParameter<T> {

    val isMissing: Boolean = key !in calloutParameters
    val isMismatchedType: Boolean = !isMissing &&
        calloutParameters[key].value?.javaClass != defaultValue.javaClass

    override val value: T = if (isMissing || isMismatchedType) {
                defaultValue
            } else {
                calloutParameters[key].value as T
            }

    override fun validate() {
        validateValueIsCorrectType()
    }

    private fun validateValueIsCorrectType() {
        if (isMismatchedType) {
        }
    }

}
