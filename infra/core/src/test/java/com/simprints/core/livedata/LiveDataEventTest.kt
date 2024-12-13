package com.simprints.core.livedata

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LiveDataEventTest {
    @Test
    fun liveDataShouldGetIfNotHandled() {
        val x = LiveDataEvent()
        assertThat(x.hasBeenHandled).isFalse()
        assertThat(x == x.getIfNotHandled()).isTrue()
    }

    @Test
    fun liveDataShouldNotGetIfAlreadyHandled() {
        val x = LiveDataEvent()
        x.getIfNotHandled()
        assertThat(x.hasBeenHandled).isTrue()
        assertThat(null == x.getIfNotHandled()).isTrue()
    }

    @Test
    fun liveDataShouldConsistentlyNotGetIfAlreadyHandled() {
        val x = LiveDataEvent()
        x.getIfNotHandled()

        assertThat(x.hasBeenHandled).isTrue()
        assertThat(null == x.getIfNotHandled()).isTrue()
        assertThat(x.hasBeenHandled).isTrue()
        assertThat(null == x.getIfNotHandled()).isTrue()
        assertThat(x.hasBeenHandled).isTrue()
        assertThat(null == x.getIfNotHandled()).isTrue()
    }
}
