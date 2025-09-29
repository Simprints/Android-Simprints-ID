package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.sample.CaptureIdentity
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.orchestrator.exceptions.MissingCaptureException
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.eventsync.sync.common.SubjectFactory
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.responses.AppEnrolResponse
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import io.mockk.*
import io.mockk.impl.annotations.MockK
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

    private val enrolmentSubjectId = "enrolmentSubjectId"
    private val projectId = "projectId"
    private val credentialEncrypted = "credentialEncrypted".asTokenizableEncrypted()

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
            subjectFactory.buildSubjectFromCaptureResults(
                subjectId = any(),
                projectId = any(),
                attendantId = any(),
                moduleId = any(),
                captures = any(),
                externalCredential = any(),
            )
        } returns mockk { every { subjectId } returns "guid" }

        assertThat(
            useCase(
                request = action,
                results = listOf(
                    CaptureIdentity("", Modality.FINGERPRINT, emptyList()),
                    CaptureIdentity("", Modality.FACE, emptyList()),
                    mockk(),
                ),
                project = project,
                enrolmentSubjectId = enrolmentSubjectId,
            ),
        ).isInstanceOf(AppEnrolResponse::class.java)
    }

    @Test
    fun `Returns error if no valid response`() = runTest {
        every {
            subjectFactory.buildSubjectFromCaptureResults(
                subjectId = any(),
                projectId = any(),
                attendantId = any(),
                moduleId = any(),
                captures = emptyList(),
                externalCredential = null,
            )
        } throws MissingCaptureException()

        assertThat(useCase(action, emptyList(), project, enrolmentSubjectId)).isInstanceOf(AppErrorResponse::class.java)
    }

    @Test
    fun `correctly processes external credential result`() = runTest {
        val externalCredentialType = ExternalCredentialType.GhanaIdCard
        val scannedCredentialMock = mockk<ScannedCredential> {
            every { credential } returns credentialEncrypted
            every { credentialType } returns externalCredentialType
        }
        val credentialSearchResult = mockk<ExternalCredentialSearchResult> {
            every { scannedCredential } returns scannedCredentialMock
        }

        every {
            subjectFactory.buildSubjectFromCaptureResults(
                subjectId = any(),
                projectId = any(),
                attendantId = any(),
                moduleId = any(),
                captures = any(),
                externalCredential = any(),
            )
        } returns mockk { every { subjectId } returns enrolmentSubjectId }

        useCase(
            request = action,
            results = listOf(
                CaptureIdentity("", Modality.FINGERPRINT, emptyList()),
                credentialSearchResult,
            ),
            project = project,
            enrolmentSubjectId = enrolmentSubjectId,
        )

        verify {
            subjectFactory.buildSubjectFromCaptureResults(
                subjectId = enrolmentSubjectId,
                projectId = projectId,
                attendantId = any(),
                moduleId = any(),
                captures = any(),
                externalCredential = match { it.value == credentialEncrypted && it.type == externalCredentialType },
            )
        }
    }
}
