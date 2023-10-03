package com.simprints.feature.orchestrator.usecases.response

import android.os.Bundle
import com.google.common.truth.Truth.assertThat
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.orchestrator.model.responses.AppEnrolResponse
import com.simprints.feature.orchestrator.model.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.ActionRequest
import io.mockk.MockKAnnotations
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CreateEnrolResponseUseCaseTest {

    @MockK
    lateinit var buildSubject: BuildEnrolledSubjectUseCase

    @MockK
    lateinit var enrolSubject: EnrolSubjectUseCase

    private val action = mockk<ActionRequest.EnrolActionRequest> {
        every { projectId } returns "projectId"
        every { userId } returns "userId"
        every { moduleId } returns "moduleId"
    }

    private lateinit var useCase: CreateEnrolResponseUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coJustRun { enrolSubject.invoke(any()) }

        useCase = CreateEnrolResponseUseCase(buildSubject, enrolSubject)
    }

    @Test
    fun `Converts correct results to response`() = runTest {
        every {
            buildSubject.invoke(any(), any(), any(), any())
        } returns mockk { every { subjectId } returns "guid" }


        assertThat(useCase(action, listOf(
            FaceCaptureResult(emptyList()),
            Bundle(),
        ))).isInstanceOf(AppEnrolResponse::class.java)
    }


    @Test
    fun `Returns error if no valid response`() = runTest {
        every {
            buildSubject.invoke(any(), any(), any(), null)
        } throws IllegalStateException("Missing capture results")

        assertThat(useCase(action, emptyList())).isInstanceOf(AppErrorResponse::class.java)
    }
}
