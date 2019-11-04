package com.simprints.fingerprint.orchestrator

import android.app.Activity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.fingerprint.activities.alert.FingerprintAlert.BLUETOOTH_NOT_SUPPORTED
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult.CloseButtonAction.CLOSE
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.connect.result.ConnectScannerTaskResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskResult
import com.simprints.fingerprint.activities.refusal.result.RefusalTaskResult
import com.simprints.fingerprint.commontesttools.generators.FingerprintGenerator
import com.simprints.fingerprint.data.domain.matching.MatchResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintMatchRequest
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.orchestrator.task.FingerprintTask.*
import com.simprints.moduleapi.fingerprint.responses.*
import com.simprints.testtools.common.syntax.failTest
import com.simprints.testtools.common.syntax.mock
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
    fun matchingTaskFlow_allResultsOk_shouldFinishSuccessfully() {
        with(Orchestrator(FinalResultBuilder())) {
            start(createFingerprintMatchRequest())
            assertNextTaskIs<Matching>()
            okMatchingResult()
            assertTrue(isFinished())
            with(getFinalResult()) {
                assertEquals(Activity.RESULT_OK, resultCode)
                assertNotNull(resultData?.extras?.getParcelable<IFingerprintMatchResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                    assertEquals(IFingerprintResponseType.MATCH, type)
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
            start(createFingerprintCaptureRequest())
            assertNextTaskIs<ConnectScanner>()
            okConnectResult()
            getState()
        }

        with(Orchestrator(FinalResultBuilder())) {
            start(createFingerprintCaptureRequest())
            restoreState(state ?: failTest("Orchestrator state is null"))
            assertNextTaskIs<CollectFingerprints>()
            okCollectResult()
            with(getFinalResult()) {
                assertEquals(Activity.RESULT_OK, resultCode)
                assertNotNull(resultData?.extras?.getParcelable<IFingerprintCaptureResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                    assertEquals(IFingerprintResponseType.CAPTURE, type)
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
            CollectFingerprintsTaskResult(FingerprintGenerator.generateRandomFingerprints(2))
        }
    }

    private fun Orchestrator.okMatchingResult() {
        handleActivityTaskResult(ResultCode.OK) { key ->
            assertEquals(MatchingTaskResult.BUNDLE_KEY, key)
            MatchingTaskResult(List(10) {
                val score = Rand.nextInt(100).toFloat()
                MatchResult(UUID.randomUUID().toString(), score)
            }.sortedByDescending { it.confidence })
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
        private val DEFAULT_FINGERS_TO_CAPTURE = listOf(
            FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.LEFT_INDEX_FINGER
        )

        private fun createFingerprintCaptureRequest() =
            FingerprintCaptureRequest(DEFAULT_FINGERS_TO_CAPTURE)

        private fun createFingerprintMatchRequest() =
            FingerprintMatchRequest(listOf(mock()), mock())
    }
}
