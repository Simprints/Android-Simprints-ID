package com.simprints.fingerprint.tools.livedata

import androidx.lifecycle.MutableLiveData
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent

fun MutableLiveData<LiveDataEvent>.postEvent() =
    postValue(LiveDataEvent())

fun <T> MutableLiveData<LiveDataEventWithContent<T>>.postEvent(content: T) =
    postValue(LiveDataEventWithContent(content))
