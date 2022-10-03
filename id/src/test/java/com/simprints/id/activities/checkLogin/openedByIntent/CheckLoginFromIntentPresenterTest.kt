package com.simprints.id.activities.checkLogin.openedByIntent

import android.os.Build
import android.os.Build.VERSION
import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.modality.Modes.FACE
import com.simprints.core.domain.modality.Modes.FINGERPRINT
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.callout.*
import com.simprints.eventsystem.event.domain.models.session.DatabaseInfo
import com.simprints.eventsystem.event.domain.models.session.Device
import com.simprints.eventsystem.event.domain.models.session.Location
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_DEVICE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_METADATA
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.ENDED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID2
import com.simprints.eventsystem.sampledata.createEnrolmentCalloutEvent
import com.simprints.eventsystem.sampledata.createSessionCaptureEvent
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppConfirmIdentityRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppEnrolLastBiometricsRequest
import com.simprints.id.secure.models.SecurityState.Status
import com.simprints.id.secure.models.SecurityState.Status.RUNNING
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.infra.login.LoginManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CheckLoginFromIntentPresenterTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var presenter: CheckLoginFromIntentPresenter

    @MockK
    lateinit var view: CheckLoginFromIntentContract.View

    @MockK
    lateinit var appComponent: AppComponent

    @MockK
    lateinit var subjectLocalDataSourceMock: SubjectLocalDataSource

    @MockK
    lateinit var preferencesManagerMock: IdPreferencesManager

    @MockK
    lateinit var eventRepositoryMock: EventRepository

    @MockK
    lateinit var securityStateRepositoryMock: SecurityStateRepository

    @MockK
    lateinit var timeHelperMock: TimeHelper

    @MockK
    lateinit var simNetworkUtilsMock: SimNetworkUtils

    private val loginManagerMock = mockk<LoginManager>(relaxed = true)

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        UnitTestConfig().coroutinesMainThread()

        presenter = CheckLoginFromIntentPresenter(
            view,
            DEFAULT_DEVICE_ID,
            appComponent,
            testCoroutineRule.testCoroutineDispatcher
        ).apply {
            subjectLocalDataSource = subjectLocalDataSourceMock
            coEvery { subjectLocalDataSource.count(any()) } returns 0

            simNetworkUtils = simNetworkUtilsMock
            every { simNetworkUtils.connectionsStates } returns emptyList()

            loginManager = loginManagerMock.apply {
                every { getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
            }
            LanguageHelper.prefs = mockk(relaxed = true)
            preferencesManager = preferencesManagerMock.apply {
                every { language } returns "EN"
            }
            eventRepository = eventRepositoryMock
            timeHelper = timeHelperMock
            coEvery { timeHelper.now() } returns CREATED_AT

            coEvery { eventRepository.getCurrentCaptureSessionEvent() } returns createSessionCaptureEvent()
            coEvery { eventRepository.getEventsFromSession(any()) } returns emptyFlow()

            securityStateRepository = securityStateRepositoryMock
            val channel = Channel<Status>(capacity = Channel.UNLIMITED)
            coEvery { securityStateRepositoryMock.getSecurityStatusFromLocal() } returns RUNNING

            appRequest = AppVerifyRequest(
                DEFAULT_PROJECT_ID,
                DEFAULT_USER_ID,
                DEFAULT_MODULE_ID,
                DEFAULT_METADATA,
                GUID1
            )
        }
    }

    @Test
    fun presenter_setupIsCalled_shouldParseAppRequest() {
        runTest(UnconfinedTestDispatcher()) {

            presenter.setup()

            coVerify(exactly = 1) { view.parseRequest() }
        }
    }

    @Test
    fun presenter_setupIsCalled_shouldSetLastUserId() {
        runTest(UnconfinedTestDispatcher()) {
            val appRequest = AppEnrolRequest(
                DEFAULT_PROJECT_ID,
                DEFAULT_USER_ID,
                DEFAULT_MODULE_ID,
                DEFAULT_METADATA
            )
            every { view.parseRequest() } returns appRequest

            presenter.setup()

            coVerify { preferencesManagerMock.lastUserUsed = DEFAULT_USER_ID }
        }
    }

    @Test
    fun presenter_setup_shouldAddEnrolmentCallout() {
        runTest(UnconfinedTestDispatcher()) {
            val appRequest = AppEnrolRequest(
                DEFAULT_PROJECT_ID,
                DEFAULT_USER_ID,
                DEFAULT_MODULE_ID,
                DEFAULT_METADATA
            )
            every { view.parseRequest() } returns appRequest

            presenter.setup()

            coVerify { eventRepositoryMock.addOrUpdateEvent(any<EnrolmentCalloutEvent>()) }
        }
    }

    @Test
    fun presenter_setup_shouldAddEnrolLastBiomentricsCallout() {
        runTest(UnconfinedTestDispatcher()) {
            val appRequest = AppEnrolLastBiometricsRequest(
                DEFAULT_PROJECT_ID,
                DEFAULT_USER_ID,
                DEFAULT_MODULE_ID,
                DEFAULT_METADATA,
                GUID1
            )
            every { view.parseRequest() } returns appRequest

            presenter.setup()

            coVerify(exactly = 1) { eventRepositoryMock.addOrUpdateEvent(any<EnrolmentLastBiometricsCalloutEvent>()) }
        }
    }

    @Test
    fun presenter_setup_shouldAddConfirmIdentityCallout() {
        runTest(UnconfinedTestDispatcher()) {
            val appRequest =
                AppConfirmIdentityRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, GUID1, GUID2)
            every { view.parseRequest() } returns appRequest

            presenter.setup()

            coVerify { eventRepositoryMock.addOrUpdateEvent(any<ConfirmationCalloutEvent>()) }
        }
    }

    @Test
    fun presenter_setup_shouldAddIdentificationCallout() {
        runTest(UnconfinedTestDispatcher()) {
            val appRequest = AppIdentifyRequest(
                DEFAULT_PROJECT_ID,
                DEFAULT_USER_ID,
                DEFAULT_MODULE_ID,
                DEFAULT_METADATA
            )
            every { view.parseRequest() } returns appRequest

            presenter.setup()

            coVerify { eventRepositoryMock.addOrUpdateEvent(any<IdentificationCalloutEvent>()) }
        }
    }

    @Test
    fun presenter_setup_shouldShowConfirmationTextForAppConfirmIdentityRequest() = runTest(
        UnconfinedTestDispatcher()
    ) {
        val appRequest = AppConfirmIdentityRequest(
            DEFAULT_PROJECT_ID,
            DEFAULT_USER_ID,
            GUID1,
            GUID2
        )
        every { view.parseRequest() } returns appRequest

        presenter.setup()

        // showConfirmationText should be called for AppConfirmIdentityRequest type
        coVerify { view.showConfirmationText() }
    }

    @Test
    fun presenter_setup_shouldNotShowConfirmationTextForAppIdentifyRequest() = runTest(
        UnconfinedTestDispatcher()
    ) {
        val appRequest = AppIdentifyRequest(
            DEFAULT_PROJECT_ID,
            DEFAULT_USER_ID,
            DEFAULT_MODULE_ID,
            DEFAULT_METADATA
        )
        every { view.parseRequest() } returns appRequest
        presenter.setup()
        // showConfirmationText shouldn't be called for AppIdentifyRequest type
        coVerify(exactly = 0) { view.showConfirmationText() }

    }

    @Test
    fun presenter_setup_shouldAddVerificationCallout() {
        runTest(UnconfinedTestDispatcher()) {
            val appRequest = AppVerifyRequest(
                DEFAULT_PROJECT_ID,
                DEFAULT_USER_ID,
                DEFAULT_MODULE_ID,
                DEFAULT_METADATA,
                GUID1
            )
            every { view.parseRequest() } returns appRequest

            presenter.setup()

            coVerify { eventRepositoryMock.addOrUpdateEvent(any<VerificationCalloutEvent>()) }
        }
    }

    @Test
    fun presenter_setup_shouldAddInfoToSessionThenCallOrchestrator() {
        runTest(UnconfinedTestDispatcher()) {
            presenter.handleSignedInUser()

            coVerify(exactly = 2) { eventRepositoryMock.addOrUpdateEvent(any()) }
            verify(exactly = 1) { view.openOrchestratorActivity(any()) }
        }
    }

    @Test
    fun presenter_signedIn_shouldUpdateUserId() {
        runTest(UnconfinedTestDispatcher()) {

            presenter.handleSignedInUser()

            coVerify { loginManagerMock setProperty "signedInUserId" value DEFAULT_USER_ID }
        }
    }

    @Test
    fun presenter_signedIn_updateCurrentSession() {
        runBlocking {
            val subjectCount = 3
            val projectId = DEFAULT_PROJECT_ID

            val session = createSessionCaptureEvent(projectId = GUID1)
            coEvery { eventRepositoryMock.getCurrentCaptureSessionEvent() } returns session
            coEvery { eventRepositoryMock.getEventsFromSession(any()) } returns emptyFlow()
            coEvery { subjectLocalDataSourceMock.count(any()) } returns subjectCount
            coEvery { loginManagerMock.getSignedInProjectIdOrEmpty() } returns projectId
            every { preferencesManagerMock.modalities } returns listOf(
                Modality.FINGER,
                Modality.FACE
            )

            presenter.handleSignedInUser()

            val appVersionNameArg = "appVersionName"
            val libSimprintsVersionNameArg = "libSimprintsVersionName"
            val languageArg = "language"
            val deviceArg = Device(
                VERSION.SDK_INT.toString(),
                Build.MANUFACTURER + "_" + Build.MODEL,
                GUID1
            )

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
                databaseInfoArg
            ).apply {
                payload.location = locationArg
                payload.endedAt = ENDED_AT
            }

            coVerify {
                eventRepositoryMock.addOrUpdateEvent(expected)
            }
        }
    }

    @Test
    fun presenter_signedIn_updateProjectIdInEventsInCurrentSession() {
        runTest(UnconfinedTestDispatcher()) {
            val session = createSessionCaptureEvent(projectId = GUID1)
            val callout = createEnrolmentCalloutEvent(projectId = GUID1)
            coEvery { eventRepositoryMock.getCurrentCaptureSessionEvent() } returns session
            coEvery { eventRepositoryMock.getEventsFromSession(any()) } returns flowOf(
                session,
                callout
            )
            coEvery { subjectLocalDataSourceMock.count(any()) } returns 2

            presenter.appRequest = AppVerifyRequest(
                DEFAULT_PROJECT_ID,
                DEFAULT_USER_ID,
                DEFAULT_MODULE_ID,
                DEFAULT_METADATA,
                GUID1
            )

            presenter.handleSignedInUser()

            coVerify {
                eventRepositoryMock.addOrUpdateEvent(createEnrolmentCalloutEvent(GUID1).apply {
                    labels = labels.copy(projectId = DEFAULT_PROJECT_ID)
                })
            }
        }
    }
}
