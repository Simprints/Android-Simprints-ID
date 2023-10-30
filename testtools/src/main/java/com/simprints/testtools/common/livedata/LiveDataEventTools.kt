package com.simprints.testtools.common.livedata

import androidx.lifecycle.LiveData
import com.google.common.truth.Truth.assertThat
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent

fun <T> TestObserver<LiveDataEventWithContent<T>>.assertEventReceivedWithContent(expected: T) {
    assertThat(this.observedValues.last()?.peekContent()).isEqualTo(expected)
}

fun TestObserver<LiveDataEvent>.assertEventReceived() {
    assertThat(this.observedValues.count()).isEqualTo(1)
}

fun <T> LiveData<LiveDataEventWithContent<T>>.assertEventWithContentNeverReceived() {
    assertThat(this.value).isNull()
}

fun LiveData<LiveDataEvent>.assertEventReceived() {
    assertThat(this.value).isNotNull()
}

fun LiveData<LiveDataEvent>.assertEventNotReceived() {
    assertThat(this.value).isNull()
}

fun <T> LiveData<LiveDataEventWithContent<T>>.assertEventReceivedWithContent(expected: T) {
    assertThat(this.value?.peekContent()).isEqualTo(expected)
}

fun <T> LiveData<LiveDataEventWithContent<T>>.assertEventReceivedWithContentAssertions(assertions: (T?) -> Unit) {
    with(this.value?.peekContent()) {
        assertions(this)
    }
}
