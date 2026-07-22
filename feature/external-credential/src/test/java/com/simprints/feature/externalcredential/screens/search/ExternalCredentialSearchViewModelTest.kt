package com.simprints.feature.externalcredential.screens.search

import android.text.InputType
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.CredentialMatch
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult
import com.simprints.feature.externalcredential.screens.search.model.SearchCredentialState
import com.simprints.feature.externalcredential.screens.search.model.SearchState
import com.simprints.feature.externalcredential.screens.search.usecase.MatchCandidatesUseCase
import com.simprints.feature.externalcredential.usecase.ExternalCredentialEventTrackerUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.simprints.infra.resources.R as IDR

internal class ExternalCredentialSearchViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var configRepository: ConfigRepository

    @MockK
    lateinit var matchCandidatesUseCase: MatchCandidatesUseCase

    @MockK
    lateinit var project: com.simprints.infra.config.store.models.Project

    @MockK
    lateinit var projectConfig: ProjectConfiguration

    @MockK
    lateinit var enrolmentRecord: EnrolmentRecord

    @MockK
    lateinit var candidateMatch: CredentialMatch

    @MockK
    lateinit var mockScannedCredentialResult: ScannedCredentialResult

    @MockK
    lateinit var externalCredentialParams: ExternalCredentialParams

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    lateinit var eventsTracker: ExternalCredentialEventTrackerUseCase

    private lateinit var viewModel: ExternalCredentialSearchViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { timeHelper.now() } returns Timestamp(1L)
        coEvery { configRepository.getProject() } returns project
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coJustRun { eventsTracker.saveSearchEvent(any(), any(), any()) }
        coJustRun { eventsTracker.saveConfirmation(any(), any()) }
        every { tokenizationProcessor.encrypt(any(), any(), any()) } returns mockk<TokenizableString.Tokenized>()
    }

    fun createViewModel() = ExternalCredentialSearchViewModel(
        scannedCredentialResult = mockScannedCredentialResult,
        externalCredentialParams = externalCredentialParams,
        timeHelper = timeHelper,
        configRepository = configRepository,
        matchCandidatesUseCase = matchCandidatesUseCase,
        tokenizationProcessor = tokenizationProcessor,
        enrolmentRecordRepository = enrolmentRecordRepository,
        eventsTracker = eventsTracker,
    )

    @Test
    fun `initial state handles missing project`() = runTest {
        clearMocks(configRepository)
        coEvery { configRepository.getProject() } returns null

        viewModel = createViewModel()

        coVerify(exactly = 0) { enrolmentRecordRepository.load(any()) }
    }

    @Test
    fun `initial state starts searching when credential not found`() = runTest {
        coEvery { enrolmentRecordRepository.load(any()) } returns emptyList()

        viewModel = createViewModel()
        val observer = viewModel.stateLiveData.test()

        assertThat(observer.value()?.searchState).isEqualTo(SearchState.CredentialNotFound)
        assertThat(observer.value()?.scannedCredentialResult).isEqualTo(mockScannedCredentialResult)
        assertThat(observer.value()?.isConfirmed).isFalse()
        assertThat(observer.value()?.displayedCredential).isEqualTo(mockScannedCredentialResult.credential)
        coVerify { eventsTracker.saveSearchEvent(any(), any(), any()) }
    }

    @Test
    fun `initial state searches and finds linked credential`() = runTest {
        coEvery { enrolmentRecordRepository.load(any()) } returns listOf(enrolmentRecord)
        coEvery {
            matchCandidatesUseCase(any(), any(), any(), any(), any())
        } returns listOf(candidateMatch)

        viewModel = createViewModel()

        val searchState = viewModel.stateLiveData.value?.searchState as SearchState.CredentialLinked
        assertThat(searchState.matchResults).hasSize(1)
        assertThat(searchState.matchResults.first()).isEqualTo(candidateMatch)
        coVerify { eventsTracker.saveSearchEvent(any(), any(), any()) }
    }

    @Test
    fun `updateConfirmation updates isConfirmed state`() = runTest {
        coEvery { enrolmentRecordRepository.load(any()) } returns emptyList()

        viewModel = createViewModel()
        val observer = viewModel.stateLiveData.test()

        viewModel.updateConfirmation(true)
        assertThat(observer.value()?.isConfirmed).isTrue()
        viewModel.updateConfirmation(false)
        assertThat(observer.value()?.isConfirmed).isFalse()
    }

    @Test
    fun `confirmCredentialUpdate handles missing project`() = runTest {
        clearMocks(configRepository) // reset default behaviour
        coEvery { configRepository.getProject() } returns null

        viewModel = createViewModel()
        viewModel.confirmCredentialUpdate("")

        coVerify(exactly = 0) { tokenizationProcessor.encrypt(any(), any(), any()) }
    }

    @Test
    fun `confirmCredentialUpdate normalizes credential and triggers new search and encrypts credential`() = runTest {
        val nonNormalizedCredential = " new Credential "
        val expectedCredential = "newCredential".asTokenizableRaw()
        val encryptedCredential = mockk<TokenizableString.Tokenized>()

        coEvery { enrolmentRecordRepository.load(any()) } returns emptyList()
        every { tokenizationProcessor.encrypt(expectedCredential, TokenKeyType.ExternalCredential, project) } returns encryptedCredential

        viewModel = createViewModel()

        viewModel.confirmCredentialUpdate(nonNormalizedCredential)

        coVerify { tokenizationProcessor.encrypt(expectedCredential, TokenKeyType.ExternalCredential, project) }
        coVerify { enrolmentRecordRepository.load(match { it.externalCredential == encryptedCredential }) }
        assertThat(viewModel.stateLiveData.value?.displayedCredential).isEqualTo(expectedCredential)
    }

    @Test
    fun `getButtonTextResource returns null when searching`() = runTest {
        val result = createViewModel().getButtonTextResource(SearchState.Searching, FlowType.IDENTIFY)
        assertThat(result).isNull()
    }

    @Test
    fun `getButtonTextResource returns 'enrol anyway' when credential linked and flow is ENROL`() = runTest {
        val searchState = SearchState.CredentialLinked(emptyList())
        val result = createViewModel().getButtonTextResource(searchState, FlowType.ENROL)
        assertThat(result).isEqualTo(IDR.string.mfid_action_enrol_anyway)
    }

    @Test
    fun `getButtonTextResource returns 'continue' when credential linked and not verified and flow is IDENTIFY`() = runTest {
        coEvery { enrolmentRecordRepository.load(any()) } returns emptyList()
        val searchState = SearchState.CredentialLinked(listOf(candidateMatch))
        every { candidateMatch.isVerificationSuccessful } returns false
        val result = createViewModel().getButtonTextResource(searchState, FlowType.IDENTIFY)
        assertThat(result).isEqualTo(IDR.string.mfid_action_continue)
    }

    @Test
    fun `getButtonTextResource returns 'enrol' when credential not found and flow is ENROL`() = runTest {
        val result = createViewModel().getButtonTextResource(SearchState.CredentialNotFound, FlowType.ENROL)
        assertThat(result).isEqualTo(IDR.string.mfid_action_enrol)
    }

    @Test
    fun `getKeyBoardInputType returns number for NHIS card`() = runTest {
        every { mockScannedCredentialResult.credentialType } returns ExternalCredentialType.NHISCard
        val result = createViewModel().getKeyBoardInputType()
        assertThat(result).isEqualTo(InputType.TYPE_CLASS_NUMBER)
    }

    @Test
    fun `getKeyBoardInputType returns text for Ghana ID card`() = runTest {
        every { mockScannedCredentialResult.credentialType } returns ExternalCredentialType.GhanaIdCard
        val result = createViewModel().getKeyBoardInputType()
        assertThat(result).isEqualTo(InputType.TYPE_CLASS_TEXT)
    }

    @Test
    fun `getKeyBoardInputType returns text for QR code`() = runTest {
        every { mockScannedCredentialResult.credentialType } returns ExternalCredentialType.QRCode
        val result = createViewModel().getKeyBoardInputType()
        assertThat(result).isEqualTo(InputType.TYPE_CLASS_TEXT)
    }

    @Test
    fun `finish sends empty matches when credential not found`() = runTest {
        viewModel = createViewModel()
        val state = mockk<SearchCredentialState> {
            every { scannedCredentialResult } returns mockScannedCredentialResult
            every { displayedCredential } returns mockk()
            every { searchState } returns SearchState.CredentialNotFound
        }
        viewModel.finish(state)
        val finishEvent = viewModel.finishEvent.value?.peekContent() as ExternalCredentialSearchResult.Complete
        assertThat(finishEvent).isNotNull()
        assertThat(finishEvent.matchResults).isEmpty()
        assertThat(finishEvent.scannedCredentialResult).isEqualTo(mockScannedCredentialResult)
    }

    @Test
    fun `finish sends empty matches when still searching`() = runTest {
        viewModel = createViewModel()
        val state = mockk<SearchCredentialState> {
            every { scannedCredentialResult } returns mockScannedCredentialResult
            every { displayedCredential } returns mockk()
            every { searchState } returns SearchState.Searching
        }
        viewModel.finish(state)
        val finishEvent = viewModel.finishEvent.value?.peekContent() as ExternalCredentialSearchResult.Complete
        assertThat(finishEvent).isNotNull()
        assertThat(finishEvent.matchResults).isEmpty()
    }

    @Test
    fun `finish sends match results when credential linked`() = runTest {
        val state = mockk<SearchCredentialState> {
            every { scannedCredentialResult } returns mockScannedCredentialResult
            every { displayedCredential } returns mockk()
            every { searchState } returns mockk<SearchState.CredentialLinked> {
                every { matchResults } returns listOf(candidateMatch)
            }
        }
        viewModel = createViewModel()
        viewModel.finish(state)
        val finishEvent = viewModel.finishEvent.value?.peekContent() as ExternalCredentialSearchResult.Complete
        assertThat(finishEvent).isNotNull()
        assertThat(finishEvent.matchResults).hasSize(1)
        assertThat(finishEvent.matchResults.first()).isEqualTo(candidateMatch)
        assertThat(finishEvent.scannedCredentialResult).isEqualTo(mockScannedCredentialResult)
    }

    @Test
    fun `trackRecapture sends confirmation event`() = runTest {
        createViewModel().trackRecapture()

        coVerify { eventsTracker.saveConfirmation(any(), any()) }
    }

    @Test
    fun `finish sends confirmation event`() = runTest {
        val state = mockk<SearchCredentialState> {
            every { scannedCredentialResult } returns mockScannedCredentialResult
            every { displayedCredential } returns mockk()
            every { searchState } returns mockk<SearchState.CredentialLinked> {
                every { matchResults } returns listOf(candidateMatch)
            }
        }
        createViewModel().finish(state)

        coVerify { eventsTracker.saveConfirmation(any(), any()) }
    }

    @Test
    fun `decryptCredentialToDisplay updates displayedCredential state`() = runTest {
        val credential = mockk<TokenizableString.Raw>()

        every { mockScannedCredentialResult.credential } returns credential
        coEvery { enrolmentRecordRepository.load(any()) } returns emptyList()

        viewModel = createViewModel()

        assertThat(viewModel.stateLiveData.value?.displayedCredential).isEqualTo(credential)
    }

    @Test
    fun `isCredentialFormatValid validates NHIS card format`() = runTest {
        every { mockScannedCredentialResult.credentialType } returns ExternalCredentialType.NHISCard

        viewModel = createViewModel()

        assertThat(viewModel.isCredentialFormatValid("12345678")).isTrue()
        assertThat(viewModel.isCredentialFormatValid("  1234  5678  ")).isTrue()
        assertThat(viewModel.isCredentialFormatValid("invalid")).isFalse()
        assertThat(viewModel.isCredentialFormatValid("1234567")).isFalse()
        assertThat(viewModel.isCredentialFormatValid(null)).isFalse()
    }

    @Test
    fun `isCredentialFormatValid validates Ghana ID card format`() = runTest {
        every { mockScannedCredentialResult.credentialType } returns ExternalCredentialType.GhanaIdCard

        viewModel = createViewModel()

        assertThat(viewModel.isCredentialFormatValid("GHA-123456789-0")).isTrue()
        assertThat(viewModel.isCredentialFormatValid("  GHA - 123456789-0 ")).isTrue()
        assertThat(viewModel.isCredentialFormatValid("invalid")).isFalse()
        assertThat(viewModel.isCredentialFormatValid(null)).isFalse()
    }

    @Test
    fun `isCredentialFormatValid always returns true for QR code`() = runTest {
        every { mockScannedCredentialResult.credentialType } returns ExternalCredentialType.QRCode

        viewModel = createViewModel()

        assertThat(viewModel.isCredentialFormatValid("any_value")).isTrue()
        assertThat(viewModel.isCredentialFormatValid(" any_value ")).isTrue()
        assertThat(viewModel.isCredentialFormatValid("")).isTrue()
        assertThat(viewModel.isCredentialFormatValid(null)).isFalse()
    }

    @Test
    fun `isCredentialFormatValid validates Fayda card format`() = runTest {
        every { mockScannedCredentialResult.credentialType } returns ExternalCredentialType.FaydaCard

        viewModel = createViewModel()

        assertThat(viewModel.isCredentialFormatValid("1234567812345678")).isTrue()
        assertThat(viewModel.isCredentialFormatValid(" 1234 5678 1234 5678 ")).isTrue()
        assertThat(viewModel.isCredentialFormatValid("1234-5678-1234-5678")).isFalse()
        assertThat(viewModel.isCredentialFormatValid("123456781234567")).isFalse()
        assertThat(viewModel.isCredentialFormatValid("invalid")).isFalse()
        assertThat(viewModel.isCredentialFormatValid(null)).isFalse()
    }
}
