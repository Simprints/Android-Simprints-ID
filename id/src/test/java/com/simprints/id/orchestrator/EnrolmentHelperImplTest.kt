package com.simprints.id.orchestrator

import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.EnrolmentEventV2
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.createPersonCreationEvent
import com.simprints.eventsystem.sampledata.createSessionCaptureEvent
import com.simprints.id.testtools.TestData.defaultSubject
import com.simprints.id.tools.mockUUID
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.SubjectAction
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EnrolmentHelperImplTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var timeHelper: TimeHelper

    private lateinit var enrolmentHelper: EnrolmentHelper
    private val personCreationEvent = createPersonCreationEvent()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        enrolmentHelper = EnrolmentHelperImpl(enrolmentRecordManager, eventRepository, timeHelper)
        every { timeHelper.now() } returns CREATED_AT
        coEvery { eventRepository.getCurrentCaptureSessionEvent() } returns createSessionCaptureEvent()
        coEvery { eventRepository.getEventsFromSession(any()) } returns flowOf(personCreationEvent)

        mockUUID()
    }

    @Test
    fun enrol_shouldRegisterEnrolmentEvents() = runTest {

        enrolmentHelper.enrol(defaultSubject)

        val expectedEnrolmentEvent = EnrolmentEventV2(
            CREATED_AT,
            defaultSubject.subjectId,
            defaultSubject.projectId,
            defaultSubject.moduleId,
            defaultSubject.attendantId,
            personCreationEvent.id
        )

        coVerify {
            eventRepository.addOrUpdateEvent(expectedEnrolmentEvent)
        }
    }

    @Test
    fun enrol_shouldEnrolANewSubject() = runTest {
        enrolmentHelper.enrol(defaultSubject)

        coVerify(exactly = 1) {
            enrolmentRecordManager.performActions(listOf(SubjectAction.Creation(defaultSubject)))
        }
    }

    @Test
    fun `enrol should run correct actions`() = runTest {

        enrolmentHelper.enrol(defaultSubject)

        val expectedEnrolmentEvent = EnrolmentEventV2(
            CREATED_AT,
            defaultSubject.subjectId,
            defaultSubject.projectId,
            defaultSubject.moduleId,
            defaultSubject.attendantId,
            personCreationEvent.id
        )

        coVerify { eventRepository.addOrUpdateEvent(expectedEnrolmentEvent) }
        coVerify(exactly = 1) {
            enrolmentRecordManager.performActions(
                listOf(
                    SubjectAction.Creation(
                        defaultSubject
                    )
                )
            )
        }
        coVerify(exactly = 0) {
            eventRepository.uploadEvents(
                projectId = DEFAULT_PROJECT_ID,
                canSyncAllDataToSimprints = false,
                canSyncBiometricDataToSimprints = false,
                canSyncAnalyticsDataToSimprints = false
            )
        }

    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
