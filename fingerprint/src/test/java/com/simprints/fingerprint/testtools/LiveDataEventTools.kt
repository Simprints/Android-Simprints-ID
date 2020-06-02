package com.simprints.fingerprint.testtools

import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.testtools.common.livedata.TestObserver

fun <T> TestObserver<LiveDataEventWithContent<T>>.assertEventReceivedWithContent(expected: T) {
    assertThat(this.observedValues.count()).isEqualTo(1)
    assertThat(this.observedValues.last()?.peekContent()).isEqualTo(expected)
}

fun TestObserver<LiveDataEvent>.assertEventReceived() {
    assertThat(this.observedValues.count()).isEqualTo(1)
}

fun MutableLiveData<LiveDataEvent>.assertEventNeverReceived() {
    assertThat(this.value).isNull()
}

fun <T> MutableLiveData<LiveDataEventWithContent<T>>.assertEventWithContentNeverReceived() {
    assertThat(this.value).isNull()
}

fun MutableLiveData<LiveDataEvent>.assertEventReceived() {
    assertThat(this.value).isNotNull()
}

fun <T> MutableLiveData<LiveDataEventWithContent<T>>.assertEventReceivedWithContent(expected: T) {
    assertThat(this.value?.peekContent()).isEqualTo(expected)
}

fun <T> MutableLiveData<LiveDataEventWithContent<T>>.assertEventReceivedWithContentAssertions(assertions: (T) -> Unit) {
    with(this.value!!.peekContent()) {
        assertions(this)
    }
}
