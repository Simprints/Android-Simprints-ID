package com.simprints.fingerprint.orchestrator

import android.app.Activity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.launch.result.LaunchTaskResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskIdentifyResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskVerifyResult
import com.simprints.fingerprint.activities.orchestrator.OrchestratorViewModel
import com.simprints.fingerprint.activities.refusal.result.RefusalTaskResult
import com.simprints.fingerprint.commontesttools.generators.PeopleGeneratorUtils
import com.simprints.fingerprint.data.domain.Action
import com.simprints.fingerprint.data.domain.matching.MatchingResult
import com.simprints.fingerprint.data.domain.matching.MatchingTier
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.MatchGroup
import com.simprints.fingerprint.orchestrator.task.FingerprintTask.*
import com.simprints.fingerprint.orchestrator.task.ResultCode
import com.simprints.fingerprint.orchestrator.taskflow.FinalResult
import com.simprints.fingerprint.tasks.saveperson.SavePersonTaskResult
import com.simprints.moduleapi.fingerprint.responses.*
import com.simprints.testtools.common.blocking.BlockingFlag
import com.simprints.testtools.common.syntax.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.random.Random as Rand

@RunWith(AndroidJUnit4::class)
class OrchestratorTest {

    private lateinit var orchestrator: Orchestrator

    private val isFlowFinished = BlockingFlag()

    private val runnableTaskDispatcherMock: RunnableTaskDispatcher = mock()
    private val viewModelMock: OrchestratorViewModel = setupMock {
        whenThis { handleFlowFinished(anyNotNull()) } then { isFlowFinished.finish() }
    }

    @Before
    fun setup() {
        orchestrator = Orchestrator(viewModelMock, runnableTaskDispatcherMock)
    }

    @Test
    fun enrolTaskFlow_allResultsOk_shouldFinishSuccessfully() {
        mockActivityResults()
        mockRunnableTaskResults()

        orchestrator.start(createFingerprintRequest(Action.ENROL))

        isFlowFinished.await()

        val activityTasks = argumentCaptor<ActivityTask>()
        val runnableTasks = argumentCaptor<RunnableTask>()
        val finalResult = argumentCaptor<FinalResult>()
        verifyExactly(2, viewModelMock) { postNextTask(activityTasks.capture()) }
        verifyOnce(runnableTaskDispatcherMock) { runTask(runnableTasks.capture(), anyNotNull()) }
        verifyOnce(viewModelMock) { handleFlowFinished(finalResult.capture()) }

        assertTrue(activityTasks.firstValue is Launch)
        assertTrue(activityTasks.secondValue is CollectFingerprints)
        assertTrue(runnableTasks.firstValue is SavePerson)
        with(finalResult.firstValue) {
            assertEquals(Activity.RESULT_OK, resultCode)
            assertNotNull(resultData?.extras?.getParcelable<IFingerprintEnrolResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                assertEquals(IFingerprintResponseType.ENROL, type)
            })
        }
    }

    @Test
    fun identifyTaskFlow_allResultsOk_shouldFinishSuccessfully() {
        mockActivityResults()
        mockRunnableTaskResults()

        orchestrator.start(createFingerprintRequest(Action.IDENTIFY))

        isFlowFinished.await()

        val activityTasks = argumentCaptor<ActivityTask>()
        val finalResult = argumentCaptor<FinalResult>()
        verifyExactly(3, viewModelMock) { postNextTask(activityTasks.capture()) }
        verifyNever(runnableTaskDispatcherMock) { runTask(anyNotNull(), anyNotNull()) }
        verifyOnce(viewModelMock) { handleFlowFinished(finalResult.capture()) }

        assertTrue(activityTasks.firstValue is Launch)
        assertTrue(activityTasks.secondValue is CollectFingerprints)
        assertTrue(activityTasks.thirdValue is Matching)
        with(finalResult.firstValue) {
            assertEquals(Activity.RESULT_OK, resultCode)
            assertNotNull(resultData?.extras?.getParcelable<IFingerprintIdentifyResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                assertEquals(IFingerprintResponseType.IDENTIFY, type)
            })
        }
    }

    @Test
    fun verifyTaskFlow_allResultsOk_shouldFinishSuccessfully() {
        mockActivityResults()
        mockRunnableTaskResults()

        orchestrator.start(createFingerprintRequest(Action.VERIFY))

        isFlowFinished.await()

        val activityTasks = argumentCaptor<ActivityTask>()
        val finalResult = argumentCaptor<FinalResult>()
        verifyExactly(3, viewModelMock) { postNextTask(activityTasks.capture()) }
        verifyNever(runnableTaskDispatcherMock) { runTask(anyNotNull(), anyNotNull()) }
        verifyOnce(viewModelMock) { handleFlowFinished(finalResult.capture()) }

        assertTrue(activityTasks.firstValue is Launch)
        assertTrue(activityTasks.secondValue is CollectFingerprints)
        assertTrue(activityTasks.thirdValue is Matching)
        with(finalResult.firstValue) {
            assertEquals(Activity.RESULT_OK, resultCode)
            assertNotNull(resultData?.extras?.getParcelable<IFingerprintVerifyResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                assertEquals(IFingerprintResponseType.VERIFY, type)
            })
        }
    }

    @Test
    fun enrolTaskFlow_failsDueToAlertInLaunch_shouldFinishCancelledWithError() {
        mockActivityResults(launch = ::setupAlertResult)

        orchestrator.start(createFingerprintRequest(Action.ENROL))

        isFlowFinished.await()

        val activityTasks = argumentCaptor<ActivityTask>()
        val finalResult = argumentCaptor<FinalResult>()
        verifyOnce(viewModelMock) { postNextTask(activityTasks.capture()) }
        verifyNever(runnableTaskDispatcherMock) { runTask(anyNotNull(), anyNotNull()) }
        verifyOnce(viewModelMock) { handleFlowFinished(finalResult.capture()) }

        assertTrue(activityTasks.firstValue is Launch)
        with(finalResult.firstValue) {
            assertEquals(Activity.RESULT_CANCELED, resultCode)
            assertNotNull(resultData?.extras?.getParcelable<IFingerprintErrorResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                assertEquals(IFingerprintResponseType.ERROR, type)
            })
        }
    }

    @Test
    fun enrolTaskFlow_failsDueToRefusalInLaunch_shouldFinishOkWithRefused() {
        mockActivityResults(launch = ::setupRefusalResult)

        orchestrator.start(createFingerprintRequest(Action.ENROL))

        isFlowFinished.await()

        val activityTasks = argumentCaptor<ActivityTask>()
        val finalResult = argumentCaptor<FinalResult>()
        verifyOnce(viewModelMock) { postNextTask(activityTasks.capture()) }
        verifyNever(runnableTaskDispatcherMock) { runTask(anyNotNull(), anyNotNull()) }
        verifyOnce(viewModelMock) { handleFlowFinished(finalResult.capture()) }

        assertTrue(activityTasks.firstValue is Launch)
        with(finalResult.firstValue) {
            assertEquals(Activity.RESULT_OK, resultCode)
            assertNotNull(resultData?.extras?.getParcelable<IFingerprintRefusalFormResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                assertEquals(IFingerprintResponseType.REFUSAL, type)
            })
        }
    }

    @Test
    fun enrolTaskFlow_cancelledSomehow_shouldFinishCancelledWithNoData() {
        mockActivityResults(collect = ::setupCancelledResult)

        orchestrator.start(createFingerprintRequest(Action.ENROL))

        isFlowFinished.await()

        val activityTasks = argumentCaptor<ActivityTask>()
        val finalResult = argumentCaptor<FinalResult>()
        verifyExactly(2, viewModelMock) { postNextTask(activityTasks.capture()) }
        verifyNever(runnableTaskDispatcherMock) { runTask(anyNotNull(), anyNotNull()) }
        verifyOnce(viewModelMock) { handleFlowFinished(finalResult.capture()) }

        assertTrue(activityTasks.firstValue is Launch)
        assertTrue(activityTasks.secondValue is CollectFingerprints)
        with(finalResult.firstValue) {
            assertEquals(Activity.RESULT_CANCELED, resultCode)
            assertNull(resultData?.extras)
        }
    }

    private fun mockActivityResults(
        launch: () -> Unit = ::setupOkLaunchResult,
        collect: () -> Unit = ::setupOkCollectResult,
        matchingVerify: () -> Unit = ::setupOkMatchingVerifyResult,
        matchingIdentify: () -> Unit = ::setupOkMatchingIdentifyResult
    ) {
        whenever(viewModelMock) { postNextTask(anyNotNull()) } then { mock ->
            when (val activityTask = mock.arguments.find { it is ActivityTask } as? ActivityTask) {
                is Launch -> launch()
                is CollectFingerprints -> collect()
                is Matching -> {
                    when (activityTask.subAction) {
                        Matching.SubAction.IDENTIFY -> matchingIdentify()
                        Matching.SubAction.VERIFY -> matchingVerify()
                    }
                }
                else -> throw IllegalStateException("Unexpected ActivityTask")
            }
        }
    }

    private fun mockRunnableTaskResults(
        savePerson: () -> Unit = ::setupOkSavePersonResult
    ) {
        whenever(runnableTaskDispatcherMock) { runTask(anyNotNull(), anyNotNull()) } then { mock ->
            when (mock.arguments.find { it is RunnableTask } as? RunnableTask) {
                is SavePerson -> savePerson()
                else -> throw IllegalStateException("Unexpected RunnableTask")
            }
        }
    }

    private fun setupOkLaunchResult() {
        orchestrator.handleActivityTaskResult(ResultCode.OK) {
            assertEquals(LaunchTaskResult.BUNDLE_KEY, it)
            LaunchTaskResult()
        }
    }

    private fun setupOkCollectResult() {
        orchestrator.handleActivityTaskResult(ResultCode.OK) {
            assertEquals(CollectFingerprintsTaskResult.BUNDLE_KEY, it)
            CollectFingerprintsTaskResult(PeopleGeneratorUtils.getRandomPerson())
        }
    }

    private fun setupOkMatchingIdentifyResult() {
        orchestrator.handleActivityTaskResult(ResultCode.OK) { key ->
            assertEquals(MatchingTaskResult.BUNDLE_KEY, key)
            MatchingTaskIdentifyResult(List(10) {
                val score = Rand.nextInt(100)
                MatchingResult(UUID.randomUUID().toString(), score, MatchingTier.computeTier(score.toFloat()))
            }.sortedByDescending { it.confidence })
        }
    }

    private fun setupOkMatchingVerifyResult() {
        orchestrator.handleActivityTaskResult(ResultCode.OK) {
            assertEquals(MatchingTaskResult.BUNDLE_KEY, it)
            val score = Rand.nextInt(100)
            MatchingTaskVerifyResult(UUID.randomUUID().toString(), score, MatchingTier.computeTier(score.toFloat()))
        }
    }

    private fun setupOkSavePersonResult() {
        orchestrator.handleRunnableTaskResult(
            SavePersonTaskResult(true)
        )
    }

    private fun setupAlertResult() {
        orchestrator.handleActivityTaskResult(ResultCode.ALERT) { key ->
            assertEquals(AlertTaskResult.BUNDLE_KEY, key)
            AlertTaskResult(FingerprintAlert.BLUETOOTH_NOT_SUPPORTED, AlertTaskResult.CloseButtonAction.CLOSE)
        }
    }

    private fun setupRefusalResult() {
        orchestrator.handleActivityTaskResult(ResultCode.REFUSED) { key ->
            assertEquals(RefusalTaskResult.BUNDLE_KEY, key)
            RefusalTaskResult(RefusalTaskResult.Action.SUBMIT, RefusalTaskResult.Answer())
        }
    }

    private fun setupCancelledResult() {
        orchestrator.handleActivityTaskResult(ResultCode.CANCELLED) {
            throw IllegalStateException("Should not be invoked")
        }
    }

    companion object {
        private const val DEFAULT_PROJECT_ID = "some_project_id"
        private const val DEFAULT_USER_ID = "some_user_id"
        private const val DEFAULT_MODULE_ID = "some_module_id"
        private const val DEFAULT_META_DATA = ""
        private const val DEFAULT_LANGUAGE = "en"
        private const val DEFAULT_LOGO_EXISTS = true
        private const val DEFAULT_PROGRAM_NAME = "This program"
        private const val DEFAULT_ORGANISATION_NAME = "This organisation"
        private const val DEFAULT_VERIFY_GUID = "verify_guid"
        private const val DEFAULT_NUMBER_OF_ID_RETURNS = 10
        private val DEFAULT_MATCH_GROUP = MatchGroup.GLOBAL
        private val DEFAULT_FINGER_STATUS = mapOf(
            FingerIdentifier.RIGHT_THUMB to false,
            FingerIdentifier.RIGHT_INDEX_FINGER to false,
            FingerIdentifier.RIGHT_3RD_FINGER to false,
            FingerIdentifier.RIGHT_4TH_FINGER to false,
            FingerIdentifier.RIGHT_5TH_FINGER to false,
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.LEFT_INDEX_FINGER to true,
            FingerIdentifier.LEFT_3RD_FINGER to false,
            FingerIdentifier.LEFT_4TH_FINGER to false,
            FingerIdentifier.LEFT_5TH_FINGER to false
        )

        private fun createFingerprintRequest(action: Action) =
            when (action) {
                Action.ENROL -> FingerprintEnrolRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID,
                    DEFAULT_MODULE_ID, DEFAULT_META_DATA, DEFAULT_LANGUAGE, DEFAULT_FINGER_STATUS,
                    DEFAULT_LOGO_EXISTS, DEFAULT_PROGRAM_NAME, DEFAULT_ORGANISATION_NAME)
                Action.IDENTIFY -> FingerprintIdentifyRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID,
                    DEFAULT_MODULE_ID, DEFAULT_META_DATA, DEFAULT_LANGUAGE, DEFAULT_FINGER_STATUS,
                    DEFAULT_LOGO_EXISTS, DEFAULT_ORGANISATION_NAME, DEFAULT_PROGRAM_NAME,
                    DEFAULT_MATCH_GROUP, DEFAULT_NUMBER_OF_ID_RETURNS)
                Action.VERIFY -> FingerprintVerifyRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID,
                    DEFAULT_MODULE_ID, DEFAULT_META_DATA, DEFAULT_LANGUAGE, DEFAULT_FINGER_STATUS,
                    DEFAULT_LOGO_EXISTS, DEFAULT_PROGRAM_NAME, DEFAULT_ORGANISATION_NAME,
                    DEFAULT_VERIFY_GUID)
            }
    }
}
