package com.simprints.id.activities.checkLogin.openedByIntent

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.sessionEvents.createFakeSession
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.*
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class CheckLoginFromIntentPresenterTest {

    private val app = ApplicationProvider.getApplicationContext<TestApplication>()

    private val appModule by lazy {
        TestAppModule(
            app,
            crashReportManagerRule = DependencyRule.MockRule,
            deviceManagerRule = DependencyRule.MockRule
        )
    }

    private val viewSpy = spy<CheckLoginFromIntentActivity>()

    @Before
    fun setUp() {
        UnitTestConfig(this, appModule).fullSetup()
    }

    @Test
    fun givenCheckLoginFromIntentPresenter_setupIsCalled_shouldAddCalloutEvent() {
        val checkLoginFromIntentPresenter = spy(CheckLoginFromIntentPresenter(viewSpy, "device_id", mock())).apply {

            whenever(view) { parseRequest() } thenReturn mock<AppEnrolRequest>()
            remoteConfigFetcher = mock()
            analyticsManager = mock()
            personLocalDataSource = mock()
            preferencesManager = mock()

            analyticsManager = mock<AnalyticsManager>().apply {
                whenever(this) { analyticsId } thenReturn Single.just("analyticsId")
            }

            crashReportManager = mock<CrashReportManager>().apply {
                whenever(this) { setSessionIdCrashlyticsKey(anyNotNull()) } thenDoNothing {}
            }

            sessionEventsManager = mock<SessionEventsManager>().apply {
                whenever(this) { createSession("") } thenReturn Single.just(createFakeSession())
                whenever(this) { getCurrentSession() } thenReturn Single.just(createFakeSession())
            }
        }

        checkLoginFromIntentPresenter.setup()

        verifyOnce(checkLoginFromIntentPresenter) { addCalloutAndConnectivityEventsInSession(anyNotNull()) }
    }

    @Test
    fun givenCheckLoginFromIntentPresenter_buildRequestIsCalledForEnrolment_buildsEnrolmentCallout() {
        val checkLoginFromIntentPresenter = spy(CheckLoginFromIntentPresenter(viewSpy, "device_id", mock()))

        checkLoginFromIntentPresenter.appRequest = mock<AppEnrolRequest>().apply {
            whenever(this) { projectId } thenReturn "projectId"
            whenever(this) { userId } thenReturn "userId"
            whenever(this) { moduleId } thenReturn "moduleId"
            whenever(this) { metadata } thenReturn "metadata"
        }

        checkLoginFromIntentPresenter.buildRequestEvent(10, checkLoginFromIntentPresenter.appRequest)

        verifyOnce(checkLoginFromIntentPresenter) {
            buildEnrolmentCalloutEvent(anyNotNull(), ArgumentMatchers.anyLong())
        }
    }

    @Test
    fun givenCheckLoginFromIntentPresenter_buildRequestIsCalledForIdentification_buildsIdentificationCallout() {
        val checkLoginFromIntentPresenter = spy(CheckLoginFromIntentPresenter(viewSpy, "device_id", mock()))

        checkLoginFromIntentPresenter.appRequest = mock<AppIdentifyRequest>().apply {
            whenever(this) { projectId } thenReturn "projectId"
            whenever(this) { userId } thenReturn "userId"
            whenever(this) { moduleId } thenReturn "moduleId"
            whenever(this) { metadata } thenReturn "metadata"
        }

        checkLoginFromIntentPresenter.buildRequestEvent(10, checkLoginFromIntentPresenter.appRequest)

        verifyOnce(checkLoginFromIntentPresenter) {
            buildIdentificationCalloutEvent(anyNotNull(), ArgumentMatchers.anyLong())
        }
    }

    @Test
    fun givenCheckLoginFromIntentPresenter_buildRequestIsCalledForVerification_buildsVerificationCallout() {
        val checkLoginFromIntentPresenter = spy(CheckLoginFromIntentPresenter(viewSpy, "device_id", mock()))

        checkLoginFromIntentPresenter.appRequest = mock<AppVerifyRequest>().apply {
            whenever(this) { projectId } thenReturn "projectId"
            whenever(this) { userId } thenReturn "userId"
            whenever(this) { moduleId } thenReturn "moduleId"
            whenever(this) { metadata } thenReturn "metadata"
            whenever(this) { verifyGuid } thenReturn "verifyGuid"
        }

        checkLoginFromIntentPresenter.buildRequestEvent(10, checkLoginFromIntentPresenter.appRequest)

        verifyOnce(checkLoginFromIntentPresenter) {
            buildVerificationCalloutEvent(anyNotNull(), ArgumentMatchers.anyLong())
        }
    }

    @Test
    fun givenCheckLoginFromIntentPresenter_setupIsCalled_shouldAddInfoToSession() {

        val checkLoginFromIntentPresenter = spy(CheckLoginFromIntentPresenter(viewSpy, "device_id", mock())).apply {

            whenever(view) { parseRequest() } thenReturn mock<AppEnrolRequest>()

            remoteConfigFetcher = mock()
            analyticsManager = mock()
            preferencesManager = mock()

            personLocalDataSource = mock<PersonLocalDataSource>().apply {
                wheneverOnSuspend(this) { count() } thenOnBlockingReturn 0
            }

            crashReportManager = mock<CrashReportManager>().apply {
                whenever(this) { setSessionIdCrashlyticsKey(anyNotNull()) } thenDoNothing {}
            }

            sessionEventsManager = mock<SessionEventsManager>().apply {
                whenever(this) { createSession("") } thenReturn Single.just(createFakeSession())
                whenever(this) { getCurrentSession() } thenReturn Single.just(createFakeSession())
                whenever(this) { getSessionCount() } thenReturn Single.just(0)
            }

            analyticsManager = mock<AnalyticsManager>().apply {
                whenever(this) { analyticsId } thenReturn Single.just("analyticsId")
            }
        }

        checkLoginFromIntentPresenter.setup()

        verifyOnce(checkLoginFromIntentPresenter) { addAnalyticsInfoAndProjectId() }
    }

    @Test
    fun withRootedDevice_shouldLogException() {
        val presenterSpy = spy(CheckLoginFromIntentPresenter(mock(), "device_id", app.component))
        whenever { presenterSpy.deviceManager.isDeviceRooted() } thenReturn true

        presenterSpy.start()

        verifyOnce(presenterSpy.crashReportManager) {
            logExceptionOrSafeException(anyNotNull())
        }
    }

    @Test
    fun withRootedDevice_shouldShowAlertScreen() {
        val presenterSpy = spy(CheckLoginFromIntentPresenter(mock(), "device_id", app.component))
        whenever(presenterSpy.deviceManager) { isDeviceRooted() } thenReturn true

        presenterSpy.start()

        verifyOnce(presenterSpy.view) {
            openAlertActivityForError(AlertType.ROOTED_DEVICE)
        }
    }

}
