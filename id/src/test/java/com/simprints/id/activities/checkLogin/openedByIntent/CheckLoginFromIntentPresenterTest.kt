package com.simprints.id.activities.checkLogin.openedByIntent

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.id.alert.AlertType
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppVerifyRequest
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_DEVICE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_METADATA
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.createSessionCaptureEvent
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.projectsecuritystore.SecurityStateRepository
import com.simprints.infra.projectsecuritystore.securitystate.models.SecurityState
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.reflection.runPrivateParentSuspendFunction
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class CheckLoginFromIntentPresenterTest {

    //TODO: We will re-enable and fix these tests when it's a VM that doesn't need field injection.

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val authStoreMock = mockk<AuthStore>(relaxed = true)
    private var configManager: ConfigManager = mockk()


    private val dispatcher = testCoroutineRule.testCoroutineDispatcher

    private lateinit var presenter: CheckLoginFromIntentPresenter

    @MockK
    lateinit var view: CheckLoginFromIntentContract.View

    @MockK
    lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @MockK
    lateinit var eventRepositoryMock: EventRepository

    @MockK
    lateinit var simNetworkUtilsMock: SimNetworkUtils

    @MockK
    lateinit var recentUserActivityManager: RecentUserActivityManager

    @MockK
    lateinit var securityStateRepositoryMock: SecurityStateRepository

    private val generalConfiguration = mockk<GeneralConfiguration>()

    @Before
    fun setUp() {

        MockKAnnotations.init(this, relaxed = true)
        coEvery { enrolmentRecordManager.count(any()) } returns 0
        every { simNetworkUtilsMock.connectionsStates } returns emptyList()
        every { authStoreMock.signedInProjectId } returns DEFAULT_PROJECT_ID
        coEvery { configManager.getProjectConfiguration() } returns mockk(relaxed = true) {
            every { general } returns generalConfiguration
        }
        coEvery { configManager.getDeviceConfiguration() } returns mockk(relaxed = true)
        coEvery { eventRepositoryMock.getCurrentCaptureSessionEvent() } returns createSessionCaptureEvent()
        coEvery { eventRepositoryMock.getEventsFromSession(any()) } returns emptyList()

        presenter = CheckLoginFromIntentPresenter(
            view,
            DEFAULT_DEVICE_ID,
            recentUserActivityManager,
            eventRepositoryMock,
            enrolmentRecordManager,
            simNetworkUtilsMock,
            CoroutineScope(dispatcher),
            dispatcher,
        ).apply {
            appRequest = AppVerifyRequest(
                DEFAULT_PROJECT_ID,
                DEFAULT_USER_ID,
                DEFAULT_MODULE_ID,
                DEFAULT_METADATA,
                GUID1
            )
            securityStateRepository = securityStateRepositoryMock
        }
    }

    @Test
    fun presenter_onViewCreatedIsCalled_shouldParseAppRequest() {
        runTest(UnconfinedTestDispatcher()) {

            presenter.onViewCreated(true)

            coVerify(exactly = 1) { view.parseRequest() }
        }
    }

    @Test
    fun presenter_onViewCreatedIsCalled_shouldSetLastUserId() {
        runTest(UnconfinedTestDispatcher()) {
            val appRequest = AppEnrolRequest(
                DEFAULT_PROJECT_ID,
                DEFAULT_USER_ID,
                DEFAULT_MODULE_ID,
                DEFAULT_METADATA
            )
            val updateConfigFn = slot<suspend (RecentUserActivity) -> RecentUserActivity>()
            coEvery { recentUserActivityManager.updateRecentUserActivity(capture(updateConfigFn)) } returns mockk()
            every { view.parseRequest() } returns appRequest

            presenter.onViewCreated(false)

            val updatedActivity =
                updateConfigFn.captured(RecentUserActivity("", "", "", 0, 0, 0, 0))
            assertThat(updatedActivity.lastUserUsed).isEqualTo(DEFAULT_USER_ID)
        }
    }

    @Test
    fun presenter_handlePausedProject_shouldOpenAlertActivityForError() {
        runTest(UnconfinedTestDispatcher()) {

            presenter.onViewCreated(true)
            presenter.handlePausedProject()

            coVerify(exactly = 1) { view.openAlertActivityForError(AlertType.PROJECT_PAUSED) }
        }
    }

    @Test
    fun presenter_gets_security_status_from_remote() {
        runTest(UnconfinedTestDispatcher()) {

            presenter.onViewCreated(true)
            presenter.runPrivateParentSuspendFunction("getSecurityStatus") as SecurityState.Status

            coVerify(exactly = 1) { securityStateRepositoryMock.getSecurityStatusFromRemote() }
        }
    }

    @Test
    fun presenter_gets_security_status_from_local_when_remote_throws_exception() {
        runTest(UnconfinedTestDispatcher()) {
            coEvery { securityStateRepositoryMock.getSecurityStatusFromRemote() } throws Exception()

            presenter.onViewCreated(true)
            presenter.runPrivateParentSuspendFunction("getSecurityStatus") as SecurityState.Status

            coVerify(exactly = 1) { securityStateRepositoryMock.getSecurityStatusFromRemote() }
            coVerify(exactly = 1) { securityStateRepositoryMock.getSecurityStatusFromLocal() }
        }
    }
//
//    @Test
//    fun presenter_setup_shouldAddEnrolmentCallout() {
//        runTest(UnconfinedTestDispatcher()) {
//            val appRequest = AppEnrolRequest(
//                DEFAULT_PROJECT_ID,
//                DEFAULT_USER_ID,
//                DEFAULT_MODULE_ID,
//                DEFAULT_METADATA
//            )
//            every { view.parseRequest() } returns appRequest
//
//            presenter.setup()
//
//            coVerify { eventRepositoryMock.addOrUpdateEvent(any<EnrolmentCalloutEvent>()) }
//        }
//    }
//
//    @Test
//    fun presenter_setup_shouldAddEnrolLastBiomentricsCallout() {
//        runTest(UnconfinedTestDispatcher()) {
//            val appRequest = AppEnrolLastBiometricsRequest(
//                DEFAULT_PROJECT_ID,
//                DEFAULT_USER_ID,
//                DEFAULT_MODULE_ID,
//                DEFAULT_METADATA,
//                GUID1
//            )
//            every { view.parseRequest() } returns appRequest
//
//            presenter.setup()
//
//            coVerify(exactly = 1) { eventRepositoryMock.addOrUpdateEvent(any<EnrolmentLastBiometricsCalloutEvent>()) }
//        }
//    }
//
//    @Test
//    fun presenter_setup_shouldAddConfirmIdentityCallout() {
//        runTest(UnconfinedTestDispatcher()) {
//            val appRequest =Ō
//                AppConfirmIdentityRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, GUID1, GUID2)
//            every { view.parseRequest() } returns appRequest
//
//            presenter.setup()
//
//            coVerify { eventRepositoryMock.addOrUpdateEvent(any<ConfirmationCalloutEvent>()) }
//        }
//    }
//
//    @Test
//    fun presenter_setup_shouldAddIdentificationCallout() {
//        runTest(UnconfinedTestDispatcher()) {
//            val appRequest = AppIdentifyRequest(
//                DEFAULT_PROJECT_ID,
//                DEFAULT_USER_ID,
//                DEFAULT_MODULE_ID,
//                DEFAULT_METADATA
//            )
//            every { view.parseRequest() } returns appRequest
//
//            presenter.setup()
//
//            coVerify { eventRepositoryMock.addOrUpdateEvent(any<IdentificationCalloutEvent>()) }
//        }
//    }
//
//    @Test
//    fun presenter_setup_shouldShowConfirmationTextForAppConfirmIdentityRequest() = runTest(
//        UnconfinedTestDispatcher()
//    ) {
//        val appRequest = AppConfirmIdentityRequest(
//            DEFAULT_PROJECT_ID,
//            DEFAULT_USER_ID,
//            GUID1,
//            GUID2
//        )
//        every { view.parseRequest() } returns appRequest
//
//        presenter.setup()
//
//        // showConfirmationText should be called for AppConfirmIdentityRequest type
//        coVerify { view.showConfirmationText() }
//    }
//
//    @Test
//    fun presenter_setup_shouldNotShowConfirmationTextForAppIdentifyRequest() = runTest(
//        UnconfinedTestDispatcher()
//    ) {
//        val appRequest = AppIdentifyRequest(
//            DEFAULT_PROJECT_ID,
//            DEFAULT_USER_ID,
//            DEFAULT_MODULE_ID,
//            DEFAULT_METADATA
//        )
//        every { view.parseRequest() } returns appRequest
//        presenter.setup()
//        // showConfirmationText shouldn't be called for AppIdentifyRequest type
//        coVerify(exactly = 0) { view.showConfirmationText() }
//
//    }
//
//    @Test
//    fun presenter_setup_shouldAddVerificationCallout() {
//        runTest(UnconfinedTestDispatcher()) {
//            val appRequest = AppVerifyRequest(
//                DEFAULT_PROJECT_ID,
//                DEFAULT_USER_ID,
//                DEFAULT_MODULE_ID,
//                DEFAULT_METADATA,
//                GUID1
//            )
//            every { view.parseRequest() } returns appRequest
//
//            presenter.setup()
//
//            coVerify { eventRepositoryMock.addOrUpdateEvent(any<VerificationCalloutEvent>()) }
//        }
//    }
//
//    @Test
//    fun presenter_setup_shouldAddInfoToSessionThenCallOrchestrator() {
//        runTest(UnconfinedTestDispatcher()) {
//            presenter.handleSignedInUser()
//
//            coVerify(exactly = 2) { eventRepositoryMock.addOrUpdateEvent(any()) }
//            verify(exactly = 1) { view.openOrchestratorActivity(any()) }
//        }
//    }
//
//    @Test
//    fun presenter_signedIn_shouldUpdateUserId() {
//        runTest(UnconfinedTestDispatcher()) {
//
//            presenter.handleSignedInUser()
//
//            coVerify { loginManagerMock setProperty "signedInUserId" value DEFAULT_USER_ID }
//        }
//    }
//
//    @Test
//    fun presenter_signedIn_updateCurrentSession() {
//        runTest {
//            val subjectCount = 3
//            val projectId = DEFAULT_PROJECT_ID
//
//            val session = createSessionCaptureEvent(projectId = GUID1)
//            coEvery { eventRepositoryMock.getCurrentCaptureSessionEvent() } returns session
//            coEvery { eventRepositoryMock.getEventsFromSession(any()) } returns emptyFlow()
//            coEvery { enrolmentRecordManager.count(any()) } returns subjectCount
//            coEvery { loginManagerMock.signedInProjectId } returns projectId
//            every { generalConfiguration.modalities } returns listOf(
//                Modality.FINGERPRINT,
//                Modality.FACE
//            )
//
//            presenter.handleSignedInUser()
//
//            val appVersionNameArg = "appVersionName"
//            val libSimprintsVersionNameArg = "libSimprintsVersionName"
//            val languageArg = "language"
//            val deviceArg = Device(
//                VERSION.SDK_INT.toString(),
//                Build.MANUFACTURER + "_" + Build.MODEL,
//                GUID1
//            )
//
//            val databaseInfoArg = DatabaseInfo(2, subjectCount)
//            val locationArg = Location(0.0, 0.0)
//            val expected = SessionCaptureEvent(
//                GUID1,
//                DEFAULT_PROJECT_ID,
//                CREATED_AT,
//                listOf(Modality.FINGERPRINT, Modality.FACE),
//                appVersionNameArg,
//                libSimprintsVersionNameArg,
//                languageArg,
//                deviceArg,
//                databaseInfoArg
//            ).apply {
//                payload.location = locationArg
//                payload.endedAt = ENDED_AT
//            }
//
//            coVerify {
//                eventRepositoryMock.addOrUpdateEvent(expected)
//            }
//        }
//    }
//
//    @Test
//    fun presenter_signedIn_updateProjectIdInEventsInCurrentSession() {
//        runTest(UnconfinedTestDispatcher()) {
//            val session = createSessionCaptureEvent(projectId = GUID1)
//            val callout = createEnrolmentCalloutEvent(projectId = GUID1)
//            coEvery { eventRepositoryMock.getCurrentCaptureSessionEvent() } returns session
//            coEvery { eventRepositoryMock.getEventsFromSession(any()) } returns flowOf(
//                session,
//                callout
//            )
//            coEvery { enrolmentRecordManager.count(any()) } returns 2
//
//            presenter.appRequest = AppVerifyRequest(
//                DEFAULT_PROJECT_ID,
//                DEFAULT_USER_ID,
//                DEFAULT_MODULE_ID,
//                DEFAULT_METADATA,
//                GUID1
//            )
//
//            presenter.handleSignedInUser()
//
//            coVerify {
//                eventRepositoryMock.addOrUpdateEvent(createEnrolmentCalloutEvent(GUID1).apply {
//                    labels = labels.copy(projectId = DEFAULT_PROJECT_ID)
//                })
//            }
//        }
//    }
//
//    @Test
//    fun `isProjectIdStoredAndMatches should return false if signed in project id is empty`() {
//        every { loginManagerMock.signedInProjectId } returns ""
//        val match = presenter.isProjectIdStoredAndMatches()
//        assertThat(match).isFalse()
//    }
//
//    @Test
//    fun `isProjectIdStoredAndMatches should return true if signed in project id is not empty and match the request`() {
//        every { loginManagerMock.signedInProjectId } returns DEFAULT_PROJECT_ID
//        presenter.appRequest = AppVerifyRequest(
//            DEFAULT_PROJECT_ID,
//            DEFAULT_USER_ID,
//            DEFAULT_MODULE_ID,
//            DEFAULT_METADATA,
//            GUID1
//        )
//        val match = presenter.isProjectIdStoredAndMatches()
//        assertThat(match).isTrue()
//    }
//
//    @Test
//    fun `isProjectIdStoredAndMatches should return throw an exception if signed in project id doesn't match the request`() {
//        every { loginManagerMock.signedInProjectId } returns "another project"
//        presenter.appRequest = AppVerifyRequest(
//            DEFAULT_PROJECT_ID,
//            DEFAULT_USER_ID,
//            DEFAULT_MODULE_ID,
//            DEFAULT_METADATA,
//            GUID1
//        )
//        assertThrows<DifferentProjectIdSignedInException> { presenter.isProjectIdStoredAndMatches() }
//    }
//
//    @Test
//    fun `isUserIdStoredAndMatches should return true if the signed in user id is not empty`() {
//        every { loginManagerMock.getSignedInUserIdOrEmpty() } returns "user"
//
//        val match = presenter.isUserIdStoredAndMatches()
//        assertThat(match).isTrue()
//    }
//
//    @Test
//    fun `isUserIdStoredAndMatches should return false if the signed in user id is empty`() {
//        every { loginManagerMock.getSignedInUserIdOrEmpty() } returns ""
//
//        val match = presenter.isUserIdStoredAndMatches()
//        assertThat(match).isFalse()
//    }
}
