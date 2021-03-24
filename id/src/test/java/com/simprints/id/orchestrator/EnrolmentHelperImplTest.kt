package com.simprints.id.orchestrator

import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.defaultSubject
import com.simprints.id.commontesttools.events.CREATED_AT
import com.simprints.id.commontesttools.events.createPersonCreationEvent
import com.simprints.id.commontesttools.events.createSessionCaptureEvent
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.EnrolmentEventV2
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.SubjectAction
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.tools.mockUUID
import com.simprints.id.tools.time.TimeHelper
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EnrolmentHelperImplTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK lateinit var subjectRepository: SubjectRepository
    @MockK lateinit var eventRepository: EventRepository
    @MockK lateinit var loginInfoManager: LoginInfoManager
    @MockK lateinit var timeHelper: TimeHelper

    private lateinit var enrolmentHelper: EnrolmentHelper
    private val personCreationEvent = createPersonCreationEvent()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        enrolmentHelper = EnrolmentHelperImpl(subjectRepository, eventRepository, loginInfoManager, timeHelper)
        every { timeHelper.now() } returns CREATED_AT
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
        coEvery { eventRepository.getCurrentCaptureSessionEvent() } returns createSessionCaptureEvent()
        coEvery { eventRepository.loadEventsFromSession(any()) } returns flowOf(personCreationEvent)

        mockUUID()
    }

    @Test
    fun enrol_shouldRegisterEnrolmentEvents() {
        runBlocking {

            enrolmentHelper.enrol(defaultSubject)

            val expectedEnrolmentEvent = EnrolmentEventV2(CREATED_AT, defaultSubject.subjectId, defaultSubject.projectId, defaultSubject.moduleId, defaultSubject.attendantId, personCreationEvent.id)

            coVerify {
                eventRepository.addEventToCurrentSession(expectedEnrolmentEvent)
            }
        }
    }

    @Test
    fun enrol_shouldEnrolANewSubject() {
        runBlocking {
            enrolmentHelper.enrol(defaultSubject)

            coVerify(exactly = 1) {
                subjectRepository.performActions(listOf(SubjectAction.Creation(defaultSubject)))
            }
        }
    }

    @Test
    fun enrol_shouldPerformUpload() {
        runBlocking {
            enrolmentHelper.enrol(defaultSubject)

            coVerify(exactly = 1) {
                eventRepository.uploadEvents(projectId = DEFAULT_PROJECT_ID)
            }
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
