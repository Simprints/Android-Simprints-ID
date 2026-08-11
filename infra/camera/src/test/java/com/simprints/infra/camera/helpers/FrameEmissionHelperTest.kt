package com.simprints.infra.camera.helpers

import com.google.common.truth.Truth.*
import org.junit.Before
import org.junit.Test

internal class FrameEmissionHelperTest {
    private lateinit var controller: FrameEmissionHelper

    @Before
    fun setUp() {
        controller = FrameEmissionHelper()
    }

    @Test
    fun `should emit analyser frames when frame emission is enabled in standard mode`() {
        controller.configure(highResolution = false)

        assertThat(controller.shouldEmitAnalyserFrame()).isTrue()
        assertThat(controller.beginHighResolutionCapture()).isFalse()
    }

    @Test
    fun `should not emit frames or capture high resolution images when frame emission is disabled`() {
        controller.configure(highResolution = false)
        controller.setFrameEmissionEnabled(false)

        assertThat(controller.shouldEmitAnalyserFrame()).isFalse()

        controller.configure(highResolution = true)

        assertThat(controller.beginHighResolutionCapture()).isFalse()
    }

    @Test
    fun `should only allow one high resolution capture at a time`() {
        controller.configure(highResolution = true)

        assertThat(controller.beginHighResolutionCapture()).isTrue()
        assertThat(controller.beginHighResolutionCapture()).isFalse()

        controller.completeHighResolutionCapture()

        assertThat(controller.beginHighResolutionCapture()).isTrue()
    }

    @Test
    fun `should drop completed high resolution frame when emission is disabled during capture`() {
        controller.configure(highResolution = true)

        assertThat(controller.beginHighResolutionCapture()).isTrue()

        controller.setFrameEmissionEnabled(false)

        assertThat(controller.completeHighResolutionCapture()).isFalse()
        assertThat(controller.beginHighResolutionCapture()).isFalse()
    }

    @Test
    fun `should reset capture state when switching back to standard mode`() {
        controller.configure(highResolution = true)
        assertThat(controller.beginHighResolutionCapture()).isTrue()

        controller.configure(highResolution = false)

        assertThat(controller.shouldEmitAnalyserFrame()).isTrue()
        assertThat(controller.beginHighResolutionCapture()).isFalse()
    }
}
