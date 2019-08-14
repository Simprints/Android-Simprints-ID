package com.simprints.id.activities.checkLogin.openedByIntent

import com.simprints.id.commontesttools.sessionEvents.createFakeSession
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.*
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers

class CheckLoginFromIntentPresenterTest {

    private val view = spy<CheckLoginFromIntentActivity>()

    @Before
    fun setUp() {
        UnitTestConfig(this).rescheduleRxMainThread()
    }

    @Test
    fun givenCheckLoginFromIntentPresenter_setupIsCalled_shouldAddCalloutEvent() {
        val checkLoginFromIntentPresenter = spy(CheckLoginFromIntentPresenter(view , "device_id", mock())).apply {

            whenever(view) { parseRequest() } thenReturn mock<AppEnrolRequest>()
            remoteConfigFetcher = mock()
            analyticsManager = mock()
            dbManager = mock()
            preferencesManager = mock()

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
        val checkLoginFromIntentPresenter = spy(CheckLoginFromIntentPresenter(view, "device_id", mock()))

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
        val checkLoginFromIntentPresenter = spy(CheckLoginFromIntentPresenter(view, "device_id", mock()))

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
        val checkLoginFromIntentPresenter = spy(CheckLoginFromIntentPresenter(view, "device_id", mock()))

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

        val checkLoginFromIntentPresenter = spy(CheckLoginFromIntentPresenter(view, "device_id", mock())).apply {

            whenever(view) { parseRequest() } thenReturn mock<AppEnrolRequest>()

            remoteConfigFetcher = mock()
            analyticsManager = mock()
            preferencesManager = mock()

            dbManager = mock<LocalDbManager>().apply {
                whenever(this) { getPeopleCountFromLocal() } thenReturn Single.just(0)
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

        verifyOnce(checkLoginFromIntentPresenter) { addAnalyticsInfo() }
        verifyOnce(checkLoginFromIntentPresenter) { populateSessionWithAnalyticsIdAndDbInfo("analyticsId", 0) }
    }
}
