package com.simprints.fingerprint.activities.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.LiveDataEventWithContentObserver

/**
 * This class represents any Fingerprint Fragment within the fingerprint module. It contains mainly
 * utility extension functions for observing livedata.
 */
abstract class FingerprintFragment : Fragment() {

    private fun <T, O : Observer<T>> LiveData<T>.fragmentObserve(observer: O) =
        observe(viewLifecycleOwner, observer)

    fun <T : Any?> LiveData<T>.fragmentObserveWith(observer: (T) -> Unit) =
        fragmentObserve(Observer(observer))

    fun LiveData<LiveDataEvent>.fragmentObserveEventWith(observer: () -> Unit) =
        fragmentObserve(LiveDataEventObserver(observer))

    fun <T> LiveData<LiveDataEventWithContent<T>>.fragmentObserveEventWith(observer: (T) -> Unit) =
        fragmentObserve(LiveDataEventWithContentObserver(observer))
}
