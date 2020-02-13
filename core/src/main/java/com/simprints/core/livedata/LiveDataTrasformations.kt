package com.simprints.core.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <A, B> LiveData<A>.combine(other: LiveData<B>): PairLiveData<A, B> {
    return PairLiveData(this, other)
}

class PairLiveData<A, B>(first: LiveData<A>, second: LiveData<B>) : MediatorLiveData<Pair<A?, B?>>() {
    init {
        addSource(first) { value = it to second.value }
        addSource(second) { value = first.value to it }
    }
}
