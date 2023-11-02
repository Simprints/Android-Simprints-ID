package com.simprints.id.orchestrator

import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.models.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.testtools.TestData.defaultSubject
import com.simprints.infra.config.store.models.Finger
import com.simprints.infra.enrolment.records.sync.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.sampledata.FACE_TEMPLATE_FORMAT
import com.simprints.infra.events.sampledata.SampleDefaults
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.STATIC_GUID
import com.simprints.infra.events.sampledata.createPersonCreationEvent
import com.simprints.infra.events.sampledata.createSessionCaptureEvent
import com.simprints.infra.eventsync.sync.down.tasks.SubjectFactory
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.mock.mockTemplate
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date
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
        coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
            personCreationEvent
        )

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

    @Test
    fun `given the fingerprint capture response, when subject is build, then subject factory is invoked`() {
        enrolmentHelper.buildSubject(
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            fingerprintResponse = fingerprintResponse,
            faceResponse = null,
            timeHelper = timeHelper
        )

        val captureResult = fingerprintResponse.captureResult.first()
        val identifier = captureResult.identifier
        val sample = captureResult.sample!!
        val sampleFinger = FingerprintSample(
            fingerIdentifier = identifier.fromDomainToModuleApi(),
            template = sample.template,
            templateQualityScore = sample.templateQualityScore,
            format = sample.format
        )
        verify {
            subjectFactory.buildSubject(
                subjectId = STATIC_GUID,
                projectId = projectId,
                attendantId = userId,
                moduleId = moduleId,
                createdAt = any(),
                updatedAt = null,
                fingerprintSamples = listOf(sampleFinger),
                faceSamples = emptyList()
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
        every { guid.toString() } returns STATIC_GUID
        every { UUID.randomUUID() } returns guid
    }

    companion object {
        private val fingerprintSample = FingerprintCaptureSample(
            Finger.LEFT_THUMB,
            templateQualityScore = 10,
            template = EncodingUtilsImplForTests.base64ToBytes(
                "sometemplate"
            ),
            format = "ISO_19794_2"
        )
        private val projectId = "projectId"
        private val userId = "userId".asTokenizableRaw()
        private val moduleId = "moduleId".asTokenizableRaw()
        private val fingerprintResponse = FingerprintCaptureResponse(
            captureResult = listOf(
                FingerprintCaptureResult(
                    Finger.LEFT_THUMB,
                    fingerprintSample
                )
            )
        )
    }
}
