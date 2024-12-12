package com.simprints.core.livedata

import com.google.common.truth.Truth
import org.junit.Test

class LiveDataEventWithContentTest {
    @Test
    fun liveDataShouldGetIfNotHandled() {
        val x = LiveDataEventWithContent("test")
        Truth.assertThat(x.hasBeenHandled).isFalse()
        Truth.assertThat(x.getContentIfNotHandled()).isEqualTo("test")
    }

    @Test
    fun liveDataShouldNotGetIfAlreadyHandled() {
        val x = LiveDataEventWithContent("test")
        x.getContentIfNotHandled()
        Truth.assertThat(x.hasBeenHandled).isTrue()
        Truth.assertThat(x.getContentIfNotHandled()).isNull()
    }

    @Test
    fun liveDataShouldConsistentlyNotGetIfAlreadyHandled() {
        val x = LiveDataEventWithContent("test")
        x.getContentIfNotHandled()

        Truth.assertThat(x.hasBeenHandled).isTrue()
        Truth.assertThat(x.getContentIfNotHandled()).isNull()
        Truth.assertThat(x.hasBeenHandled).isTrue()
        Truth.assertThat(x.getContentIfNotHandled()).isNull()
        Truth.assertThat(x.hasBeenHandled).isTrue()
        Truth.assertThat(x.getContentIfNotHandled()).isNull()
    }
}
