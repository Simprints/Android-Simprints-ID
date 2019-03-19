package com.simprints.testtools.common.livedata

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry


class TestLifecycleOwner : LifecycleOwner {

    private val lifecycle = LifecycleRegistry(this)

    fun onCreate(): TestLifecycleOwner {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        return this
    }

    fun onStart(): TestLifecycleOwner {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        return this
    }

    fun onResume(): TestLifecycleOwner {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        return this
    }

    fun onPause(): TestLifecycleOwner {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        return this
    }

    fun onStop(): TestLifecycleOwner {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        return this
    }


    fun onDestroy(): TestLifecycleOwner {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        return this
    }
    override fun getLifecycle() = lifecycle
}
