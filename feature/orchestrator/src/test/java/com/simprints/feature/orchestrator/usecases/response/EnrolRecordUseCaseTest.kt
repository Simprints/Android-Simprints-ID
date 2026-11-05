package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordAction
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent.BiometricReferenceCreationPayload
import com.simprints.infra.events.event.domain.models.EnrolmentEventV4
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureValueEvent
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.session.SessionEventRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EnrolRecordUseCaseTest {
    @MockK
    private lateinit var eventRepository: SessionEventRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    private lateinit var project: Project

    private lateinit var useCase: EnrolRecordUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { eventRepository.getCurrentSessionScope() } returns mockk {
            every { id } returns "sessionId"
        }

        useCase = EnrolRecordUseCase(
            eventRepository,
            timeHelper,
            enrolmentRecordRepository,
        )
    }

    @Test
    fun `Adds enrolment V4 event when called`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            mockk<BiometricReferenceCreationEvent> {
                every { payload.id } returns "referenceId"
            },
        )

        useCase.invoke(
            EnrolmentRecord(
                subjectId = "subjectId",
                projectId = "projectId",
                attendantId = "moduleId".asTokenizableRaw(),
                moduleId = "attendantId".asTokenizableRaw(),
            ),
            project,
        )

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(it).isInstanceOf(EnrolmentEventV4::class.java)
                },
            )
        }
    }

    @Test
    fun `Saves enrolment record when called`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            mockk<PersonCreationEvent> { every { id } returns "personCreationId" },
        )

        useCase.invoke(
            EnrolmentRecord(
                subjectId = "subjectId",
                projectId = "projectId",
                attendantId = "moduleId".asTokenizableRaw(),
                moduleId = "attendantId".asTokenizableRaw(),
            ),
            project,
        )

        coVerify {
            enrolmentRecordRepository.performActions(
                withArg {
                    assertThat(it.first()).isInstanceOf(EnrolmentRecordAction.Creation::class.java)
                },
                project,
            )
        }
    }

    @Test
    fun `Uses all BiometricReferenceCreationEvent`() = runTest {
        val biometricReferenceCreationEvent1 = mockk<BiometricReferenceCreationEvent> {
            every { payload } returns mockk<BiometricReferenceCreationPayload> {
                every { createdAt } returns Timestamp(1)
                every { id } returns "referenceId1"
            }
        }
        val biometricReferenceCreationEvent2 = mockk<BiometricReferenceCreationEvent> {
            every { payload } returns mockk<BiometricReferenceCreationPayload> {
                every { createdAt } returns Timestamp(2)
                every { id } returns "referenceId2"
            }
        }
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            biometricReferenceCreationEvent2,
            biometricReferenceCreationEvent1,
        )

        useCase.invoke(
            EnrolmentRecord(
                subjectId = "subjectId",
                projectId = "projectId",
                attendantId = "moduleId".asTokenizableRaw(),
                moduleId = "attendantId".asTokenizableRaw(),
            ),
            project,
        )

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(it).isInstanceOf(EnrolmentEventV4::class.java)
                    assertThat((it as EnrolmentEventV4).payload.biometricReferenceIds)
                        .containsExactly("referenceId1", "referenceId2")
                },
            )
        }
    }

    @Test
    fun `Only references the external credential linked to the record when there were recaptures`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            captureValueEvent("discardedScanId"),
            captureValueEvent("finalScanId"),
        )

        useCase.invoke(enrolmentRecord(listOf(externalCredential("finalScanId"))), project)

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat((it as EnrolmentEventV4).payload.externalCredentialIds).containsExactly("finalScanId")
                },
            )
        }
    }

    @Test
    fun `Does not reference external credentials when none are linked to the record`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            captureValueEvent("scanId"),
        )

        useCase.invoke(enrolmentRecord(emptyList()), project)

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat((it as EnrolmentEventV4).payload.externalCredentialIds).isEmpty()
                },
            )
        }
    }

    private fun captureValueEvent(payloadId: String) = mockk<ExternalCredentialCaptureValueEvent> {
        every { payload.id } returns payloadId
    }

    private fun externalCredential(id: String) = ExternalCredential(
        id = id,
        value = "credential".asTokenizableEncrypted(),
        subjectId = "subjectId",
        type = ExternalCredentialType.NHISCard,
    )

    private fun enrolmentRecord(credentials: List<ExternalCredential>) = EnrolmentRecord(
        subjectId = "subjectId",
        projectId = "projectId",
        attendantId = "attendantId".asTokenizableRaw(),
        moduleId = "moduleId".asTokenizableRaw(),
        externalCredentials = credentials,
    )
}
