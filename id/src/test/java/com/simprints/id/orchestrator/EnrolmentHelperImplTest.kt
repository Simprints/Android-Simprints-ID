package com.simprints.id.orchestrator

import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.testtools.TestData.defaultSubject
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.SubjectAction
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.sampledata.SampleDefaults
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.createPersonCreationEvent
import com.simprints.infra.events.sampledata.createSessionCaptureEvent
import com.simprints.infra.eventsync.sync.down.tasks.SubjectFactory
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class EnrolmentHelperImplTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var subjectFactory: SubjectFactory

    private lateinit var enrolmentHelper: EnrolmentHelper
    private val personCreationEvent = createPersonCreationEvent()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        enrolmentHelper = EnrolmentHelperImpl(
            enrolmentRecordManager = enrolmentRecordManager,
            eventRepository = eventRepository,
            timeHelper = timeHelper,
            subjectFactory = subjectFactory
        )
        every { timeHelper.now() } returns CREATED_AT
        coEvery { eventRepository.getCurrentCaptureSessionEvent() } returns createSessionCaptureEvent()
        coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(personCreationEvent)

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
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun mockUUID() {
        mockkStatic(UUID::class)
        val guid = mockk<UUID>()
        every { guid.toString() } returns SampleDefaults.STATIC_GUID
        every { UUID.randomUUID() } returns guid
    }
}
