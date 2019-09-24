package com.simprints.scarecrow

import androidx.lifecycle.Observer


/**
 * An [Observer] for [LiveDataEvent1]s, simplifying the pattern of checking if the [LiveDataEvent1]'s content has
 * already been handled.
 *
 * [onEventUnhandledContent] is *only* called if the [LiveDataEvent1]'s contents has not been handled.
 */
class LiveDataEvent1Observer<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<LiveDataEvent1<T>> {
    override fun onChanged(event: LiveDataEvent1<T>?) {
        event?.getContentIfNotHandled()?.let { value ->
            onEventUnhandledContent(value)
        }
    }
}
