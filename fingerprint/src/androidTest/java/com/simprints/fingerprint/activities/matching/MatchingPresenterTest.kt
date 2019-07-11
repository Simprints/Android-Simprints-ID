package com.simprints.fingerprint.activities.matching

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.simprints.fingerprint.commontesttools.PeopleGeneratorUtils
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.repository.models.PersonFetchResult
import com.simprints.fingerprint.activities.matching.request.MatchingTaskIdentifyRequest
import com.simprints.fingerprint.activities.matching.request.MatchingTaskIdentifyRequest.QueryForIdentifyPool
import com.simprints.fingerprint.activities.matching.request.MatchingTaskRequest
import com.simprints.fingerprint.activities.matching.request.MatchingTaskVerifyRequest
import com.simprints.fingerprint.activities.matching.result.MatchingTaskIdentifyResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskVerifyResult
import com.simprints.fingerprint.data.domain.person.Person
import com.simprints.fingerprint.testtools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.fingerprint.testtools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.fingerprint.testtools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.fingerprintmatcher.EVENT
import com.simprints.fingerprintmatcher.LibMatcher
import com.simprints.fingerprintmatcher.Progress
import com.simprints.fingerprintmatcher.sourceafis.MatcherEventListener
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.reactive.RxSchedulerRule
import io.reactivex.Single
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import java.util.concurrent.LinkedBlockingQueue
import kotlin.random.Random
import com.simprints.fingerprintmatcher.Person as LibPerson

@RunWith(AndroidJUnit4::class)
@MediumTest
class MatchingPresenterTest {

    @get:Rule val rxSchedulerRule = RxSchedulerRule()

    private val matchTaskFinishedFlag = LinkedBlockingQueue<Unit>()

    private val viewMock = mock<MatchingContract.View> {
        whenever(it) { makeToastMatchFailed() } then { matchTaskFinishedFlag.put(Unit) }
        whenever(it) { doFinish() } then { matchTaskFinishedFlag.put(Unit) }
        whenever(it) {
            setIdentificationProgressFinished(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())
        } then { matchTaskFinishedFlag.put(Unit) }
    }

    private val dbManagerMock = mock<FingerprintDbManager>()
    private val preferencesManagerMock = mock<PreferencesManager>()
    private val sessionEventsManagerMock = mock<FingerprintSessionEventsManager>()
    private val crashReportManagerMock = mock<FingerprintCrashReportManager>()

    private val mockIdentificationLibMatcher: (LibPerson, List<LibPerson>,
                                               LibMatcher.MATCHER_TYPE, MutableList<Float>, MatcherEventListener, Int) -> LibMatcher =
        { probe, candidates, _, scores, callback, _ ->
            mock {
                whenever(it) { start() } then {
                    IDENTIFY_PROGRESS_RANGE.forEach { n -> callback.onMatcherProgress(Progress(n)) }
                    scores.addAll(candidates.map {
                        (if (it.guid == probe.guid) { 1L } else { 0L }).toFloat()
                    })
                    callback.onMatcherEvent(EVENT.MATCH_COMPLETED)
                }
            }
        }

    private val mockVerificationLibMatcher: (LibPerson, List<LibPerson>,
                                             LibMatcher.MATCHER_TYPE, MutableList<Float>, MatcherEventListener, Int) -> LibMatcher =
        { probe, candidates, _, scores, callback, _ ->
            mock {
                whenever(it) { start() } then {
                    callback.onMatcherProgress(Progress(0))
                    scores.add(Random.nextFloat())
                    callback.onMatcherEvent(EVENT.MATCH_COMPLETED)
                }
            }
        }

    private val mockErrorLibMatcher: (LibPerson, List<LibPerson>,
                                      LibMatcher.MATCHER_TYPE, MutableList<Float>, MatcherEventListener, Int) -> LibMatcher =
        { _, _, _, _, callback, _ ->
            mock {
                whenever(it) { start() } then { callback.onMatcherEvent(EVENT.MATCH_NOT_RUNNING) }
            }
        }

    @Test
    @MediumTest
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
    fun identificationRequestWithinUserGroupAndProbeEnroledByDifferentUser_startedAndAwaited_finishesWithoutProbeInMatchResult() {
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
        setupPrefs()
        val result = captureMatchingResult()

        val presenter = createPresenter(verifyRequest, mockVerificationLibMatcher)
        presenter.start()
        matchTaskFinishedFlag.take()

        val verifyResponse = result.firstValue.getParcelableExtra<MatchingTaskVerifyResult>(MatchingTaskResult.BUNDLE_KEY)!!
        Assert.assertEquals(VERIFY_GUID, verifyResponse.guid)
    }

    @Test
    fun identificationRequestWithinModule_startedAndAwaited_updatesViewCorrectly() {
        val candidates = PeopleGeneratorUtils.getRandomPeople(CANDIDATE_POOL, DEFAULT_PROJECT_ID).toList()

        setupDbManagerLoadCandidates(candidates)
        setupPrefs()

        val presenter = createPresenter(identifyRequestWithinProjectGroup, mockIdentificationLibMatcher)
        presenter.start()
        matchTaskFinishedFlag.take()

        verifyOnce(viewMock) { setIdentificationProgressLoadingStart() }
        verifyOnce(viewMock) { setIdentificationProgressMatchingStart(eq(CANDIDATE_POOL)) }
        val progressIntCaptor = argumentCaptor<Int>()
        verifyExactly(IDENTIFY_PROGRESS_RANGE.count(), viewMock) { setIdentificationProgress(progressIntCaptor.capture()) }
        assertThat(progressIntCaptor.allValues)
            .containsExactlyElementsIn(IDENTIFY_PROGRESS_RANGE)
            .inOrder()
        verifyOnce(viewMock) { setIdentificationProgressReturningStart() }
        verifyOnce(viewMock) { setIdentificationProgressFinished(eq(NUMBER_OF_ID_RETURNS), anyInt(), anyInt(), anyInt(), anyInt()) }
    }

    @Test
    fun verificationRequest_startedAndAwaited_updatesViewCorrectly() {
        setupDbManagerLoadCandidate()
        setupPrefs()

        val presenter = createPresenter(verifyRequest, mockVerificationLibMatcher)
        presenter.start()
        matchTaskFinishedFlag.take()

        verifyOnce(viewMock) { setVerificationProgress() }
        verifyOnce(viewMock) { doFinish() }
    }

    @Test
    fun identifyRequest_matchFailsToComplete_showsToastAndLogsError() {
        val candidates = PeopleGeneratorUtils.getRandomPeople(CANDIDATE_POOL, DEFAULT_PROJECT_ID).toList()
        setupDbManagerLoadCandidates(candidates)
        setupPrefs()

        val presenter = createPresenter(identifyRequestWithinProjectGroup, mockErrorLibMatcher)
        presenter.start()
        matchTaskFinishedFlag.take()

        verifyOnce(viewMock) { makeToastMatchFailed() }
        verifyOnce(crashReportManagerMock) { logExceptionOrSafeException(anyNotNull()) }
    }

    @Test
    fun verifyRequest_matchFailsToComplete_showsToastAndLogsError() {
        setupDbManagerLoadCandidate()
        setupPrefs()

        val presenter = createPresenter(verifyRequest, mockErrorLibMatcher)
        presenter.start()
        matchTaskFinishedFlag.take()

        verifyOnce(viewMock) { makeToastMatchFailed() }
        verifyOnce(crashReportManagerMock) { logExceptionOrSafeException(anyNotNull()) }
    }

    private fun testIdentification(personToIdentify: Person,
                                   queryForIdentifyPool: QueryForIdentifyPool,
                                   shouldProbeInMatchingResult: Boolean = true) {

        val rightCandidates: List<Person>
        val extraCandidates: List<Person>

        //Create people for the local db
        if(!queryForIdentifyPool.moduleId.isNullOrEmpty()) {
            rightCandidates = PeopleGeneratorUtils.getRandomPeople(CANDIDATE_POOL, DEFAULT_PROJECT_ID, moduleId = DEFAULT_MODULE_ID).toList()
            extraCandidates = PeopleGeneratorUtils.getRandomPeople(CANDIDATE_POOL, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, "${DEFAULT_MODULE_ID}_other_module").toList()
        } else if(!queryForIdentifyPool.userId.isNullOrEmpty()) {
            rightCandidates = PeopleGeneratorUtils.getRandomPeople(CANDIDATE_POOL, DEFAULT_PROJECT_ID, moduleId = DEFAULT_MODULE_ID).toList()
            extraCandidates = PeopleGeneratorUtils.getRandomPeople(CANDIDATE_POOL, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, "${DEFAULT_MODULE_ID}_other_module").toList()
        } else {
            rightCandidates = PeopleGeneratorUtils.getRandomPeople(CANDIDATE_POOL, DEFAULT_PROJECT_ID).toList()
            extraCandidates = emptyList()
        }

        setupDbManagerLoadCandidates(rightCandidates + extraCandidates + personToIdentify)
        setupPrefs()
        val result = captureMatchingResult()

        runIdentification(MatchingTaskIdentifyRequest(
            DEFAULT_LANGUAGE,
            probe,
            queryForIdentifyPool,
            10))

        verifyIdentificationResult(result, personToIdentify, shouldProbeInMatchingResult)
    }

    private fun verifyIdentificationResult(result: KArgumentCaptor<Intent>, probe: Person, shouldProbeInMatchingResult: Boolean = true) {
        val identifyResponse = result.firstValue.getParcelableExtra<MatchingTaskIdentifyResult>(MatchingTaskResult.BUNDLE_KEY)!!
        val identificationsResult = identifyResponse.identifications
        Assert.assertEquals(NUMBER_OF_ID_RETURNS, identificationsResult.size)
        if(shouldProbeInMatchingResult) {
            val highestScoreCandidate = identificationsResult.sortedByDescending { it.confidence }.first().guid
            assertThat(highestScoreCandidate).isEqualTo(probe.patientId)
        } else {
            assertThat(identificationsResult).doesNotContain(probe.patientId)
        }
    }

    private fun createPresenter(request: MatchingTaskRequest,
                                mockLibMatcher: (LibPerson, List<LibPerson>, LibMatcher.MATCHER_TYPE, MutableList<Float>, MatcherEventListener, Int) -> LibMatcher) =
        MatchingPresenter(viewMock, request, dbManagerMock,
            sessionEventsManagerMock, crashReportManagerMock, mock(), mock(), mockLibMatcher)

    private fun setupDbManagerLoadCandidates(candidates: List<Person>) {
        whenever(dbManagerMock) { loadPeople(anyNotNull(), anyOrNull(), anyOrNull()) } thenReturn Single.just(candidates)
    }

    private fun setupDbManagerLoadCandidate(verifyGuid: String = VERIFY_GUID) {
        val candidate = PeopleGeneratorUtils.getRandomPerson(verifyGuid, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID)

        whenever(dbManagerMock) { loadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.just(PersonFetchResult(candidate, false))
    }

    private fun setupPrefs(numberIdReturns: Int = NUMBER_OF_ID_RETURNS) {
        whenever(preferencesManagerMock) { returnIdCount } thenReturn numberIdReturns
    }

    private fun captureMatchingResult(): KArgumentCaptor<Intent> {
        val result = argumentCaptor<Intent>()
        whenever(viewMock) { doSetResult(anyNotNull(), result.capture()) } thenDoNothing {}
        return result
    }

    private fun runIdentification(identifyRequest: MatchingTaskIdentifyRequest) {
        val presenter = createPresenter(identifyRequest, mockIdentificationLibMatcher)
        presenter.start()
        matchTaskFinishedFlag.take()
    }


    companion object {

        private const val NUMBER_OF_ID_RETURNS = 10
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
            10)

        private val verifyRequest = MatchingTaskVerifyRequest(
            DEFAULT_LANGUAGE,
            probe,
            MatchingTaskVerifyRequest.QueryForVerifyPool(DEFAULT_PROJECT_ID),
            VERIFY_GUID)
    }
}
