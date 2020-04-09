package com.simprints.fingerprint.testtools

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
