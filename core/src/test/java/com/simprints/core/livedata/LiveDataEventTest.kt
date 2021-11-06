package com.simprints.core.livedata

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class LiveDataEventTest {

    @Test
    fun liveDataShouldGetIfNotHandled() {
        var x = LiveDataEvent()
        assertThat(x.hasBeenHandled).isFalse()
        assertThat(x == x.getIfNotHandled()).isTrue()
    }

    @Test
    fun liveDataShouldNotGetIfAlreadyHandled() {
        var x = LiveDataEvent()
        x.getIfNotHandled()
        assertThat(x.hasBeenHandled).isTrue()
        assertThat(null == x.getIfNotHandled()).isTrue()
    }

    @Test
    fun liveDataShouldConsistentlyNotGetIfAlreadyHandled() {
        var x = LiveDataEvent()
        x.getIfNotHandled()

        assertThat(x.hasBeenHandled).isTrue()
        assertThat(null == x.getIfNotHandled()).isTrue()
        assertThat(x.hasBeenHandled).isTrue()
        assertThat(null == x.getIfNotHandled()).isTrue()
        assertThat(x.hasBeenHandled).isTrue()
        assertThat(null == x.getIfNotHandled()).isTrue()
    }

}
