package com.simprints.feature.selectsubject.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.screens.search.model.MfidDocument
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult
import com.simprints.feature.externalcredential.usecase.ResetExternalCredentialsInSessionUseCase
import com.simprints.feature.selectsubject.SelectSubjectParams
import com.simprints.feature.selectsubject.model.SelectSubjectState
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.infra.events.event.domain.models.EnrolmentUpdateEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialSelectionEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class SelectSubjectViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var eventRepository: SessionEventRepository

    @MockK
    lateinit var configRepository: ConfigRepository

    @MockK
    lateinit var project: Project

    @MockK
    lateinit var resetScannedCredentialsInSession: ResetExternalCredentialsInSessionUseCase

    @MockK
    lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    lateinit var tokenizationProcessor: TokenizationProcessor

    lateinit var selectSubjectParams: SelectSubjectParams

    private lateinit var viewModel: SelectSubjectViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns TIMESTAMP
        selectSubjectParams = SelectSubjectParams(PROJECT_ID, SUBJECT_ID, null)

        viewModel = SelectSubjectViewModel(
            params = selectSubjectParams,
            timeHelper = timeHelper,
            authStore = authStore,
            eventRepository = eventRepository,
            configRepository = configRepository,
            resetExternalCredentialsUseCase = resetScannedCredentialsInSession,
            enrolmentRecordRepository = enrolmentRecordRepository,
            tokenizationProcessor = tokenizationProcessor,
            sessionCoroutineScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    private fun createViewModel(params: SelectSubjectParams = selectSubjectParams) = SelectSubjectViewModel(
        params = params,
        timeHelper = timeHelper,
        authStore = authStore,
        eventRepository = eventRepository,
        configRepository = configRepository,
        resetExternalCredentialsUseCase = resetScannedCredentialsInSession,
        enrolmentRecordRepository = enrolmentRecordRepository,
        tokenizationProcessor = tokenizationProcessor,
        sessionCoroutineScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
    )

    @Test
    fun `saves selection if signed in`() = runTest {
        coEvery { authStore.isProjectIdSignedIn(PROJECT_ID) } returns true
        val viewModel = createViewModel()

        coVerify { eventRepository.addOrUpdateEvent(any()) }
        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isTrue()
        assertThat(result?.credentialSearchResult).isNull()
    }

    @Test
    fun `does not save selection if not signed in`() = runTest {
        coEvery { authStore.isProjectIdSignedIn(PROJECT_ID) } returns false

        val viewModel = createViewModel()

        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any()) }
        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isFalse()
        assertThat(result?.credentialSearchResult).isNull()
    }

    @Test
    fun `correctly handles exception with saving`() = runTest {
        coEvery { authStore.isProjectIdSignedIn(PROJECT_ID) } returns true
        coEvery { eventRepository.addOrUpdateEvent(any()) } throws RuntimeException("RuntimeException")

        val viewModel = createViewModel()

        coVerify { eventRepository.addOrUpdateEvent(any()) }
        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isFalse()
        assertThat(result?.credentialSearchResult).isNull()
    }

    @Test
    fun `displays credential dialog when credential is scanned and not already linked`() = runTest {
        val confirmedCredential = mockk<TokenizableString.Raw>(relaxed = true)
        val scannedCredentialResult = mockk<ScannedCredentialResult>(relaxed = true)
        val credentialSearchResult = mockk<ExternalCredentialSearchResult.Complete>(relaxed = true) {
            every { this@mockk.scannedCredentialResult } returns scannedCredentialResult
            every { this@mockk.confirmedCredential } returns confirmedCredential
        }
        setupCredentialState(confirmedCredential, repositoryResponse = emptyList())

        val viewModel = createViewModel(params = selectSubjectParams.copy(credentialSearchResult = credentialSearchResult))

        val state = viewModel.stateLiveData.test().value()
        assertThat(state).isInstanceOf(SelectSubjectState.CredentialDialogDisplayed::class.java)
        val dialogState = state as SelectSubjectState.CredentialDialogDisplayed
        assertThat(dialogState.scannedCredentialResult).isEqualTo(scannedCredentialResult)
        assertThat(dialogState.displayedCredential).isEqualTo(confirmedCredential)
    }

    @Test
    fun `displays credential dialog when credential is scanned and linked to different subject`() = runTest {
        val confirmedCredential = mockk<TokenizableString.Raw>(relaxed = true)
        val scannedCredentialResult = mockk<ScannedCredentialResult>(relaxed = true)
        val credentialSearchResult = mockk<ExternalCredentialSearchResult.Complete>(relaxed = true) {
            every { this@mockk.scannedCredentialResult } returns scannedCredentialResult
            every { this@mockk.confirmedCredential } returns confirmedCredential
        }
        val repositoryResponse = listOf<EnrolmentRecord>(mockk { every { subjectId } returns "not_this_subject_id" })
        setupCredentialState(confirmedCredential, repositoryResponse = repositoryResponse)

        val viewModel = createViewModel(params = selectSubjectParams.copy(credentialSearchResult = credentialSearchResult))

        val state = viewModel.stateLiveData.test().value()
        assertThat(state).isInstanceOf(SelectSubjectState.CredentialDialogDisplayed::class.java)
        val dialogState = state as SelectSubjectState.CredentialDialogDisplayed
        assertThat(dialogState.scannedCredentialResult).isEqualTo(scannedCredentialResult)
        assertThat(dialogState.displayedCredential).isEqualTo(confirmedCredential)
    }

    @Test
    fun `does not display credential dialog when credential is already linked to same subject`() = runTest {
        val confirmedCredential = "12345678".asTokenizableRaw()
        val scannedCredentialResult = ScannedCredentialResult(
            credentialScanId = "credentialId",
            document = MfidDocument.GhanaNhisCard(
                credential = confirmedCredential,
            ),
            documentImagePath = null,
            zoomedCredentialImagePath = null,
            credentialBoundingBox = null,
            scanStartTime = Timestamp(0L),
            scanEndTime = Timestamp(1L),
        )
        val credentialSearchResult = mockk<ExternalCredentialSearchResult.Complete>(relaxed = true) {
            every { this@mockk.scannedCredentialResult } returns scannedCredentialResult
            every { this@mockk.confirmedCredential } returns confirmedCredential
        }
        val repositoryResponse = listOf<EnrolmentRecord>(mockk { every { subjectId } returns SUBJECT_ID })
        setupCredentialState(confirmedCredential, repositoryResponse = repositoryResponse)

        val viewModel = createViewModel(params = selectSubjectParams.copy(credentialSearchResult = credentialSearchResult))

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isTrue()
        assertThat(result?.credentialSearchResult).isEqualTo(credentialSearchResult)
    }

    @Test
    fun `does not display credential dialog when subject ID is none_selected`() = runTest {
        val confirmedCredential = "12345678".asTokenizableRaw()
        val scannedCredentialResult = mockk<ScannedCredentialResult>(relaxed = true)
        val credentialSearchResult = mockk<ExternalCredentialSearchResult.Complete>(relaxed = true) {
            every { this@mockk.scannedCredentialResult } returns scannedCredentialResult
            every { this@mockk.confirmedCredential } returns confirmedCredential
        }
        val repositoryResponse = listOf<EnrolmentRecord>(mockk { every { subjectId } returns SUBJECT_ID })
        setupCredentialState(confirmedCredential, repositoryResponse = repositoryResponse)

        val viewModel = createViewModel(
            params = selectSubjectParams.copy(
                subjectId = "none_selected",
                credentialSearchResult = credentialSearchResult,
            ),
        )

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isTrue()
    }

    @Test
    fun `does not display credential dialog when project not available`() = runTest {
        val confirmedCredential = mockk<TokenizableString.Raw>(relaxed = true)
        val scannedCredentialResult = mockk<ScannedCredentialResult>(relaxed = true)
        val credentialSearchResult = mockk<ExternalCredentialSearchResult.Complete>(relaxed = true) {
            every { this@mockk.scannedCredentialResult } returns scannedCredentialResult
            every { this@mockk.confirmedCredential } returns confirmedCredential
        }
        val repositoryResponse = listOf<EnrolmentRecord>(mockk { every { subjectId } returns "not_this_subject_id" })
        setupCredentialState(
            confirmedCredential,
            repositoryResponse = repositoryResponse,
            configuredProject = null,
        )

        val viewModel = createViewModel(
            params = selectSubjectParams.copy(
                credentialSearchResult = credentialSearchResult,
            ),
        )

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isTrue()
    }

    @Test
    fun `finishes without credential when no credential is scanned`() = runTest {
        coEvery { authStore.isProjectIdSignedIn(PROJECT_ID) } returns true

        val viewModel = createViewModel()

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isTrue()
        assertThat(result?.credentialSearchResult).isNull()
    }

    private fun setupCredentialState(
        confirmedCredential: TokenizableString.Raw,
        repositoryResponse: List<EnrolmentRecord>,
        configuredProject: Project? = project,
    ) {
        coEvery { authStore.isProjectIdSignedIn(PROJECT_ID) } returns true
        coEvery { authStore.signedInProjectId } returns PROJECT_ID
        every { project?.id } returns PROJECT_ID
        coEvery { configRepository.getProject() } returns configuredProject
        coEvery { enrolmentRecordRepository.load(any()) } returns repositoryResponse
        coEvery {
            tokenizationProcessor.encrypt(
                decrypted = confirmedCredential,
                tokenKeyType = TokenKeyType.ExternalCredential,
                project = any(),
            )
        } returns "encrypted_credential".asTokenizableEncrypted()
    }

    @Test
    fun `saveCredential successfully saves credential`() = runTest {
        val confirmedCredential = "12345678".asTokenizableRaw()
        val scannedCredentialResult = mockk<ScannedCredentialResult>(relaxed = true)
        val credentialSearchResult = mockk<ExternalCredentialSearchResult.Complete>(relaxed = true) {
            every { this@mockk.scannedCredentialResult } returns scannedCredentialResult
            every { this@mockk.confirmedCredential } returns confirmedCredential
        }
        coEvery {
            eventRepository.getEventsInCurrentSession()
        } returns listOf(
            mockk<ExternalCredentialCaptureEvent>(),
            mockk<EnrolmentUpdateEvent>(),
            mockk<ExternalCredentialSelectionEvent>(),
        )

        coJustRun {
            resetScannedCredentialsInSession(any(), any())
        }

        val viewModel = createViewModel(params = selectSubjectParams.copy(credentialSearchResult = credentialSearchResult))
        viewModel.saveCredential()

        val state = viewModel.stateLiveData.test().value()
        assertThat(state).isEqualTo(SelectSubjectState.SavingExternalCredential)

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isTrue()
        assertThat(result?.credentialSearchResult).isEqualTo(credentialSearchResult)

        coVerify {
            resetScannedCredentialsInSession(
                credentialSearchResult = credentialSearchResult,
                subjectId = SUBJECT_ID,
            )
        }
        coVerify { eventRepository.addOrUpdateEvent(match { it is EnrolmentUpdateEvent }) }
    }

    @Test
    fun `saveCredential does not save update event if invalid subject id`() = runTest {
        val confirmedCredential = "12345678".asTokenizableRaw()
        val scannedCredentialResult = mockk<ScannedCredentialResult>(relaxed = true)
        val credentialSearchResult = mockk<ExternalCredentialSearchResult.Complete>(relaxed = true) {
            every { this@mockk.scannedCredentialResult } returns scannedCredentialResult
            every { this@mockk.confirmedCredential } returns confirmedCredential
        }

        coJustRun {
            resetScannedCredentialsInSession(any(), any())
        }

        val viewModel = createViewModel(
            params = selectSubjectParams.copy(subjectId = "none_selected", credentialSearchResult = credentialSearchResult),
        )
        viewModel.saveCredential()

        val state = viewModel.stateLiveData.test().value()
        assertThat(state).isEqualTo(SelectSubjectState.SavingExternalCredential)

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isTrue()
        assertThat(result?.credentialSearchResult).isEqualTo(credentialSearchResult)

        coVerify {
            // Still needs to remove previous links
            resetScannedCredentialsInSession(
                credentialSearchResult = credentialSearchResult,
                subjectId = "none_selected",
            )
        }
        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any()) }
    }

    @Test
    fun `saveCredential handles exception when saving fails`() = runTest {
        coEvery {
            resetScannedCredentialsInSession(
                credentialSearchResult = any(),
                subjectId = SUBJECT_ID,
            )
        } throws RuntimeException("RuntimeException")

        viewModel.saveCredential()

        val state = viewModel.stateLiveData.test().value()
        assertThat(state).isEqualTo(SelectSubjectState.SavingExternalCredential)

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isTrue()
        assertThat(result?.credentialSearchResult).isNull()
    }

    @Test
    fun `finishWithoutSavingCredential finishes with no credential`() = runTest {
        viewModel.finishWithoutSavingCredential()

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isTrue()
        assertThat(result?.credentialSearchResult).isNull()
    }

    companion object {
        private val TIMESTAMP = Timestamp(1L)
        private const val PROJECT_ID = "projectId"
        private const val SUBJECT_ID = "302cc3c8-72cb-4525-95c5-ac5abfab43a9"
    }
}
