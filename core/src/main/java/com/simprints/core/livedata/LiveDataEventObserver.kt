package com.simprints.core.livedata

import androidx.lifecycle.Observer


/**
 * An [Observer] for [Event]s, simplifying the pattern of checking if the [Event]'s content has
 * already been handled.
 *
 * [onEventUnhandledContent] is *only* called if the [Event]'s contents has not been handled.
 */
class LiveDataEventObserver(private val onEventUnhandledContent: () -> Unit) : Observer<LiveDataEvent> {
    override fun onChanged(event: LiveDataEvent?) {
        event?.getIfNotHandled()?.let {
            onEventUnhandledContent()
        }
    }
}
