package com.simprints.face.capture.screens.livefeedback

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LightingHintDebouncerTest {
    private val debouncer = LightingHintDebouncer(debounceMs = 500)

    @Test
    fun `shows only after the condition holds for the debounce time`() {
        assertThat(debouncer.update(condition = true, nowMs = 0)).isFalse()
        assertThat(debouncer.update(condition = true, nowMs = 499)).isFalse()
        assertThat(debouncer.update(condition = true, nowMs = 500)).isTrue()
    }

    @Test
    fun `a brief interruption restarts the debounce`() {
        debouncer.update(condition = true, nowMs = 0)
        debouncer.update(condition = false, nowMs = 300)

        assertThat(debouncer.update(condition = true, nowMs = 600)).isFalse()
        assertThat(debouncer.update(condition = true, nowMs = 1100)).isTrue()
    }

    @Test
    fun `hides only after the condition stops holding for the debounce time`() {
        debouncer.update(condition = true, nowMs = 0)
        debouncer.update(condition = true, nowMs = 500)

        assertThat(debouncer.update(condition = false, nowMs = 600)).isTrue()
        assertThat(debouncer.update(condition = true, nowMs = 700)).isTrue() // flicker ignored
        assertThat(debouncer.update(condition = false, nowMs = 800)).isTrue()
        assertThat(debouncer.update(condition = false, nowMs = 1300)).isFalse()
    }

    @Test
    fun `zero debounce follows the condition`() {
        val immediate = LightingHintDebouncer(debounceMs = 0)

        assertThat(immediate.update(condition = true, nowMs = 0)).isTrue()
        assertThat(immediate.update(condition = false, nowMs = 1)).isFalse()
    }

    @Test
    fun `reset hides the hint`() {
        debouncer.update(condition = true, nowMs = 0)
        debouncer.update(condition = true, nowMs = 500)

        debouncer.reset()

        assertThat(debouncer.isShown).isFalse()
        assertThat(debouncer.update(condition = true, nowMs = 600)).isFalse()
    }
}
