package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.event.domain.models.PersonCreationEvent.PersonCreationPayload
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EnrolSubjectUseCaseTest {

    @MockK
    private lateinit var eventRepository: SessionEventRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    private lateinit var useCase: EnrolSubjectUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { eventRepository.getCurrentSessionScope() } returns mockk {
            every { id } returns "sessionId"
        }

        useCase = EnrolSubjectUseCase(
            eventRepository,
            timeHelper,
            enrolmentRecordRepository,
        )
    }

    @Test
    fun `Adds enrolment V2 event when called`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            mockk<PersonCreationEvent> { every { id } returns "personCreationId" }
        )

        useCase.invoke(
            Subject(
                subjectId = "subjectId",
                projectId = "projectId",
                attendantId = "moduleId".asTokenizableRaw(),
                moduleId = "attendantId".asTokenizableRaw()
            )
        )

        coVerify {
            eventRepository.addOrUpdateEvent(withArg {
                assertThat(it).isInstanceOf(EnrolmentEventV2::class.java)
            })
        }
    }

    @Test
    fun `Saves enrolment record when called`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            mockk<PersonCreationEvent> { every { id } returns "personCreationId" }
        )

        useCase.invoke(
            Subject(
                subjectId = "subjectId",
                projectId = "projectId",
                attendantId = "moduleId".asTokenizableRaw(),
                moduleId = "attendantId".asTokenizableRaw()
            )
        )

        coVerify {
            enrolmentRecordRepository.performActions(withArg {
                assertThat(it.first()).isInstanceOf(SubjectAction.Creation::class.java)
            })
        }
    }

    @Test
    fun `Uses latest PersonCreationEvent`() = runTest {
        val personCreationEvent1 = mockk<PersonCreationEvent> {
            every { id } returns "personCreationEventId1"
            every { payload } returns mockk<PersonCreationPayload> {
                every { createdAt } returns Timestamp(1)
            }
        }
        val personCreationId2 = "personCreationEventId2"
        val personCreationEvent2 = mockk<PersonCreationEvent> {
            every { id } returns personCreationId2
            every { payload } returns mockk<PersonCreationPayload> {
                every { createdAt } returns Timestamp(2)
            }
        }
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            personCreationEvent1,
            personCreationEvent2
        )

        useCase.invoke(
            Subject(
                subjectId = "subjectId",
                projectId = "projectId",
                attendantId = "moduleId".asTokenizableRaw(),
                moduleId = "attendantId".asTokenizableRaw()
            )
        )

        coVerify { eventRepository.addOrUpdateEvent(
            match { it is EnrolmentEventV2 && it.payload.personCreationEventId == personCreationId2 }
        ) }
    }
}
