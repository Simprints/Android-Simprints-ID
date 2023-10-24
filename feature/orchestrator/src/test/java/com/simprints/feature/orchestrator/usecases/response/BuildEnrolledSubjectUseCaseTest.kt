package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class BuildEnrolledSubjectUseCaseTest {

    @MockK
    lateinit var timeHelper: TimeHelper

    private lateinit var useCase: BuildEnrolledSubjectUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every { timeHelper.now() } returns 0L

        useCase = BuildEnrolledSubjectUseCase(timeHelper)
    }

    @Test
    fun `Throws when no responses provided`() {
        assertThrows<IllegalStateException> {
            useCase("", "", "", null)
        }
    }

    @Test
    fun `Builds subject from face response`() {
        val result = useCase(
            "", "", "",
            FaceCaptureResult(listOf(createFaceCaptureItem()))
        )

        assertThat(result.faceSamples).isNotEmpty()
        assertThat(result.fingerprintSamples).isEmpty()
    }

    private fun createFaceCaptureItem() =
        FaceCaptureResult.Item(0, FaceCaptureResult.Sample("", byteArrayOf(), null, ""))
}
