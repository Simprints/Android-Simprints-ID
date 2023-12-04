package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EnrolSubjectUseCaseTest {

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    private lateinit var useCase: EnrolSubjectUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { eventRepository.getCurrentCaptureSessionEvent() } returns mockk {
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
        coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
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
        coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
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
}
