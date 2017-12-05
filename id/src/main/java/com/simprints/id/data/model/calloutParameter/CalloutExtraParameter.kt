package com.simprints.id.data.model.calloutParameter

import android.content.Intent

/**
 * Abstraction of any callout parameter that is parsed from the extras of the intent.
 */
@Suppress("UNCHECKED_CAST", "USELESS_IS_CHECK")
abstract class CalloutExtraParameter<out T: Any>(intent: Intent,
                                                 key: String,
                                                 defaultValue: T)
    : CalloutParameter<T> {

    private val parsedValue: Any? = intent.extras?.get(key)

    val isMissing: Boolean = (parsedValue == null)

    val isMismatchedType: Boolean = (parsedValue?.javaClass != defaultValue.javaClass)

    override val value: T =
            if (isMissing || isMismatchedType) {
                defaultValue
            } else {
                parsedValue as T
            }

}