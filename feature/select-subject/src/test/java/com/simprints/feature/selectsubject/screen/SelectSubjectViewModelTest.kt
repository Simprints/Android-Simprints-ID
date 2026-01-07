package com.simprints.feature.selectsubject.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
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
        assertThat(result?.savedCredential).isNull()
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
        assertThat(result?.savedCredential).isNull()
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
        assertThat(result?.savedCredential).isNull()
    }

    @Test
    fun `displays credential dialog when credential is scanned and not already linked`() = runTest {
        val scannedCredential = mockk<ScannedCredential>(relaxed = true)
        val displayedCredential = mockk<TokenizableString.Raw>(relaxed = true)
        setupCredentialState(displayedCredential, repositoryResponse = emptyList())

        val viewModel = createViewModel(params = selectSubjectParams.copy(scannedCredential = scannedCredential))

        val state = viewModel.stateLiveData.test().value()
        assertThat(state).isInstanceOf(SelectSubjectState.CredentialDialogDisplayed::class.java)
        val dialogState = state as SelectSubjectState.CredentialDialogDisplayed
        assertThat(dialogState.scannedCredential).isEqualTo(scannedCredential)
        assertThat(dialogState.displayedCredential).isEqualTo(displayedCredential)
    }

    @Test
    fun `displays credential dialog when credential is scanned and linked to different subject`() = runTest {
        val scannedCredential = mockk<ScannedCredential>(relaxed = true)
        val displayedCredential = mockk<TokenizableString.Raw>(relaxed = true)
        val repositoryResponse = listOf<EnrolmentRecord>(mockk { every { subjectId } returns "not_this_subject_id" })
        setupCredentialState(displayedCredential, repositoryResponse = repositoryResponse)

        val viewModel = createViewModel(params = selectSubjectParams.copy(scannedCredential = scannedCredential))

        val state = viewModel.stateLiveData.test().value()
        assertThat(state).isInstanceOf(SelectSubjectState.CredentialDialogDisplayed::class.java)
        val dialogState = state as SelectSubjectState.CredentialDialogDisplayed
        assertThat(dialogState.scannedCredential).isEqualTo(scannedCredential)
        assertThat(dialogState.displayedCredential).isEqualTo(displayedCredential)
    }

    @Test
    fun `does not display credential dialog when credential is already linked to same subject`() = runTest {
        val tokenizedValue = "tokenizedValue".asTokenizableEncrypted()
        val type = ExternalCredentialType.NHISCard
        val scannedCredential = mockk<ScannedCredential> {
            every { credentialScanId } returns "credentialId"
            every { credential } returns tokenizedValue
            every { credentialType } returns type
        }
        val displayedCredential = mockk<TokenizableString.Raw>(relaxed = true)
        val repositoryResponse = listOf<EnrolmentRecord>(mockk { every { subjectId } returns SUBJECT_ID })
        setupCredentialState(displayedCredential, repositoryResponse = repositoryResponse)

        val viewModel = createViewModel(params = selectSubjectParams.copy(scannedCredential = scannedCredential))

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isTrue()
        assertThat(result?.savedCredential?.value).isEqualTo(tokenizedValue)
        assertThat(result?.savedCredential?.type).isEqualTo(type)
    }

    @Test
    fun `does not display credential dialog when subject ID is none_selected`() = runTest {
        val tokenizedValue = "tokenizedValue".asTokenizableEncrypted()
        val type = ExternalCredentialType.NHISCard
        val scannedCredential = mockk<ScannedCredential> {
            every { credentialScanId } returns "credentialId"
            every { credential } returns tokenizedValue
            every { credentialType } returns type
        }
        val displayedCredential = mockk<TokenizableString.Raw>(relaxed = true)
        val repositoryResponse = listOf<EnrolmentRecord>(mockk { every { subjectId } returns SUBJECT_ID })
        setupCredentialState(displayedCredential, repositoryResponse = repositoryResponse)

        val viewModel = createViewModel(
            params = selectSubjectParams.copy(
                subjectId = "none_selected",
                scannedCredential = scannedCredential,
            ),
        )

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isTrue()
    }

    @Test
    fun `does not display credential dialog when project not availalbe`() = runTest {
        val scannedCredential = mockk<ScannedCredential>(relaxed = true)
        val displayedCredential = mockk<TokenizableString.Raw>(relaxed = true)
        val repositoryResponse = listOf<EnrolmentRecord>(mockk { every { subjectId } returns "not_this_subject_id" })
        setupCredentialState(
            displayedCredential,
            repositoryResponse = repositoryResponse,
            configuredProject = null,
        )

        val viewModel = createViewModel(
            params = selectSubjectParams.copy(
                scannedCredential = scannedCredential,
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
        assertThat(result?.savedCredential).isNull()
    }

    private fun setupCredentialState(
        displayedCredential: TokenizableString.Raw,
        repositoryResponse: List<EnrolmentRecord>,
        configuredProject: Project? = project,
    ) {
        coEvery { authStore.isProjectIdSignedIn(PROJECT_ID) } returns true
        coEvery { authStore.signedInProjectId } returns PROJECT_ID
        every { project?.id } returns PROJECT_ID
        coEvery { configRepository.getProject() } returns configuredProject
        coEvery { enrolmentRecordRepository.load(any()) } returns repositoryResponse
        coEvery {
            tokenizationProcessor.decrypt(
                encrypted = any(),
                tokenKeyType = TokenKeyType.ExternalCredential,
                project = any(),
            )
        } returns displayedCredential
    }

    @Test
    fun `saveCredential successfully saves credential`() = runTest {
        val tokenizedValue = "tokenizedValue".asTokenizableEncrypted()
        val type = ExternalCredentialType.NHISCard
        val scannedCredential = mockk<ScannedCredential> {
            every { credentialScanId } returns "credentialId"
            every { credential } returns tokenizedValue
            every { credentialType } returns type
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

        val viewModel = createViewModel(params = selectSubjectParams.copy(scannedCredential = scannedCredential))
        viewModel.saveCredential(scannedCredential)

        val state = viewModel.stateLiveData.test().value()
        assertThat(state).isEqualTo(SelectSubjectState.SavingExternalCredential)

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isTrue()
        assertThat(result?.savedCredential?.value).isEqualTo(tokenizedValue)
        assertThat(result?.savedCredential?.type).isEqualTo(type)

        coVerify {
            resetScannedCredentialsInSession(
                scannedCredential = scannedCredential,
                subjectId = SUBJECT_ID,
            )
        }
        coVerify { eventRepository.addOrUpdateEvent(match { it is EnrolmentUpdateEvent }) }
    }

    @Test
    fun `saveCredential does not saves update event if invalid subject id`() = runTest {
        val tokenizedValue = "tokenizedValue".asTokenizableEncrypted()
        val type = ExternalCredentialType.NHISCard
        val scannedCredential = mockk<ScannedCredential> {
            every { credentialScanId } returns "credentialId"
            every { credential } returns tokenizedValue
            every { credentialType } returns type
        }

        coJustRun {
            resetScannedCredentialsInSession(any(), any())
        }

        val viewModel = createViewModel(
            params = selectSubjectParams.copy(subjectId = "none_selected", scannedCredential = scannedCredential),
        )
        viewModel.saveCredential(scannedCredential)

        val state = viewModel.stateLiveData.test().value()
        assertThat(state).isEqualTo(SelectSubjectState.SavingExternalCredential)

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isTrue()
        assertThat(result?.savedCredential?.value).isEqualTo(tokenizedValue)
        assertThat(result?.savedCredential?.type).isEqualTo(type)

        coVerify {
            // Still needs to remove previous links
            resetScannedCredentialsInSession(
                scannedCredential = scannedCredential,
                subjectId = "none_selected",
            )
        }
        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any()) }
    }

    @Test
    fun `saveCredential handles exception when saving fails`() = runTest {
        val scannedCredential = mockk<ScannedCredential>(relaxed = true)
        coEvery {
            resetScannedCredentialsInSession(
                scannedCredential = scannedCredential,
                subjectId = SUBJECT_ID,
            )
        } throws RuntimeException("RuntimeException")

        viewModel.saveCredential(scannedCredential)

        val state = viewModel.stateLiveData.test().value()
        assertThat(state).isEqualTo(SelectSubjectState.SavingExternalCredential)

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isTrue()
        assertThat(result?.savedCredential).isNull()
    }

    @Test
    fun `finishWithoutSavingCredential finishes with no credential`() = runTest {
        viewModel.finishWithoutSavingCredential()

        val result = viewModel.finish
            .test()
            .value()
            .getContentIfNotHandled()
        assertThat(result?.isSubjectIdSaved).isTrue()
        assertThat(result?.savedCredential).isNull()
    }

    companion object {
        private val TIMESTAMP = Timestamp(1L)
        private const val PROJECT_ID = "projectId"
        private const val SUBJECT_ID = "302cc3c8-72cb-4525-95c5-ac5abfab43a9"
    }
}
