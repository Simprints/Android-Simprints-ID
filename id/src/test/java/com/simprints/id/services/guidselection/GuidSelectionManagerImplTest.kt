package com.simprints.id.services.guidselection

import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.GuidSelectionEvent
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import com.simprints.infra.login.LoginManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GuidSelectionManagerImplTest {

    private lateinit var guidSelectionManager: GuidSelectionManager

    private val guidSelectionRequest = GuidSelectionRequest(DEFAULT_PROJECT_ID, GUID1, GUID2)

    @MockK
    private lateinit var loginManager: LoginManager

    @MockK
    private lateinit var timerHelper: TimeHelper

    @MockK
    private lateinit var eventRepository: EventRepository

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        val scope = CoroutineScope(testCoroutineRule. testCoroutineDispatcher)
        guidSelectionManager =
            GuidSelectionManagerImpl(loginManager, timerHelper, eventRepository, scope)
        every { timerHelper.now() } returns CREATED_AT
        every { loginManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
    }

    @Test
    fun handleConfirmIdentityRequest_shouldCheckProjectId() {
        runBlocking {
            guidSelectionManager.handleConfirmIdentityRequest(guidSelectionRequest)

            verify(exactly = 1) { loginManager.isProjectIdSignedIn(DEFAULT_PROJECT_ID) }
        }
    }

    @Test
    fun handleConfirmIdentityRequest_shouldAddAnEvent() {
        runBlocking {
            every { loginManager.isProjectIdSignedIn(any()) } returns true

            guidSelectionManager.handleConfirmIdentityRequest(guidSelectionRequest)

            coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(any<GuidSelectionEvent>()) }
        }
    }

}
