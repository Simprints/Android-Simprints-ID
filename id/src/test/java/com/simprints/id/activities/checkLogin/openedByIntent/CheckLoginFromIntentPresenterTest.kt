package com.simprints.id.activities.checkLogin.openedByIntent

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.sessionEvents.createFakeSession
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.extensions.just
import com.simprints.testtools.common.di.DependencyRule
import io.mockk.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class CheckLoginFromIntentPresenterTest {

    private val app = ApplicationProvider.getApplicationContext<TestApplication>()

    private val appModule by lazy {
        TestAppModule(app, crashReportManagerRule = DependencyRule.MockRule)
    }

    private val viewMock = mockk<CheckLoginFromIntentActivity>()

    @Before
    fun setUp() {
        UnitTestConfig(this, appModule).fullSetup()
    }

    @Test
    fun givenCheckLoginFromIntentPresenter_setupIsCalled_shouldAddCalloutEvent() {
        runBlockingTest {
            val checkLoginFromIntentPresenter = spyk(CheckLoginFromIntentPresenter(viewMock, "device_id", mockk(relaxed = true))).apply {

                coEvery { viewMock.parseRequest() } returns mockk(relaxed = true)
                remoteConfigFetcher = mockk()
                analyticsManager = mockk()
                personLocalDataSource = mockk()
                preferencesManager = mockk()

                analyticsManager = mockk()
                coEvery { analyticsManager.getAnalyticsId() } returns "analyticsId"

                crashReportManager = mockk()
                coEvery { crashReportManager.setSessionIdCrashlyticsKey(any()) } just Runs

                sessionRepository = mockk(relaxed = true)
                coEvery { sessionRepository.getCurrentSession() } returns createFakeSession()
            }

            checkLoginFromIntentPresenter.setup()

            coVerify(exactly = 1) { checkLoginFromIntentPresenter.addCalloutAndConnectivityEventsInSession(any()) }
        }
    }

    @Test
    fun givenCheckLoginFromIntentPresenter_buildRequestIsCalledForEnrolment_buildsEnrolmentCallout() {
        val checkLoginFromIntentPresenter = spyk(CheckLoginFromIntentPresenter(viewMock, "device_id", mockk(relaxed = true)))

        checkLoginFromIntentPresenter.appRequest = mockk<AppEnrolRequest>().apply {
            every { this@apply.projectId } returns "projectId"
            every { this@apply.userId } returns "userId"
            every { this@apply.moduleId } returns "moduleId"
            every { this@apply.metadata } returns "metadata"
        }

        checkLoginFromIntentPresenter.buildRequestEvent(10, checkLoginFromIntentPresenter.appRequest as AppEnrolRequest)

        verify(exactly = 1) { checkLoginFromIntentPresenter.buildEnrolmentCalloutEvent(any(), any()) }
    }

    @Test
    fun givenCheckLoginFromIntentPresenter_buildRequestIsCalledForConfirmIdentity_buildsConfirmIdentityCallout() {
        val checkLoginFromIntentPresenter = spyk(CheckLoginFromIntentPresenter(viewMock, "device_id", mockk(relaxed = true)))

        checkLoginFromIntentPresenter.appRequest = mockk<AppRequest.AppConfirmIdentityRequest>().apply {
            every { this@apply.projectId } returns "projectId"
            every { this@apply.userId } returns "userId"
            every { this@apply.sessionId } returns "sessionId"
            every { this@apply.selectedGuid } returns "selectedGuid"
        }

        checkLoginFromIntentPresenter.buildRequestEvent(10, checkLoginFromIntentPresenter.appRequest as AppRequest.AppConfirmIdentityRequest)

        verify(exactly = 1) { checkLoginFromIntentPresenter.addConfirmationCalloutEvent(any(), any()) }
    }

    @Test
    fun givenCheckLoginFromIntentPresenter_buildRequestIsCalledForIdentification_buildsIdentificationCallout() {
        val checkLoginFromIntentPresenter = spyk(CheckLoginFromIntentPresenter(viewMock, "device_id", mockk(relaxed = true)))

        checkLoginFromIntentPresenter.appRequest = mockk<AppIdentifyRequest>().apply {
            every { this@apply.projectId } returns "projectId"
            every { this@apply.userId } returns "userId"
            every { this@apply.moduleId } returns "moduleId"
            every { this@apply.metadata } returns "metadata"
        }

        checkLoginFromIntentPresenter.buildRequestEvent(10, checkLoginFromIntentPresenter.appRequest as AppIdentifyRequest)

        verify(exactly = 1) { checkLoginFromIntentPresenter.buildIdentificationCalloutEvent(any(), any()) }
    }

    @Test
    fun givenCheckLoginFromIntentPresenter_buildRequestIsCalledForVerification_buildsVerificationCallout() {
        val checkLoginFromIntentPresenter = spyk(CheckLoginFromIntentPresenter(viewMock, "device_id", mockk(relaxed = true)))

        checkLoginFromIntentPresenter.appRequest = mockk<AppVerifyRequest>().apply {
            every { this@apply.projectId } returns "projectId"
            every { this@apply.userId } returns "userId"
            every { this@apply.moduleId } returns "moduleId"
            every { this@apply.metadata } returns "metadata"
            every { this@apply.verifyGuid } returns "verifyGuid"
        }

        checkLoginFromIntentPresenter.buildRequestEvent(10, checkLoginFromIntentPresenter.appRequest as AppVerifyRequest)

        verify(exactly = 1) { checkLoginFromIntentPresenter.buildVerificationCalloutEvent(any(), any()) }

    }

    @Test
    fun givenCheckLoginFromIntentPresenter_setupIsCalled_shouldAddInfoToSession() {
        runBlockingTest {
            val checkLoginFromIntentPresenter = spyk(CheckLoginFromIntentPresenter(viewMock, "device_id", mockk(relaxed = true))).apply {

                every { view.parseRequest() } returns mockk<AppEnrolRequest>()

                remoteConfigFetcher = mockk()
                analyticsManager = mockk()
                preferencesManager = mockk()

                personLocalDataSource = mockk()
                coEvery { personLocalDataSource.count() } returns 0


                crashReportManager = mockk()
                every { crashReportManager.setSessionIdCrashlyticsKey(any()) } just runs

                sessionRepository = mockk(relaxed = true)
                coEvery { sessionRepository.getCurrentSession() } returns createFakeSession()

                analyticsManager = mockk()
                coEvery { analyticsManager.getAnalyticsId() } returns "analyticsId"
            }

            checkLoginFromIntentPresenter.setup()

            coVerify(exactly = 1) { checkLoginFromIntentPresenter.addAnalyticsInfoAndProjectId() }
        }
    }

}
