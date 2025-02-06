package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.orchestrator.exceptions.MissingCaptureException
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.eventsync.sync.down.tasks.SubjectFactory
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.responses.AppEnrolResponse
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import io.mockk.MockKAnnotations
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class CreateEnrolResponseUseCaseTest {
    @MockK
    lateinit var subjectFactory: SubjectFactory

    @MockK
    lateinit var enrolSubject: EnrolSubjectUseCase

    @MockK
    lateinit var project: Project

    private val action = mockk<ActionRequest.EnrolActionRequest> {
        every { projectId } returns "projectId"
        every { userId } returns "userId".asTokenizableRaw()
        every { moduleId } returns "moduleId".asTokenizableRaw()
    }

    private lateinit var useCase: CreateEnrolResponseUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coJustRun { enrolSubject.invoke(any(), any()) }

        useCase = CreateEnrolResponseUseCase(subjectFactory, enrolSubject)
    }

    @Test
    fun `Converts correct results to response`() = runTest {
        every {
            subjectFactory.buildSubjectFromCaptureResults(any(), any(), any(), any(), any())
        } returns mockk { every { subjectId } returns "guid" }

        assertThat(
            useCase(
                action,
                listOf(
                    FingerprintCaptureResult("", emptyList()),
                    FaceCaptureResult("", emptyList()),
                    mockk(),
                ),
                project
            ),
        ).isInstanceOf(AppEnrolResponse::class.java)
    }

    @Test
    fun `Returns error if no valid response`() = runTest {
        every {
            subjectFactory.buildSubjectFromCaptureResults(any(), any(), any(), null, null)
        } throws MissingCaptureException()

        assertThat(useCase(action, emptyList(), project)).isInstanceOf(AppErrorResponse::class.java)
    }
}
