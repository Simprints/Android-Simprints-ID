package com.simprints.id.services.guidselection

import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_DEVICE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.commontesttools.events.CREATED_AT
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.GuidSelectionEvent
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest
import com.simprints.id.tools.time.TimeHelper
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GuidSelectionManagerImplTest {

    private lateinit var guidSelectionManager: GuidSelectionManager

    private val guidSelectionRequest = GuidSelectionRequest(DEFAULT_PROJECT_ID, GUID1, GUID2)

    @MockK private lateinit var loginInfoManager: LoginInfoManager
    @MockK private lateinit var analyticsManager: AnalyticsManager
    @MockK private lateinit var crashReportManager: CrashReportManager
    @MockK private lateinit var timerHelper: TimeHelper
    @MockK private lateinit var eventRepository: EventRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        guidSelectionManager = GuidSelectionManagerImpl(DEFAULT_DEVICE_ID, loginInfoManager, analyticsManager, crashReportManager, timerHelper, eventRepository)
        every { timerHelper.now() } returns CREATED_AT
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
    }

    @Test
    fun handleConfirmIdentityRequest_shouldCheckProjectId() {
        runBlocking {
            guidSelectionManager.handleConfirmIdentityRequest(guidSelectionRequest)

            verify(exactly = 1) { loginInfoManager.isProjectIdSignedIn(DEFAULT_PROJECT_ID) }
        }
    }

    @Test
    fun handleConfirmIdentityRequest_shouldAddAnEvent() {
        runBlocking {
            every { loginInfoManager.isProjectIdSignedIn(any()) } returns true

            guidSelectionManager.handleConfirmIdentityRequest(guidSelectionRequest)

            coVerify(exactly = 1) { eventRepository.addEventToCurrentSession(any<GuidSelectionEvent>()) }
        }
    }

    @Test
    fun handleConfirmIdentityRequest_shouldReportToAnalytics() {
        runBlocking {
            every { loginInfoManager.isProjectIdSignedIn(any()) } returns true

            guidSelectionManager.handleConfirmIdentityRequest(guidSelectionRequest)

            coVerify(exactly = 1) { analyticsManager.logGuidSelectionWorker(DEFAULT_PROJECT_ID, GUID1, DEFAULT_DEVICE_ID, GUID2, true) }
        }
    }

    @Test
    fun handleConfirmIdentityRequest_somethingWrongHappens_shouldReportToAnalytics() {
        runBlocking {
            guidSelectionManager.handleConfirmIdentityRequest(guidSelectionRequest)

            coVerify(exactly = 1) { analyticsManager.logGuidSelectionWorker(DEFAULT_PROJECT_ID, GUID1, DEFAULT_DEVICE_ID, GUID2, false) }
        }
    }

    @Test
    fun handleConfirmIdentityRequest_somethingWrongHappens_shouldLogException() {
        runBlocking {
            every { loginInfoManager.isProjectIdSignedIn(any()) } throws Throwable("Error")

            guidSelectionManager.handleConfirmIdentityRequest(guidSelectionRequest)

            coVerify(exactly = 1) { crashReportManager.logExceptionOrSafeException(any()) }
        }
    }
}
