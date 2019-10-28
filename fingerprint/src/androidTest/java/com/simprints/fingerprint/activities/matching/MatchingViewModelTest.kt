package com.simprints.fingerprint.activities.matching

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.fingerprint.activities.matching.request.MatchingTaskIdentifyRequest.QueryForIdentifyPool
import com.simprints.fingerprint.activities.matching.request.MatchingTaskRequest
import com.simprints.fingerprint.activities.matching.result.MatchingTaskResult
import com.simprints.fingerprint.commontesttools.DEFAULT_MODULE_ID
import com.simprints.fingerprint.commontesttools.DEFAULT_PROJECT_ID
import com.simprints.fingerprint.commontesttools.DEFAULT_USER_ID
import com.simprints.fingerprint.commontesttools.generators.PeopleGeneratorUtils
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.data.domain.fingerprint.Person
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
import kotlin.random.Random
import com.simprints.fingerprintmatcher.Person as LibPerson

@RunWith(AndroidJUnit4::class)
@SmallTest
class MatchingViewModelTest : KoinTest {

    @get:Rule val taskExecutorRule = InstantTaskExecutorRule()

    private val dbManagerMock: FingerprintDbManager = mock()
    private val crashReportManagerMock: FingerprintCrashReportManager = mock()

    private val mockIdentificationLibMatcher: (LibPerson, List<LibPerson>,
                                               LibMatcher.MATCHER_TYPE, MutableList<Float>, MatcherEventListener, Int) -> LibMatcher =
        { probe, candidates, _, scores, callback, _ ->
            setupMock {
                whenThis { start() } then {
                    IDENTIFY_PROGRESS_RANGE.forEach { n -> callback.onMatcherProgress(Progress(n)) }
                    scores.addAll(candidates.map { ((if (it.guid == probe.guid) 1f else 0f)) })
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
        }
        declareMock<FingerprintPreferencesManager>()
        declareMock<FingerprintSessionEventsManager>()
    }

    @Test
    fun identificationRequestWithinProjectGroup_startedAndAwaited_finishesWithProbeInMatchResult() {
        testIdentification(probe, QueryForIdentifyPool(DEFAULT_PROJECT_ID))
        verifyOnce(dbManagerMock) { loadPeople(DEFAULT_PROJECT_ID, null, null) }
    }

    @Test
    fun identificationRequestWithinUserGroup_startedAndAwaited_finishesWithProbeInMatchResult() {
        testIdentification(probe, QueryForIdentifyPool(DEFAULT_PROJECT_ID, DEFAULT_USER_ID))
        verifyOnce(dbManagerMock) { loadPeople(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, null) }
    }

    @Test
    fun identificationRequestWithinUserGroupAndProbeEnrolledByDifferentUser_startedAndAwaited_finishesWithoutProbeInMatchResult() {
        val probe = PeopleGeneratorUtils.getRandomPerson(
            projectId = DEFAULT_PROJECT_ID,
            userId = "${DEFAULT_USER_ID}_different_user",
            moduleId = DEFAULT_MODULE_ID
        )

        testIdentification(probe, QueryForIdentifyPool(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, null), false)

        verifyOnce(dbManagerMock) { loadPeople(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, null) }
    }

    @Test
    fun identificationRequestWithinModuleGroup_startedAndAwaited_finishesWithProbeInMatchResult() {
        testIdentification(probe, QueryForIdentifyPool(DEFAULT_PROJECT_ID, null, DEFAULT_MODULE_ID))
        verifyOnce(dbManagerMock) { loadPeople(DEFAULT_PROJECT_ID, null, DEFAULT_MODULE_ID) }
    }

    @Test
    fun identificationRequestWithinModuleGroupAndProbeNoInTheGroup_startedAndAwaited_finishesWithoutProbeInMatchResult() {

        val probe = PeopleGeneratorUtils.getRandomPerson(
            projectId = DEFAULT_PROJECT_ID,
            userId = DEFAULT_USER_ID,
            moduleId = "${DEFAULT_MODULE_ID}_different_module"
        )

        testIdentification(probe, QueryForIdentifyPool(DEFAULT_PROJECT_ID, null, DEFAULT_MODULE_ID), false)

        verifyOnce(dbManagerMock) { loadPeople(DEFAULT_PROJECT_ID, null, DEFAULT_MODULE_ID) }
    }

    @Test
    fun verificationRequest_startedAndAwaited_finishesWithCorrectResult() {
        setupDbManagerLoadCandidate()

        with(createViewModelAndStart(verifyRequest, mockVerificationLibMatcher)) {
            with(result.test().awaitValue().value()) {
                assertEquals(ResultCode.OK, resultCode)
                assertNotNull(
                    data?.getParcelableExtra<MatchingTaskVerifyResult>(MatchingTaskResult.BUNDLE_KEY)?.let {
                        assertEquals(VERIFY_GUID, it.guid)
                    }
                )
            }
        }
    }

    @Test
    fun identificationRequestWithinModule_startedAndAwaited_updatesViewCorrectly() {
        val candidates = PeopleGeneratorUtils.getRandomPeople(CANDIDATE_POOL, DEFAULT_PROJECT_ID).toList()
        setupDbManagerLoadCandidates(candidates)

        with(createViewModel()) {
            val progressTestObserver = progress.test()
            start(identifyRequestWithinProjectGroup, mockIdentificationLibMatcher)
            result.test().awaitValue().assertValue { it.finishDelayMillis == IdentificationTask.matchingEndWaitTimeInMillis }
            hasLoadingBegun.test().assertValue { it }
            matchBeginningSummary.test().assertValue { it.matchSize == CANDIDATE_POOL }
            assertThat(progressTestObserver.valueHistory())
                .containsExactlyElementsIn((0..0) + (25..25) + (50..50) + IDENTIFY_PROGRESS_RANGE.map { it / 2 + 50 } + (100..100))
                .inOrder()
            matchFinishedSummary.test().assertValue { it.returnSize == DEFAULT_NUMBER_OF_ID_RETURNS }
        }
    }

    @Test
    fun verificationRequest_startedAndAwaited_updatesViewCorrectly() {
        setupDbManagerLoadCandidate()

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
        val candidates = PeopleGeneratorUtils.getRandomPeople(CANDIDATE_POOL, DEFAULT_PROJECT_ID).toList()
        setupDbManagerLoadCandidates(candidates)

        with(createViewModelAndStart(identifyRequestWithinProjectGroup, mockErrorLibMatcher)) {
            result.test().awaitValue()
            hasMatchFailed.test().assertValue { it }
            verifyOnce(crashReportManagerMock) { logExceptionOrSafeException(anyNotNull()) }
        }
    }

    @Test
    fun verifyRequest_matchFailsToComplete_showsToastAndLogsError() {
        setupDbManagerLoadCandidate()

        with(createViewModelAndStart(verifyRequest, mockErrorLibMatcher)) {
            result.test().awaitValue()
            hasMatchFailed.test().assertValue { it }
            verifyOnce(crashReportManagerMock) { logExceptionOrSafeException(anyNotNull()) }
        }
    }

    private fun testIdentification(personToIdentify: Person,
                                   queryForIdentifyPool: QueryForIdentifyPool,
                                   shouldProbeInMatchingResult: Boolean = true) {

        val rightCandidates: List<Person>
        val extraCandidates: List<Person>

        //Create people for the local db
        if (!queryForIdentifyPool.moduleId.isNullOrEmpty()) {
            rightCandidates = PeopleGeneratorUtils.getRandomPeople(CANDIDATE_POOL, DEFAULT_PROJECT_ID, moduleId = DEFAULT_MODULE_ID).toList()
            extraCandidates = PeopleGeneratorUtils.getRandomPeople(CANDIDATE_POOL, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, "${DEFAULT_MODULE_ID}_other_module").toList()
        } else if (!queryForIdentifyPool.userId.isNullOrEmpty()) {
            rightCandidates = PeopleGeneratorUtils.getRandomPeople(CANDIDATE_POOL, DEFAULT_PROJECT_ID, moduleId = DEFAULT_MODULE_ID).toList()
            extraCandidates = PeopleGeneratorUtils.getRandomPeople(CANDIDATE_POOL, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, "${DEFAULT_MODULE_ID}_other_module").toList()
        } else {
            rightCandidates = PeopleGeneratorUtils.getRandomPeople(CANDIDATE_POOL, DEFAULT_PROJECT_ID).toList()
            extraCandidates = emptyList()
        }

        setupDbManagerLoadCandidates(rightCandidates + extraCandidates + personToIdentify)

        with(createViewModelAndStart(MatchingTaskIdentifyRequest(
            DEFAULT_LANGUAGE,
            probe,
            queryForIdentifyPool,
            DEFAULT_NUMBER_OF_ID_RETURNS), mockIdentificationLibMatcher)) {
            verifyIdentificationResult(result.test().awaitValue().value(), personToIdentify, shouldProbeInMatchingResult)
        }
    }

    private fun verifyIdentificationResult(result: MatchingViewModel.FinishResult, probe: Person, shouldProbeInMatchingResult: Boolean = true) {
        assertNotNull(result.data?.getParcelableExtra<MatchingTaskIdentifyResult>(MatchingTaskResult.BUNDLE_KEY)?.apply {
            assertEquals(DEFAULT_NUMBER_OF_ID_RETURNS, identifications.size)
            if (shouldProbeInMatchingResult) {
                val highestScoreCandidate = identifications.maxBy { it.confidence }?.guid
                assertThat(highestScoreCandidate).isEqualTo(probe.patientId)
            } else {
                assertThat(identifications).doesNotContain(probe.patientId)
            }
        })
    }

    private fun createViewModelAndStart(request: MatchingTaskRequest,
                                        mockLibMatcher: (LibPerson, List<LibPerson>, LibMatcher.MATCHER_TYPE, MutableList<Float>, MatcherEventListener, Int) -> LibMatcher) =
        createViewModel().apply { start(request, mockLibMatcher) }

    private fun createViewModel() = get<MatchingViewModel>()

    private fun setupDbManagerLoadCandidates(candidates: List<Person>) {
        whenever(dbManagerMock) { loadPeople(anyNotNull(), anyOrNull(), anyOrNull()) } thenReturn Single.just(candidates)
    }

    private fun setupDbManagerLoadCandidate(verifyGuid: String = VERIFY_GUID) {
        val candidate = PeopleGeneratorUtils.getRandomPerson(verifyGuid, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID)

        whenever(dbManagerMock) { loadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.just(PersonFetchResult(candidate, false))
    }

    @After
    fun tearDown() {
        releaseFingerprintKoinModules()
    }

    companion object {

        private const val DEFAULT_NUMBER_OF_ID_RETURNS = 10
        private const val CANDIDATE_POOL = 50
        private val IDENTIFY_PROGRESS_RANGE = 0..100

        private const val VERIFY_GUID = "33eda6d0-22bb-475e-b439-3464433e5a87"
        private const val DEFAULT_LANGUAGE = "en"

        private val probe = PeopleGeneratorUtils.getRandomPerson(
            projectId = DEFAULT_PROJECT_ID,
            userId = DEFAULT_USER_ID,
            moduleId = DEFAULT_MODULE_ID
        )

        private val identifyRequestWithinProjectGroup = MatchingTaskIdentifyRequest(
            DEFAULT_LANGUAGE,
            probe,
            QueryForIdentifyPool(DEFAULT_PROJECT_ID),
            DEFAULT_NUMBER_OF_ID_RETURNS)

        private val verifyRequest = MatchingTaskVerifyRequest(
            DEFAULT_LANGUAGE,
            probe,
            MatchingTaskVerifyRequest.QueryForVerifyPool(DEFAULT_PROJECT_ID),
            VERIFY_GUID)
    }
}
