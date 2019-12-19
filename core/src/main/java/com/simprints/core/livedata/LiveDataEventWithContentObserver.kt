package com.simprints.core.livedata

import androidx.lifecycle.Observer


/**
 * An [Observer] for [LiveDataEventWithContent]s, simplifying the pattern of checking if the [LiveDataEventWithContent]'s content has
 * already been handled.
 *
 * [onEventUnhandledContent] is *only* called if the [LiveDataEventWithContent]'s contents has not been handled.
 */
class LiveDataEventWithContentObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<LiveDataEventWithContent<T>> {
    override fun onChanged(event: LiveDataEventWithContent<T>?) {
        event?.getContentIfNotHandled()?.let { value ->
            onEventUnhandledContent(value)
        }
    }
}
