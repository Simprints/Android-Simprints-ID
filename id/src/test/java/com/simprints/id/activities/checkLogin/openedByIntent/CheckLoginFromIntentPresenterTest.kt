package com.simprints.id.activities.checkLogin.openedByIntent

import android.os.Build
import android.os.Build.VERSION
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_DEVICE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_METADATA
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.commontesttools.events.CREATED_AT
import com.simprints.id.commontesttools.events.ENDED_AT
import com.simprints.id.commontesttools.events.createEnrolmentCalloutEvent
import com.simprints.id.commontesttools.events.createSessionCaptureEvent
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.callout.*
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
import com.simprints.id.data.db.event.domain.models.session.Location
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigFetcher
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.domain.modality.Modes.FACE
import com.simprints.id.domain.modality.Modes.FINGERPRINT
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppConfirmIdentityRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppEnrolLastBiometricsRequest
import com.simprints.id.secure.models.SecurityState.Status
import com.simprints.id.secure.models.SecurityState.Status.RUNNING
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.time.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CheckLoginFromIntentPresenterTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var presenter: CheckLoginFromIntentPresenter

    @MockK lateinit var view: CheckLoginFromIntentContract.View
    @MockK lateinit var appComponent: AppComponent
    @MockK lateinit var remoteConfigFetcherMock: RemoteConfigFetcher
    @MockK lateinit var analyticsManagerMock: AnalyticsManager
    @MockK lateinit var subjectLocalDataSourceMock: SubjectLocalDataSource
    @MockK lateinit var preferencesManagerMock: PreferencesManager
    @MockK lateinit var eventRepositoryMock: EventRepository
    @MockK lateinit var crashReportManagerMock: CrashReportManager
    @MockK lateinit var securityStateRepositoryMock: SecurityStateRepository
    @MockK lateinit var loginInfoManagerMock: LoginInfoManager
    @MockK lateinit var timeHelperMock: TimeHelper
    @MockK lateinit var simNetworkUtilsMock: SimNetworkUtils

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        UnitTestConfig(this).coroutinesMainThread()

        presenter = CheckLoginFromIntentPresenter(view, DEFAULT_DEVICE_ID, appComponent).apply {
            remoteConfigFetcher = remoteConfigFetcherMock
            analyticsManager = analyticsManagerMock
            subjectLocalDataSource = subjectLocalDataSourceMock
            coEvery { subjectLocalDataSource.count(any()) } returns 0

            simNetworkUtils = simNetworkUtilsMock
            every { simNetworkUtils.connectionsStates } returns emptyList()
            every { simNetworkUtils.mobileNetworkType } returns ""

            loginInfoManager = loginInfoManagerMock.apply {
                every { getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
            }
            LanguageHelper.prefs = mockk(relaxed = true)
            preferencesManager = preferencesManagerMock.apply {
                every { language } returns "EN"
            }
            analyticsManager = analyticsManagerMock
            eventRepository = eventRepositoryMock
            timeHelper = timeHelperMock
            coEvery { timeHelper.now() } returns CREATED_AT

            coEvery { eventRepository.getCurrentCaptureSessionEvent() } returns createSessionCaptureEvent()
            coEvery { eventRepository.getEventsFromSession(any()) } returns emptyFlow()

            coEvery { analyticsManager.getAnalyticsId() } returns GUID1
            crashReportManager = crashReportManagerMock
            securityStateRepository = securityStateRepositoryMock
            val channel = Channel<Status>(capacity = Channel.UNLIMITED)
            coEvery { securityStateRepositoryMock.securityStatusChannel } returns channel
            runBlocking {
                channel.send(RUNNING)
                channel.close()
            }

            appRequest = AppVerifyRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, GUID1)
        }
    }

    @Test
    fun presenter_setupIsCalled_shouldParseAppRequest() {
        runBlockingTest {

            presenter.setup()

            coVerify(exactly = 1) { view.parseRequest() }
        }
    }

    @Test
    fun presenter_setupIsCalledWithAMainFlow_shouldExtractParamsForAnalyticsManager() {
        runBlockingTest {
            val appRequest = AppEnrolRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA)
            every { view.parseRequest() } returns appRequest

            presenter.setup()

            coVerify(exactly = 1) { analyticsManagerMock.logCallout(appRequest) }
            coVerify(exactly = 1) { analyticsManagerMock.logUserProperties(DEFAULT_USER_ID, DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, DEFAULT_DEVICE_ID) }
        }
    }

    @Test
    fun presenter_setupIsCalledWithAFollowUpRequest_shouldNotExtractParamsForAnalyticsManager() {
        runBlockingTest {
            val appRequest = AppEnrolLastBiometricsRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, GUID1)
            every { view.parseRequest() } returns appRequest

            presenter.setup()

            coVerify(exactly = 0) { analyticsManagerMock.logCallout(any()) }
            coVerify(exactly = 0) { analyticsManagerMock.logUserProperties(any(), any(), any(), any()) }
        }
    }

    @Test
    fun presenter_setupIsCalled_shouldSetLastUserId() {
        runBlockingTest {
            val appRequest = AppEnrolRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA)
            every { view.parseRequest() } returns appRequest

            presenter.setup()

            coVerify { preferencesManagerMock.lastUserUsed = DEFAULT_USER_ID }
        }
    }


    @Test
    fun presenter_setupIsCalled_shouldSetSessionIdInCrashlytics() {
        runBlockingTest {
            val session = createSessionCaptureEvent()
            coEvery { eventRepositoryMock.getCurrentCaptureSessionEvent() } returns session
            presenter.setup()

            coVerify { crashReportManagerMock.setSessionIdCrashlyticsKey(session.id) }
        }
    }

    @Test
    fun presenter_setup_shouldAddEnrolmentCallout() {
        runBlockingTest {
            val appRequest = AppEnrolRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA)
            every { view.parseRequest() } returns appRequest

            presenter.setup()

            coVerify { eventRepositoryMock.addOrUpdateEvent(any<EnrolmentCalloutEvent>()) }
        }
    }

    @Test
    fun presenter_setup_shouldAddEnrolLastBiomentricsCallout() {
        runBlockingTest {
            val appRequest = AppEnrolLastBiometricsRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, GUID1)
            every { view.parseRequest() } returns appRequest

            presenter.setup()

            coVerify(exactly = 1) { eventRepositoryMock.addOrUpdateEvent(any<EnrolmentLastBiometricsCalloutEvent>()) }
        }
    }

    @Test
    fun presenter_setup_shouldAddConfirmIdentityCallout() {
        runBlockingTest {
            val appRequest = AppConfirmIdentityRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, GUID1, GUID2)
            every { view.parseRequest() } returns appRequest

            presenter.setup()

            coVerify { eventRepositoryMock.addOrUpdateEvent(any<ConfirmationCalloutEvent>()) }
        }
    }

    @Test
    fun presenter_setup_shouldAddIdentificationCallout() {
        runBlockingTest {
            val appRequest = AppIdentifyRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA)
            every { view.parseRequest() } returns appRequest

            presenter.setup()

            coVerify { eventRepositoryMock.addOrUpdateEvent(any<IdentificationCalloutEvent>()) }
        }
    }

    @Test
    fun presenter_setup_shouldAddVerificationCallout() {
        runBlockingTest {
            val appRequest = AppVerifyRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, GUID1)
            every { view.parseRequest() } returns appRequest

            presenter.setup()

            coVerify { eventRepositoryMock.addOrUpdateEvent(any<VerificationCalloutEvent>()) }
        }
    }

    @Test
    fun presenter_setup_shouldAddInfoToSession() {
        runBlockingTest {

            presenter.handleSignedInUser()

            coVerify(exactly = 3) { eventRepositoryMock.addOrUpdateEvent(any()) }
        }
    }

    @Test
    fun presenter_signedIn_shouldUpdateUserId() {
        runBlockingTest {

            presenter.handleSignedInUser()

            coVerify { loginInfoManagerMock setProperty "signedInUserId" value DEFAULT_USER_ID }
        }
    }

    @Test
    fun presenter_signedIn_updateCurrentSession() {
        runBlocking {
            val subjectCount = 3
            val analyticsId = "analyticsId"
            val projectId = DEFAULT_PROJECT_ID

            val session = createSessionCaptureEvent(projectId = GUID1)
            coEvery { eventRepositoryMock.getCurrentCaptureSessionEvent() } returns session
            coEvery { eventRepositoryMock.getEventsFromSession(any()) } returns emptyFlow()
            coEvery { subjectLocalDataSourceMock.count(any()) } returns subjectCount
            coEvery { analyticsManagerMock.getAnalyticsId() } returns GUID1
            coEvery { loginInfoManagerMock.getSignedInProjectIdOrEmpty() } returns projectId
            every { preferencesManagerMock.modalities } returns listOf(FINGER, Modality.FACE)

            presenter.handleSignedInUser()

            val appVersionNameArg = "appVersionName"
            val libSimprintsVersionNameArg = "libSimprintsVersionName"
            val languageArg = "language"
            val deviceArg = Device(
                VERSION.SDK_INT.toString(),
                Build.MANUFACTURER + "_" + Build.MODEL,
                GUID1)

            val databaseInfoArg = DatabaseInfo(2, subjectCount)
            val locationArg = Location(0.0, 0.0)
            val expected = SessionCaptureEvent(
                GUID1,
                DEFAULT_PROJECT_ID,
                CREATED_AT,
                listOf(FINGERPRINT, FACE),
                appVersionNameArg,
                libSimprintsVersionNameArg,
                languageArg,
                deviceArg,
                databaseInfoArg).apply {
                payload.location = locationArg
                payload.analyticsId = GUID1
                payload.endedAt = ENDED_AT
            }

            coVerify {
                eventRepositoryMock.addOrUpdateEvent(expected)
            }
        }
    }

    @Test
    fun presenter_signedIn_updateProjectIdInEventsInCurrentSession() {
        runBlockingTest {
            val session = createSessionCaptureEvent(projectId = GUID1)
            val callout = createEnrolmentCalloutEvent(projectId = GUID1)
            coEvery { eventRepositoryMock.getCurrentCaptureSessionEvent() } returns session
            coEvery { eventRepositoryMock.getEventsFromSession(any()) } returns flowOf(session, callout)
            coEvery { subjectLocalDataSourceMock.count(any()) } returns 2

            presenter.appRequest = AppVerifyRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, GUID1)

            presenter.handleSignedInUser()

            coVerify {
                eventRepositoryMock.addOrUpdateEvent(createEnrolmentCalloutEvent(GUID1).apply {
                    labels = labels.copy(projectId = DEFAULT_PROJECT_ID)
                })
            }
        }
    }
}
