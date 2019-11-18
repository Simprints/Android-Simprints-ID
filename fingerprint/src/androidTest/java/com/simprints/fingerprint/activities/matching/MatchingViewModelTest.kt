package com.simprints.fingerprint.activities.matching

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
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
import com.simprints.fingerprintmatcher.EVENT
import com.simprints.fingerprintmatcher.LibMatcher
import com.simprints.fingerprintmatcher.Progress
import com.simprints.fingerprintmatcher.sourceafis.MatcherEventListener
import com.simprints.testtools.common.syntax.*
import io.reactivex.Single
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import org.koin.test.mock.declareMock
import java.io.Serializable
import kotlin.random.Random
import com.simprints.fingerprintmatcher.Person as LibPerson

@RunWith(AndroidJUnit4::class)
class MatchingViewModelTest : KoinTest {

    @get:Rule val taskExecutorRule = InstantTaskExecutorRule()

    private val dbManagerMock: FingerprintDbManager = mock()
    private val crashReportManagerMock: FingerprintCrashReportManager = mock()
    private val masterFlowManager: MasterFlowManager = mock()

    private val mockIdentificationLibMatcher: (LibPerson, List<LibPerson>,
                                               LibMatcher.MATCHER_TYPE, MutableList<Float>, MatcherEventListener, Int) -> LibMatcher =
        { probe, candidates, _, scores, callback, _ ->
            setupMock {
                whenThis { start() } then {
                    IDENTIFY_PROGRESS_RANGE.forEach { n -> callback.onMatcherProgress(Progress(n)) }
                    scores.addAll(candidates.map { ((if (it.fingerprints == probe.fingerprints) SUCCESSFUL_MATCH_SCORE else NOT_MATCH_SCORE)) })
                    callback.onMatcherEvent(EVENT.MATCH_COMPLETED)
                }
            }
        }

    private val mockVerificationLibMatcher: (LibPerson, List<LibPerson>,
                                             LibMatcher.MATCHER_TYPE, MutableList<Float>, MatcherEventListener, Int) -> LibMatcher =
        { _, _, _, scores, callback, _ ->
            setupMock {
                whenThis { start() } then {
                    callback.onMatcherProgress(Progress(0))
                    scores.add(Random.nextFloat())
                    callback.onMatcherEvent(EVENT.MATCH_COMPLETED)
                }
            }
        }

    private val mockErrorLibMatcher: (LibPerson, List<LibPerson>,
                                      LibMatcher.MATCHER_TYPE, MutableList<Float>, MatcherEventListener, Int) -> LibMatcher =
        { _, _, _, _, callback, _ ->
            setupMock {
                whenThis { start() } then { callback.onMatcherEvent(EVENT.MATCH_NOT_RUNNING) }
            }
        }

    @Before
    fun setUp() {
        acquireFingerprintKoinModules()
        declare {
            factory { dbManagerMock }
            factory { crashReportManagerMock }
            factory { masterFlowManager }
        }
        declareMock<FingerprintPreferencesManager>()
        declareMock<FingerprintSessionEventsManager>()
    }

    @Test
    fun identificationRequest_startedAndAwaitedWithSuccessfulMatch_finishesWithProbeInMatchResult() {
        whenever(masterFlowManager) { getCurrentAction() } thenReturn Action.IDENTIFY
        setupDbManagerLoadCandidates(candidatesWithProbe)

        val query = mock<Serializable>()
        val matchingRequest = MatchingTaskRequest(probeFingerprintRecord.fingerprints, query)
        val viewModel = createViewModelAndStart(matchingRequest, mockIdentificationLibMatcher)
        val result = viewModel.result.test().awaitValue().value().data?.getParcelableExtra<MatchingTaskResult>(MatchingTaskResult.BUNDLE_KEY)

        assertNotNull(result)
        result?.let { matchingResult ->
            assertEquals(DEFAULT_NUMBER_OF_ID_RETURNS, matchingResult.results.size)
            val highestScoreCandidate = matchingResult.results.maxBy { it.confidence }?.guid
            assertThat(highestScoreCandidate).isEqualTo(probeFingerprintRecord.personId)
        }
        verifyOnce(dbManagerMock) { loadPeople(query) }
    }

    @Test
    fun identificationRequest_startedAndAwaitedWithoutSuccessfulMatch_finishesWithoutProbeInMatchResult() {
        whenever(masterFlowManager) { getCurrentAction() } thenReturn Action.IDENTIFY
        setupDbManagerLoadCandidates(candidatesWithoutProbe)

        val query = mock<Serializable>()
        val matchingRequest = MatchingTaskRequest(probeFingerprintRecord.fingerprints, query)
        val viewModel = createViewModelAndStart(matchingRequest, mockIdentificationLibMatcher)
        val result = viewModel.result.test().awaitValue().value().data?.getParcelableExtra<MatchingTaskResult>(MatchingTaskResult.BUNDLE_KEY)

        assertNotNull(result)
        result?.let { matchingResult ->
            assertThat(matchingResult.results).doesNotContain(probeFingerprintRecord.personId)
        }
        verifyOnce(dbManagerMock) { loadPeople(query) }
    }

    @Test
    fun verificationRequest_startedAndAwaited_finishesWithCorrectResult() {
        whenever(masterFlowManager) { getCurrentAction() } thenReturn Action.VERIFY
        setupDbManagerLoadCandidates(listOf(probeFingerprintRecord))

        val viewModel = createViewModelAndStart(verifyRequest, mockVerificationLibMatcher)
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
    fun identificationRequest_startedAndAwaited_updatesViewCorrectly() {
        whenever(masterFlowManager) { getCurrentAction() } thenReturn Action.IDENTIFY
        setupDbManagerLoadCandidates(candidatesWithProbe)

        with(createViewModel()) {
            val progressTestObserver = progress.test()
            start(identifyRequest, mockIdentificationLibMatcher)
            result.test().awaitValue().assertValue { it.finishDelayMillis == IdentificationTask.matchingEndWaitTimeInMillis }
            hasLoadingBegun.test().assertValue { it }
            matchBeginningSummary.test().assertValue { it.matchSize == CANDIDATE_POOL + 1 }
            assertThat(progressTestObserver.valueHistory())
                .containsExactlyElementsIn((0..0) + (25..25) + (50..50) + IDENTIFY_PROGRESS_RANGE.map { it / 2 + 50 } + (100..100))
                .inOrder()
            matchFinishedSummary.test().assertValue { it.returnSize == DEFAULT_NUMBER_OF_ID_RETURNS }
        }
    }

    @Test
    fun verificationRequest_startedAndAwaited_updatesViewCorrectly() {
        whenever(masterFlowManager) { getCurrentAction() } thenReturn Action.VERIFY
        setupDbManagerLoadCandidates(listOf(probeFingerprintRecord))

        with(createViewModel()) {
            val progressTestObserver = progress.test()
            start(verifyRequest, mockVerificationLibMatcher)
            result.test().awaitValue()
            assertThat(progressTestObserver.valueHistory())
                .containsExactlyElementsIn(arrayOf(0, 100))
                .inOrder()
        }
    }

    @Test
    fun identifyRequest_matchFailsToComplete_showsToastAndLogsError() {
        whenever(masterFlowManager) { getCurrentAction() } thenReturn Action.IDENTIFY
        setupDbManagerLoadCandidates(candidatesWithProbe)

        val viewModel = createViewModelAndStart(identifyRequest, mockErrorLibMatcher)

        with(viewModel) {
            result.test().awaitValue()
            hasMatchFailed.test().assertValue { it }
            verifyOnce(crashReportManagerMock) { logExceptionOrSafeException(anyNotNull()) }
        }
    }

    @Test
    fun verifyRequest_matchFailsToComplete_showsToastAndLogsError() {
        whenever(masterFlowManager) { getCurrentAction() } thenReturn Action.VERIFY
        setupDbManagerLoadCandidates(listOf(probeFingerprintRecord))

        val viewModel = createViewModelAndStart(verifyRequest, mockErrorLibMatcher)

        with(viewModel) {
            result.test().awaitValue()
            hasMatchFailed.test().assertValue { it }
            verifyOnce(crashReportManagerMock) { logExceptionOrSafeException(anyNotNull()) }
        }
    }

    private fun createViewModelAndStart(request: MatchingTaskRequest,
                                        mockLibMatcher: (LibPerson, List<LibPerson>, LibMatcher.MATCHER_TYPE, MutableList<Float>, MatcherEventListener, Int) -> LibMatcher) =
        createViewModel().apply { start(request, mockLibMatcher) }

    private fun createViewModel() = get<MatchingViewModel>()

    private fun setupDbManagerLoadCandidates(candidates: List<FingerprintIdentity>) {
        whenever(dbManagerMock) { loadPeople(anyNotNull()) } thenReturn Single.just(candidates)
    }

    @After
    fun tearDown() {
        releaseFingerprintKoinModules()
    }

    companion object {
        private const val NOT_MATCH_SCORE = 0f
        private const val SUCCESSFUL_MATCH_SCORE = 1f

        private const val DEFAULT_NUMBER_OF_ID_RETURNS = 10
        private const val CANDIDATE_POOL = 49
        private val IDENTIFY_PROGRESS_RANGE = 0..100

        private val probeFingerprintRecord = FingerprintGenerator.generateRandomFingerprintRecord()
        private val candidatesWithoutProbe = FingerprintGenerator.generateRandomFingerprintRecords(CANDIDATE_POOL)
        private val candidatesWithProbe = candidatesWithoutProbe + probeFingerprintRecord

        private val identifyRequest = MatchingTaskRequest(
            probeFingerprintRecord.fingerprints,
            mock())

        private val verifyRequest = MatchingTaskRequest(
            probeFingerprintRecord.fingerprints,
            mock())
    }
}
