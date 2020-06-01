package com.simprints.fingerprint.activities.orchestrator

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.activities.connect.result.ConnectScannerTaskResult
import com.simprints.fingerprint.di.KoinInjector.acquireFingerprintKoinModules
import com.simprints.fingerprint.di.KoinInjector.releaseFingerprintKoinModules
import com.simprints.fingerprint.integration.createFingerprintCaptureRequestIntent
import com.simprints.fingerprint.orchestrator.Orchestrator
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.fingerprint.orchestrator.state.FingerprintTaskFlowState
import com.simprints.fingerprint.orchestrator.state.OrchestratorState
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.scanner.data.worker.FirmwareFileUpdateScheduler
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.test.KoinTest
import org.koin.test.mock.declareModule

@RunWith(AndroidJUnit4::class)
class OrchestratorActivityAndroidTest : KoinTest {

    private val orchestratorMock = mockk<Orchestrator>(relaxed = true)
    private val firmwareFileUpdateSchedulerMock = mockk<FirmwareFileUpdateScheduler>(relaxed = true)
    private val scannerManagerMock = spyk<ScannerManager>(ScannerManagerImpl(mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true)))
    private val orchestratorViewModel = spyk(OrchestratorViewModel(orchestratorMock, scannerManagerMock, firmwareFileUpdateSchedulerMock))

    private lateinit var scenario: ActivityScenario<OrchestratorActivity>

    @Before
    fun setUp() {
        acquireFingerprintKoinModules()
        Intents.init()

        declareModule {
            factory { orchestratorMock }
            viewModel { orchestratorViewModel }
        }
    }

    @Test
    fun orchestratorActivityCallsNextActivity_returnsWithResult_handlesActivityResult() {
        every { orchestratorMock.isFinished() } returns false
        every { orchestratorMock.getNextTask() } returns FingerprintTask.ConnectScanner("connect") {
            launchTaskRequest()
        }

        intending(hasExtraWithKey(ConnectScannerTaskRequest.BUNDLE_KEY))
            .respondWith(Instrumentation.ActivityResult(ResultCode.OK.value,
                Intent().putExtra(ConnectScannerTaskResult.BUNDLE_KEY, ConnectScannerTaskResult())))

        scenario = ActivityScenario.launch(createFingerprintCaptureRequestIntent())

        every { orchestratorMock.isFinished() } returns true
        every { orchestratorMock.getFinalResult() } returns
            FinalResult(Activity.RESULT_OK, Intent().putExtra("test_key", 42))
        assertNotNull(scenario.result.resultData.extras?.get("test_key") as Int?)

        verify { orchestratorMock.handleActivityTaskResult(any(), any()) }
    }

    @Test
    fun orchestratorActivityWithFinishedOrchestrator_getsFinalResultAndFinishes() {
        every { orchestratorMock.isFinished() } returns true
        every { orchestratorMock.getFinalResult() } returns
            FinalResult(Activity.RESULT_OK, Intent().putExtra("test_key", 42))

        scenario = ActivityScenario.launch(createFingerprintCaptureRequestIntent())

        assertNotNull(scenario.result.resultData.extras?.get("test_key") as Int?)

        verify(exactly = 0) { orchestratorMock.handleActivityTaskResult(any(), any()) }
        verify { orchestratorMock.getFinalResult() }
    }

    @Test
    fun orchestratorActivity_destroyedBeneathActivity_resumesProperly() {
        val orchestratorState = OrchestratorState(FingerprintTaskFlowState(
            mockk(relaxed = true),
            2,
            mutableMapOf("connect" to mockk(relaxed = true), "collect" to mockk(relaxed = true))
        ))

        every { orchestratorMock.getState() } returns orchestratorState

        // Make sure other activities don't start appearing
        every { orchestratorViewModel.start(any()) } returns Unit

        scenario = ActivityScenario.launch(createFingerprintCaptureRequestIntent())

        scenario.recreate()

        verify { orchestratorMock.getState() }
        verify { orchestratorMock.restoreState(eq(orchestratorState)) } }

    @After
    fun tearDown() {
        Intents.release()
        if (::scenario.isInitialized) scenario.close()
        releaseFingerprintKoinModules()
    }

    companion object {
        private fun launchTaskRequest() = ConnectScannerTaskRequest(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
    }
}
