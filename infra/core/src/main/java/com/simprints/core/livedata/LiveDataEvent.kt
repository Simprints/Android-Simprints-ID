package com.simprints.core.livedata

import androidx.lifecycle.MutableLiveData


open class LiveDataEvent {

    var hasBeenHandled = false
        private set // Allow external read but not write

    fun getIfNotHandled(): LiveDataEvent? {
        return if (hasBeenHandled)
            null
        else {
            hasBeenHandled = true
            this
        }
    }

}

fun MutableLiveData<LiveDataEvent>.send() {
    this.value = LiveDataEvent()
}
