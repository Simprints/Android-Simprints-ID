package com.simprints.fingerprint.activities.matching

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.fingerprint.activities.matching.request.MatchingTaskRequest
import com.simprints.fingerprint.activities.matching.result.MatchingTaskResult
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.FingerComparisonStrategy
import com.simprints.fingerprint.controllers.core.eventData.model.OneToOneMatchEvent
import com.simprints.fingerprint.controllers.core.flow.Action
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.fingerprint.FingerprintIdentity
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.testtools.FingerprintGenerator
import com.simprints.fingerprint.testtools.FullUnitTestConfigRule
import com.simprints.fingerprintmatcher.FingerprintMatcher
import com.simprints.fingerprintmatcher.domain.MatchResult
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.FingerprintConfiguration
import io.mockk.*
import kotlinx.coroutines.flow.asFlow
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
    var unitTestConfigRule = FullUnitTestConfigRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val dbManagerMock: FingerprintDbManager = mockk(relaxed = true)
    private val masterFlowManager: MasterFlowManager = mockk(relaxed = true)
    private val mockMatcher: FingerprintMatcher = mockk()
    private val fingerprintConfiguration = mockk<FingerprintConfiguration>(relaxed = true)
    private val mockConfigManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { fingerprint } returns fingerprintConfiguration
        }
    }
    private val mockFingerprintSessionEventsManager: FingerprintSessionEventsManager =
        mockk(relaxed = true)

    private fun mockSuccessfulMatcher() {
        every { mockMatcher.match(any(), any(), any(), any()) } coAnswers {
            val probe = this.firstArg<MatcherFingerprintIdentity>()
            this.secondArg<List<MatcherFingerprintIdentity>>()
                .map {
                    MatchResult(
                        it.id,
                        if (doFingerprintsMatch(
                                probe,
                                it
                            )
                        ) SUCCESSFUL_MATCH_SCORE else NOT_MATCH_SCORE
                    )
                }
        }
    }

    private fun mockMatcherError() {
        coEvery {
            mockMatcher.match(
                any(),
                any(),
                any(),
                any()
            )
        } throws Exception("Oops! Match failed")
    }

    private lateinit var viewModel: MatchingViewModel

    @Before
    fun setUp() {
        val mockModule = module {
            factory { mockk<FingerprintTimeHelper>(relaxed = true) }
            factory { dbManagerMock }
            factory { masterFlowManager }
            factory { mockConfigManager }
            factory { mockFingerprintSessionEventsManager }
            factory { mockMatcher }
        }
        loadKoinModules(mockModule)
        viewModel = get()
    }


    @Test
    fun identifyRequest_startedAndAwaitedWithSuccessfulMatch_finishesWithProbeInMatchResult() {
        mockSuccessfulMatcher()
        every { masterFlowManager.getCurrentAction() } returns Action.IDENTIFY
        setupDbManagerLoadCandidates(candidatesWithProbe)

        val query = mockk<Serializable>()
        val matchingRequest = MatchingTaskRequest(probeFingerprintRecord.fingerprints, query)
        viewModel.start(matchingRequest)
        val result = viewModel.result.test().awaitValue()
            .value().data?.getParcelableExtra<MatchingTaskResult>(MatchingTaskResult.BUNDLE_KEY)

        assertNotNull(result)
        result?.let { matchingResult ->
            assertEquals(DEFAULT_NUMBER_OF_ID_RETURNS, matchingResult.results.size)
            val highestScoreCandidate = matchingResult.results.maxByOrNull { it.confidence }?.guid
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
        viewModel.start(matchingRequest)
        val result = viewModel.result.test().awaitValue()
            .value().data?.getParcelableExtra<MatchingTaskResult>(MatchingTaskResult.BUNDLE_KEY)

        assertNotNull(result)
        result?.let { matchingResult ->
            assertThat(matchingResult.results).doesNotContain(probeFingerprintRecord.personId)
        }
        coVerify { dbManagerMock.loadPeople(query) }
    }

    @Test
    fun verifyRequest_startedSameFingerComparisonAndAwaited_finishesWithCorrectResult() {
        mockSuccessfulMatcher()
        every { fingerprintConfiguration.comparisonStrategyForVerification } returns FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER
        every { masterFlowManager.getCurrentAction() } returns Action.VERIFY
        setupDbManagerLoadCandidates(listOf(probeFingerprintRecord))

        viewModel.start(verifyRequest)
        val result = viewModel.result.test().awaitValue().value()

        val savedEvent = CapturingSlot<OneToOneMatchEvent>()
        verify { mockFingerprintSessionEventsManager.addEventInBackground(capture(savedEvent)) }
        assertThat(savedEvent.captured.fingerComparisonStrategy).isEqualTo(FingerComparisonStrategy.SAME_FINGER)

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
    fun verifyRequest_startedCrossFingerMatchAndAwaited_finishesWithCorrectResult() {
        mockSuccessfulMatcher()
        every { fingerprintConfiguration.comparisonStrategyForVerification } returns FingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX
        every { masterFlowManager.getCurrentAction() } returns Action.VERIFY
        setupDbManagerLoadCandidates(listOf(probeFingerprintRecord))

        viewModel.start(verifyRequest)
        val result = viewModel.result.test().awaitValue().value()

        val savedEvent = CapturingSlot<OneToOneMatchEvent>()
        verify { mockFingerprintSessionEventsManager.addEventInBackground(capture(savedEvent)) }
        assertThat(savedEvent.captured.fingerComparisonStrategy).isEqualTo(FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX)

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

        with(viewModel) {
            val progressTestObserver = progress.test()
            start(identifyRequest)
            result.test().awaitValue()
                .assertValue { it.finishDelayMillis == IdentificationTask.matchingEndWaitTimeInMillis }
            hasLoadingBegun.test().assertValue { it }
            matchBeginningSummary.test().assertValue { it.matchSize == CANDIDATE_POOL + 1 }
            assertThat(progressTestObserver.valueHistory())
                .containsExactlyElementsIn((0..0) + (25..25) + (50..50) + (100..100))
                .inOrder()
            matchFinishedSummary.test()
                .assertValue { it.returnSize == DEFAULT_NUMBER_OF_ID_RETURNS }
        }
    }

    @Test
    fun verifyRequest_startedAndAwaited_updatesViewCorrectly() {
        mockSuccessfulMatcher()
        every { masterFlowManager.getCurrentAction() } returns Action.VERIFY
        setupDbManagerLoadCandidates(listOf(probeFingerprintRecord))

        with(viewModel) {
            val progressTestObserver = progress.test()
            start(verifyRequest)
            result.test().awaitValue()
            assertThat(progressTestObserver.valueHistory())
                .containsExactlyElementsIn(arrayOf(0, 100))
                .inOrder()
        }
    }

    @Test
    fun identifyRequest_matchFailsToComplete_showsToast() {
        mockMatcherError()
        every { masterFlowManager.getCurrentAction() } returns Action.IDENTIFY
        setupDbManagerLoadCandidates(candidatesWithProbe)

        viewModel.start(identifyRequest)

        with(viewModel) {
            result.test().awaitValue()
            hasMatchFailed.test().assertValue { it }
        }
    }

    @Test
    fun verifyRequest_matchFailsToComplete_showsToast() {
        mockMatcherError()
        every { masterFlowManager.getCurrentAction() } returns Action.VERIFY
        setupDbManagerLoadCandidates(listOf(probeFingerprintRecord))

        viewModel.start(verifyRequest)

        with(viewModel) {
            result.test().awaitValue()
            hasMatchFailed.test().assertValue { it }
        }
    }


    private fun setupDbManagerLoadCandidates(candidates: List<FingerprintIdentity>) {
        coEvery { dbManagerMock.loadPeople(any()) } returns candidates.asFlow()
    }

    private fun doFingerprintsMatch(
        probe: MatcherFingerprintIdentity,
        candidate: MatcherFingerprintIdentity
    ) =
        probe.fingerprints.map { it.template }
            .zip(candidate.fingerprints.map { it.template })
            .all { (first, second) -> first.zip(second).all { (a, b) -> a == b } }


    companion object {
        private const val NOT_MATCH_SCORE = 0f
        private const val SUCCESSFUL_MATCH_SCORE = 1f

        private const val DEFAULT_NUMBER_OF_ID_RETURNS = 10
        private const val CANDIDATE_POOL = 49

        private val probeFingerprintRecord = FingerprintGenerator.generateRandomFingerprintRecord()
        private val candidatesWithoutProbe =
            FingerprintGenerator.generateRandomFingerprintRecords(CANDIDATE_POOL)
        private val candidatesWithProbe = candidatesWithoutProbe + probeFingerprintRecord

        private val identifyRequest = MatchingTaskRequest(
            probeFingerprintRecord.fingerprints,
            mockk()
        )

        private val verifyRequest = MatchingTaskRequest(
            probeFingerprintRecord.fingerprints,
            mockk()
        )
    }
}
