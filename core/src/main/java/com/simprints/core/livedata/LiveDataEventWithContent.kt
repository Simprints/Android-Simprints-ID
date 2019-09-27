package com.simprints.core.livedata

import androidx.lifecycle.MutableLiveData


/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
open class LiveDataEventWithContent<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}

fun <T> MutableLiveData<LiveDataEventWithContent<T>>.send(content: T) {
    this.value = LiveDataEventWithContent(content)
}
