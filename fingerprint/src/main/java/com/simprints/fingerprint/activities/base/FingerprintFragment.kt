package com.simprints.fingerprint.activities.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.LiveDataEventWithContentObserver

abstract class FingerprintFragment : Fragment() {

    private fun <T, O : Observer<T>> MutableLiveData<T>.fragmentObserve(observer: O) =
        observe(viewLifecycleOwner, observer)

    fun <T : Any?> MutableLiveData<T>.fragmentObserveWith(observer: (T) -> Unit) =
        fragmentObserve(Observer(observer))

    fun MutableLiveData<LiveDataEvent>.fragmentObserveEventWith(observer: () -> Unit) =
        fragmentObserve(LiveDataEventObserver(observer))

    fun <T> MutableLiveData<LiveDataEventWithContent<T>>.fragmentObserveEventWith(observer: (T) -> Unit) =
        fragmentObserve(LiveDataEventWithContentObserver(observer))
}
