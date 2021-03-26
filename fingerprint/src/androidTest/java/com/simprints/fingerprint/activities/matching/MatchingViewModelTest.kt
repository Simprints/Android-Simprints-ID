package com.simprints.fingerprint.activities.matching

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.fingerprint.activities.matching.request.MatchingTaskRequest
import com.simprints.fingerprint.activities.matching.result.MatchingTaskResult
import com.simprints.fingerprint.commontesttools.generators.FingerprintGenerator
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.flow.Action
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.data.domain.fingerprint.FingerprintIdentity
import com.simprints.fingerprint.di.KoinInjector.acquireFingerprintKoinModules
import com.simprints.fingerprint.di.KoinInjector.releaseFingerprintKoinModules
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprintmatcher.FingerprintMatcher
import com.simprints.fingerprintmatcher.domain.MatchResult
import io.mockk.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import java.io.Serializable
import com.simprints.fingerprintmatcher.domain.FingerprintIdentity as MatcherFingerprintIdentity

@RunWith(AndroidJUnit4::class)
class MatchingViewModelTest : KoinTest {

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private val dbManagerMock: FingerprintDbManager = mockk(relaxed = true)
    private val crashReportManagerMock: FingerprintCrashReportManager = mockk(relaxed = true)
    private val masterFlowManager: MasterFlowManager = mockk(relaxed = true)
    private val mockMatcher: FingerprintMatcher = mockk()

    private fun mockSuccessfulMatcher() {
        coEvery { mockMatcher.match(any(), any(), any()) } coAnswers {
            val probe = this.firstArg<MatcherFingerprintIdentity>()
            this.secondArg<Flow<MatcherFingerprintIdentity>>()
                .map {
                    MatchResult(it.id, if (doFingerprintsMatch(probe, it)) SUCCESSFUL_MATCH_SCORE else NOT_MATCH_SCORE)
                }
        }
    }

    private fun mockMatcherError() {
        coEvery { mockMatcher.match(any(), any(), any()) } throws Exception("Oops! Match failed")
    }

    @Before
    fun setUp() {
        acquireFingerprintKoinModules()
        loadKoinModules(module(override = true) {
            factory { dbManagerMock }
            factory { crashReportManagerMock }
            factory { masterFlowManager }
            factory<FingerprintPreferencesManager> { mockk(relaxed = true) }
            factory<FingerprintSessionEventsManager> { mockk(relaxed = true) }
            factory { mockMatcher }
        })
    }

    @Test
    fun identifyRequest_startedAndAwaitedWithSuccessfulMatch_finishesWithProbeInMatchResult() {
        mockSuccessfulMatcher()
        every { masterFlowManager.getCurrentAction() } returns Action.IDENTIFY
        setupDbManagerLoadCandidates(candidatesWithProbe)

        val query = mockk<Serializable>()
        val matchingRequest = MatchingTaskRequest(probeFingerprintRecord.fingerprints, query)
        val viewModel = createViewModelAndStart(matchingRequest)
        val result = viewModel.result.test().awaitValue().value().data?.getParcelableExtra<MatchingTaskResult>(MatchingTaskResult.BUNDLE_KEY)

        assertNotNull(result)
        result?.let { matchingResult ->
            assertEquals(DEFAULT_NUMBER_OF_ID_RETURNS, matchingResult.results.size)
            val highestScoreCandidate = matchingResult.results.maxBy { it.confidence }?.guid
            assertThat(highestScoreCandidate).isEqualTo(probeFingerprintRecord.personId)
        }
        coVerify { dbManagerMock.loadPeople(query) }
    }

    @Test
    fun identifyRequest_startedAndAwaitedWithoutSuccessfulMatch_finishesWithoutProbeInMatchResult() {
        mockSuccessfulMatcher()
        every { masterFlowManager.getCurrentAction() } returns Action.IDENTIFY
        setupDbManagerLoadCandidates(candidatesWithoutProbe)

        val query = mockk<Serializable>()
        val matchingRequest = MatchingTaskRequest(probeFingerprintRecord.fingerprints, query)
        val viewModel = createViewModelAndStart(matchingRequest)
        val result = viewModel.result.test().awaitValue().value().data?.getParcelableExtra<MatchingTaskResult>(MatchingTaskResult.BUNDLE_KEY)

        assertNotNull(result)
        result?.let { matchingResult ->
            assertThat(matchingResult.results).doesNotContain(probeFingerprintRecord.personId)
        }
        coVerify { dbManagerMock.loadPeople(query) }
    }

    @Test
    fun verifyRequest_startedAndAwaited_finishesWithCorrectResult() {
        mockSuccessfulMatcher()
        every { masterFlowManager.getCurrentAction() } returns Action.VERIFY
        setupDbManagerLoadCandidates(listOf(probeFingerprintRecord))

        val viewModel = createViewModelAndStart(verifyRequest)
        val result = viewModel.result.test().awaitValue().value()

        with(result) {
            assertEquals(ResultCode.OK, resultCode)
            assertNotNull(
                data?.getParcelableExtra<MatchingTaskResult>(MatchingTaskResult.BUNDLE_KEY)?.let {
                    assertEquals(probeFingerprintRecord.personId, it.results.first().guid)
                }
            )
        }
    }

    @Test
    fun identifyRequest_startedAndAwaited_updatesViewCorrectly() {
        mockSuccessfulMatcher()
        every { masterFlowManager.getCurrentAction() } returns Action.IDENTIFY
        setupDbManagerLoadCandidates(candidatesWithProbe)

        with(createViewModel()) {
            val progressTestObserver = progress.test()
            start(identifyRequest)
            result.test().awaitValue().assertValue { it.finishDelayMillis == IdentificationTask.matchingEndWaitTimeInMillis }
            hasLoadingBegun.test().assertValue { it }
            matchBeginningSummary.test().assertValue { it.matchSize == CANDIDATE_POOL + 1 }
            assertThat(progressTestObserver.valueHistory())
                .containsExactlyElementsIn((0..0) + (25..25) + (50..50) + (100..100))
                .inOrder()
            matchFinishedSummary.test().assertValue { it.returnSize == DEFAULT_NUMBER_OF_ID_RETURNS }
        }
    }

    @Test
    fun verifyRequest_startedAndAwaited_updatesViewCorrectly() {
        mockSuccessfulMatcher()
        every { masterFlowManager.getCurrentAction() } returns Action.VERIFY
        setupDbManagerLoadCandidates(listOf(probeFingerprintRecord))

        with(createViewModel()) {
            val progressTestObserver = progress.test()
            start(verifyRequest)
            result.test().awaitValue()
            assertThat(progressTestObserver.valueHistory())
                .containsExactlyElementsIn(arrayOf(0, 100))
                .inOrder()
        }
    }

    @Test
    fun identifyRequest_matchFailsToComplete_showsToastAndLogsError() {
        mockMatcherError()
        every { masterFlowManager.getCurrentAction() } returns Action.IDENTIFY
        setupDbManagerLoadCandidates(candidatesWithProbe)

        val viewModel = createViewModelAndStart(identifyRequest)

        with(viewModel) {
            result.test().awaitValue()
            hasMatchFailed.test().assertValue { it }
            verify { crashReportManagerMock.logExceptionOrSafeException(any()) }
        }
    }

    @Test
    fun verifyRequest_matchFailsToComplete_showsToastAndLogsError() {
        mockMatcherError()
        every { masterFlowManager.getCurrentAction() } returns Action.VERIFY
        setupDbManagerLoadCandidates(listOf(probeFingerprintRecord))

        val viewModel = createViewModelAndStart(verifyRequest)

        with(viewModel) {
            result.test().awaitValue()
            hasMatchFailed.test().assertValue { it }
            verify { crashReportManagerMock.logExceptionOrSafeException(any()) }
        }
    }

    private fun createViewModelAndStart(request: MatchingTaskRequest) =
        createViewModel().apply { start(request) }

    private fun createViewModel() = get<MatchingViewModel>()

    private fun setupDbManagerLoadCandidates(candidates: List<FingerprintIdentity>) {
        coEvery { dbManagerMock.loadPeople(any()) } returns candidates.asFlow()
    }

    private fun doFingerprintsMatch(probe: MatcherFingerprintIdentity, candidate: MatcherFingerprintIdentity) =
        probe.fingerprints.map { it.template }
            .zip(candidate.fingerprints.map {it.template})
            .all { (first, second) -> first.zip(second).all { (a, b) -> a == b } }

    @After
    fun tearDown() {
        releaseFingerprintKoinModules()
    }

    companion object {
        private const val NOT_MATCH_SCORE = 0f
        private const val SUCCESSFUL_MATCH_SCORE = 1f

        private const val DEFAULT_NUMBER_OF_ID_RETURNS = 10
        private const val CANDIDATE_POOL = 49

        private val probeFingerprintRecord = FingerprintGenerator.generateRandomFingerprintRecord()
        private val candidatesWithoutProbe = FingerprintGenerator.generateRandomFingerprintRecords(CANDIDATE_POOL)
        private val candidatesWithProbe = candidatesWithoutProbe + probeFingerprintRecord

        private val identifyRequest = MatchingTaskRequest(
            probeFingerprintRecord.fingerprints,
            mockk())

        private val verifyRequest = MatchingTaskRequest(
            probeFingerprintRecord.fingerprints,
            mockk())
    }
}
