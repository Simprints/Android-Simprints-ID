package com.simprints.fingerprints.activities.matching

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockito_kotlin.KArgumentCaptor
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.simprints.fingerprints.testtools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.fingerprints.testtools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.fingerprints.testtools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.fingerprints.testtools.PeopleGeneratorUtils
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.domain.requests.IdentifyRequest
import com.simprints.id.domain.requests.Request
import com.simprints.id.domain.requests.VerifyRequest
import com.simprints.id.domain.responses.IdentifyResponse
import com.simprints.id.domain.responses.Response
import com.simprints.id.domain.responses.VerifyResponse
import com.simprints.id.tools.TimeHelper
import com.simprints.libmatcher.EVENT
import com.simprints.libmatcher.LibMatcher
import com.simprints.libmatcher.Progress
import com.simprints.libmatcher.sourceafis.MatcherEventListener
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.reactive.RxSchedulerRule
import io.reactivex.Single
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.robolectric.annotation.Config
import java.util.concurrent.LinkedBlockingQueue
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
@Config(sdk = [25], manifest = Config.NONE)
class MatchingPresenterTest {

    @get:Rule val rxSchedulerRule = RxSchedulerRule()

    private val matchTaskFinishedFlag = LinkedBlockingQueue<Unit>()

    private val viewMock = mock<MatchingContract.View>().apply {
        whenever(this) { doFinish() } then { matchTaskFinishedFlag.put(Unit) }
        whenever(this) {
            setIdentificationProgressFinished(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())
        } then { matchTaskFinishedFlag.put(Unit) }
    }

    private val dbManagerMock = mock<DbManager>()
    private val preferencesManagerMock = mock<PreferencesManager>()
    private val sessionEventsManagerMock = mock<SessionEventsManager>()
    private val crashReportManagerMock = mock<CrashReportManager>()
    private val timeHelperMock = mock<TimeHelper>().apply {
        whenever(this) { now() } thenReturn System.currentTimeMillis()
    }

    private val mockIdentificationLibMatcher: (com.simprints.libcommon.Person, List<com.simprints.libcommon.Person>,
                                               LibMatcher.MATCHER_TYPE, MutableList<Float>, MatcherEventListener, Int) -> LibMatcher = { _, candidates, _, scores, callback, _ ->
        mock<LibMatcher>().apply {
            whenever(this) { start() } then {
                (0..100).forEach { callback.onMatcherProgress(Progress(it)) }
                repeat(candidates.size) { scores.add(Random.nextFloat() * 100f) }
                callback.onMatcherEvent(EVENT.MATCH_COMPLETED)
            }
        }
    }

    @Test
    fun identificationRequest_startedAndAwaited_finishesWithCorrectResult() {
        setupDbManagerLoadCandidates()
        setupPrefs()
        val result = captureMatchingResult()

        val presenter = createPresenter(identifyRequest, probe)
        presenter.start()
        matchTaskFinishedFlag.take()

        val identifyResponse = result.firstValue.getParcelableExtra<IdentifyResponse>(Response.BUNDLE_KEY)!!
        val identifications = identifyResponse.identifications
        Assert.assertEquals(NUMBER_OF_ID_RETURNS, identifications.size)
    }

    @Test
    fun verificationRequest_startedAndAwaited_finishesWithCorrectResult() {
        setupDbManagerLoadCandidate()
        setupPrefs()
        val result = captureMatchingResult()

        val presenter = createPresenter(verifyRequest, probe)
        presenter.start()
        matchTaskFinishedFlag.take()

        val verifyResponse = result.firstValue.getParcelableExtra<VerifyResponse>(Response.BUNDLE_KEY)!!
        Assert.assertEquals(VERIFY_GUID, verifyResponse.guid)
    }

    private fun createPresenter(request: Request, probe: Person) =
        MatchingPresenter(viewMock, probe, request, dbManagerMock, preferencesManagerMock,
            sessionEventsManagerMock, crashReportManagerMock, timeHelperMock, mockIdentificationLibMatcher)

    private fun setupDbManagerLoadCandidates() {
        val candidates = PeopleGeneratorUtils.getRandomPeople(CANDIDATE_POOL, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID).toList()

        whenever(dbManagerMock) { loadPeople(anyNotNull()) } thenReturn Single.just(candidates)
    }

    private fun setupDbManagerLoadCandidate(verifyGuid: String = VERIFY_GUID) {
        val candidate = PeopleGeneratorUtils.getRandomPerson(verifyGuid, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID)

        whenever(dbManagerMock) { loadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.just(PersonFetchResult(candidate, false))
    }

    private fun setupPrefs(numberIdReturns: Int = NUMBER_OF_ID_RETURNS,
                           matcherTypeInt: Int = 0) {
        whenever(preferencesManagerMock) { returnIdCount } thenReturn numberIdReturns
        whenever(preferencesManagerMock) { matcherType } thenReturn matcherTypeInt
    }

    private fun captureMatchingResult(): KArgumentCaptor<Intent> {
        val result = argumentCaptor<Intent>()
        whenever(viewMock) { doSetResult(anyInt(), result.capture()) } thenDoNothing {}
        return result
    }

    companion object {

        private const val NUMBER_OF_ID_RETURNS = 10
        private const val CANDIDATE_POOL = 50

        private const val VERIFY_GUID = "33eda6d0-22bb-475e-b439-3464433e5a87"

        private val probe = PeopleGeneratorUtils.getRandomPerson(
            projectId = DEFAULT_PROJECT_ID,
            userId = DEFAULT_USER_ID,
            moduleId = DEFAULT_USER_ID
        )

        private val identifyRequest = IdentifyRequest(
            DEFAULT_PROJECT_ID,
            DEFAULT_USER_ID,
            DEFAULT_MODULE_ID,
            "")

        private val verifyRequest = VerifyRequest(
            DEFAULT_PROJECT_ID,
            DEFAULT_USER_ID,
            DEFAULT_MODULE_ID,
            "",
            VERIFY_GUID)
    }
}
