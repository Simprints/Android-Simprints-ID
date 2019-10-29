package com.simprints.fingerprint.orchestrator

import android.app.Activity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.fingerprint.activities.alert.FingerprintAlert.BLUETOOTH_NOT_SUPPORTED
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult.CloseButtonAction.CLOSE
import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.connect.result.ConnectScannerTaskResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskResult
import com.simprints.fingerprint.activities.refusal.result.RefusalTaskResult
import com.simprints.fingerprint.commontesttools.generators.PeopleGeneratorUtils
import com.simprints.fingerprint.controllers.core.flow.Action
import com.simprints.fingerprint.data.domain.matching.MatchResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.MatchGroup
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.orchestrator.task.FingerprintTask.*
import com.simprints.moduleapi.fingerprint.responses.*
import com.simprints.testtools.common.syntax.failTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.random.Random as Rand

@RunWith(AndroidJUnit4::class)
class OrchestratorTest {

    @Test
    fun captureTaskFlow_allResultsOk_shouldFinishSuccessfully() {
        with(Orchestrator(FinalResultBuilder())) {
            start(createFingerprintCaptureRequest())
            assertNextTaskIs<ConnectScanner>()
            okConnectResult()
            assertNextTaskIs<CollectFingerprints>()
            okCollectResult()
            assertTrue(isFinished())
            with(getFinalResult()) {
                assertEquals(Activity.RESULT_OK, resultCode)
                assertNotNull(resultData?.extras?.getParcelable<IFingerprintCaptureResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                    assertEquals(IFingerprintResponseType.CAPTURE, type)
                })
            }
        }
    }

    @Test
    fun identifyTaskFlow_allResultsOk_shouldFinishSuccessfully() {
        with(Orchestrator(FinalResultBuilder())) {
            start(createFingerprintRequest(Action.IDENTIFY))
            assertNextTaskIs<ConnectScanner>()
            okConnectResult()
            assertNextTaskIs<CollectFingerprints>()
            okCollectResult()
            assertNextTaskIs<Matching>()
            okMatchingIdentifyResult()
            assertTrue(isFinished())
            with(getFinalResult()) {
                assertEquals(Activity.RESULT_OK, resultCode)
                assertNotNull(resultData?.extras?.getParcelable<IFingerprintIdentifyResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                    assertEquals(IFingerprintResponseType.IDENTIFY, type)
                })
            }
        }
    }

    @Test
    fun verifyTaskFlow_allResultsOk_shouldFinishSuccessfully() {
        with(Orchestrator(FinalResultBuilder())) {
            start(createFingerprintRequest(Action.VERIFY))
            assertNextTaskIs<ConnectScanner>()
            okConnectResult()
            assertNextTaskIs<CollectFingerprints>()
            okCollectResult()
            assertNextTaskIs<Matching>()
            okMatchingVerifyResult()
            assertTrue(isFinished())
            with(getFinalResult()) {
                assertEquals(Activity.RESULT_OK, resultCode)
                assertNotNull(resultData?.extras?.getParcelable<IFingerprintVerifyResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                    assertEquals(IFingerprintResponseType.VERIFY, type)
                })
            }
        }
    }

    @Test
    fun captureTaskFlow_failsDueToAlertInConnectScanner_shouldFinishCancelledWithError() {
        with(Orchestrator(FinalResultBuilder())) {
            start(createFingerprintCaptureRequest())
            assertNextTaskIs<ConnectScanner>()
            alertResult()
            assertTrue(isFinished())
            with(getFinalResult()) {
                assertEquals(Activity.RESULT_CANCELED, resultCode)
                assertNotNull(resultData?.extras?.getParcelable<IFingerprintErrorResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                    assertEquals(IFingerprintResponseType.ERROR, type)
                })
            }
        }
    }

    @Test
    fun captureTaskFlow_failsDueToRefusalInConnectScanner_shouldFinishOkWithRefused() {
        with(Orchestrator(FinalResultBuilder())) {
            start(createFingerprintCaptureRequest())
            assertNextTaskIs<ConnectScanner>()
            refusalResult()
            assertTrue(isFinished())
            with(getFinalResult()) {
                assertEquals(Activity.RESULT_OK, resultCode)
                assertNotNull(resultData?.extras?.getParcelable<IFingerprintExitFormResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                    assertEquals(IFingerprintResponseType.REFUSAL, type)
                })
            }
        }
    }

    @Test
    fun captureTaskFlow_cancelledSomehow_shouldFinishCancelledWithNoData() {
        with(Orchestrator(FinalResultBuilder())) {
            start(createFingerprintCaptureRequest())
            assertNextTaskIs<ConnectScanner>()
            okConnectResult()
            assertNextTaskIs<CollectFingerprints>()
            cancelledResult()
            assertTrue(isFinished())
            with(getFinalResult()) {
                assertEquals(Activity.RESULT_CANCELED, resultCode)
                assertNull(resultData?.extras)
            }
        }
    }

    @Test
    fun newOrchestrator_resumedFromStateAfterStarted_shouldAssumeNewState() {
        val state = with(Orchestrator(FinalResultBuilder())) {
            start(createFingerprintRequest(Action.IDENTIFY))
            assertNextTaskIs<ConnectScanner>()
            okConnectResult()
            assertNextTaskIs<CollectFingerprints>()
            okCollectResult()
            getState()
        }

        with(Orchestrator(FinalResultBuilder())) {
            start(createFingerprintRequest(Action.IDENTIFY))
            restoreState(state ?: failTest("Orchestrator state is null"))
            assertNextTaskIs<Matching>()
            okMatchingIdentifyResult()
            with(getFinalResult()) {
                assertEquals(Activity.RESULT_OK, resultCode)
                assertNotNull(resultData?.extras?.getParcelable<IFingerprintIdentifyResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                    assertEquals(IFingerprintResponseType.IDENTIFY, type)
                })
            }
        }
    }
    private inline fun <reified T : FingerprintTask> Orchestrator.assertNextTaskIs() {
        assertFalse(isFinished())
        assertTrue(getNextTask() is T)
    }

    private fun Orchestrator.okConnectResult() {
        handleActivityTaskResult(ResultCode.OK) {
            assertEquals(ConnectScannerTaskResult.BUNDLE_KEY, it)
            ConnectScannerTaskResult()
        }
    }

    private fun Orchestrator.okCollectResult() {
        handleActivityTaskResult(ResultCode.OK) {
            assertEquals(CollectFingerprintsTaskResult.BUNDLE_KEY, it)
            CollectFingerprintsTaskResult(List(2) { PeopleGeneratorUtils.getRandomFingerprint() }, PeopleGeneratorUtils.getRandomPerson())
        }
    }

    private fun Orchestrator.okMatchingIdentifyResult() {
        handleActivityTaskResult(ResultCode.OK) { key ->
            assertEquals(MatchingTaskResult.BUNDLE_KEY, key)
            MatchingTaskIdentifyResult(List(10) {
                val score = Rand.nextInt(100)
                MatchResult(UUID.randomUUID().toString(), score, MatchingTier.computeTier(score.toFloat()))
            }.sortedByDescending { it.confidence })
        }
    }

    private fun Orchestrator.okMatchingVerifyResult() {
        handleActivityTaskResult(ResultCode.OK) {
            assertEquals(MatchingTaskResult.BUNDLE_KEY, it)
            val score = Rand.nextInt(100)
            MatchingTaskVerifyResult(UUID.randomUUID().toString(), score, MatchingTier.computeTier(score.toFloat()))
        }
    }

    private fun Orchestrator.alertResult() {
        handleActivityTaskResult(ResultCode.ALERT) { key ->
            assertEquals(AlertTaskResult.BUNDLE_KEY, key)
            AlertTaskResult(BLUETOOTH_NOT_SUPPORTED, CLOSE)
        }
    }

    private fun Orchestrator.refusalResult() {
        handleActivityTaskResult(ResultCode.REFUSED) { key ->
            assertEquals(RefusalTaskResult.BUNDLE_KEY, key)
            RefusalTaskResult(RefusalTaskResult.Action.SUBMIT, RefusalTaskResult.Answer())
        }
    }

    private fun Orchestrator.cancelledResult() {
        handleActivityTaskResult(ResultCode.CANCELLED) {
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

        private fun createFingerprintCaptureRequest() =
            FingerprintCaptureRequest(DEFAULT_LANGUAGE, DEFAULT_FINGER_STATUS)

        private fun createFingerprintRequest(action: Action) =
            when (action) {
                Action.IDENTIFY -> FingerprintIdentifyRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID,
                    DEFAULT_MODULE_ID, DEFAULT_META_DATA, DEFAULT_LANGUAGE, DEFAULT_FINGER_STATUS,
                    DEFAULT_LOGO_EXISTS, DEFAULT_ORGANISATION_NAME, DEFAULT_PROGRAM_NAME,
                    DEFAULT_MATCH_GROUP, DEFAULT_NUMBER_OF_ID_RETURNS)
                Action.VERIFY -> FingerprintVerifyRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID,
                    DEFAULT_MODULE_ID, DEFAULT_META_DATA, DEFAULT_LANGUAGE, DEFAULT_FINGER_STATUS,
                    DEFAULT_LOGO_EXISTS, DEFAULT_PROGRAM_NAME, DEFAULT_ORGANISATION_NAME,
                    DEFAULT_VERIFY_GUID)
                else -> throw UnsupportedOperationException()
            }
    }
}
