package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.infra.eventsync.sync.down.tasks.SubjectFactory
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class BuildEnrolledSubjectUseCaseTest {

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var subjectFactory: SubjectFactory

    private lateinit var useCase: BuildEnrolledSubjectUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every { timeHelper.now() } returns 0L
        every { subjectFactory.buildSubjectFromFace(
            projectId = any(),
            userId = any(),
            moduleId = any(),
            faceResponse = any(),
            timeHelper = any()
        ) } returns mockk()
        useCase =
            BuildEnrolledSubjectUseCase(timeHelper = timeHelper, subjectFactory = subjectFactory)
    }

    @Test
    fun `Throws when no responses provided`() {
        assertThrows<IllegalStateException> {
            useCase(
                projectId = "",
                userId = "".asTokenizableRaw(),
                moduleId = "".asTokenizableRaw(),
                faceResponse = null
            )
        }
    }

    @Test
    fun `Builds subject from face response`() {
        val projectId = ""
        val userId = "".asTokenizableRaw()
        val moduleId = "".asTokenizableRaw()
        val faceResponse = FaceCaptureResult(listOf(createFaceCaptureItem()))
        useCase(
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            faceResponse = faceResponse
        )

        verify { subjectFactory.buildSubjectFromFace(
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            faceResponse = faceResponse,
            timeHelper = timeHelper
        ) }
    }

    private fun createFaceCaptureItem() =
        FaceCaptureResult.Item(0, FaceCaptureResult.Sample("", byteArrayOf(), null, ""))
}
