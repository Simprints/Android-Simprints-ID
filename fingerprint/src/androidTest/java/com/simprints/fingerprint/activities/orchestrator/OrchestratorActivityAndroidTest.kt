package com.simprints.fingerprint.activities.orchestrator

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.simprints.fingerprint.activities.launch.request.LaunchTaskRequest
import com.simprints.fingerprint.activities.launch.result.LaunchTaskResult
import com.simprints.fingerprint.data.domain.Action
import com.simprints.fingerprint.di.KoinInjector.acquireFingerprintKoinModules
import com.simprints.fingerprint.di.KoinInjector.releaseFingerprintKoinModules
import com.simprints.fingerprint.integration.createFingerprintRequestIntent
import com.simprints.fingerprint.orchestrator.Orchestrator
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.fingerprint.orchestrator.state.FingerprintTaskFlowState
import com.simprints.fingerprint.orchestrator.state.OrchestratorState
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.testtools.common.syntax.*
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.test.KoinTest
import org.koin.test.mock.declare

@RunWith(AndroidJUnit4::class)
class OrchestratorActivityAndroidTest : KoinTest {

    private val orchestratorMock = mock<Orchestrator>()
    private val orchestratorViewModel = spy(OrchestratorViewModel(orchestratorMock, mock()))

    private lateinit var scenario: ActivityScenario<OrchestratorActivity>

    @Before
    fun setUp() {
        acquireFingerprintKoinModules()
        Intents.init()

        declare {
            factory { orchestratorMock }
            viewModel { orchestratorViewModel }
        }
    }

    @Test
    fun orchestratorActivityCallsNextActivity_returnsWithResult_handlesActivityResult() {
        whenever(orchestratorMock) { isFinished() } thenReturn false
        whenever(orchestratorMock) { getNextTask() } thenReturn FingerprintTask.Launch("launch") {
            launchTaskRequest(Action.IDENTIFY)
        }

        intending(hasExtraWithKey(LaunchTaskRequest.BUNDLE_KEY))
            .respondWith(Instrumentation.ActivityResult(ResultCode.OK.value,
                Intent().putExtra(LaunchTaskResult.BUNDLE_KEY, LaunchTaskResult())))

        scenario = ActivityScenario.launch(createFingerprintRequestIntent(Action.IDENTIFY))

        whenever(orchestratorMock) { isFinished() } thenReturn true
        whenever(orchestratorMock) { getFinalResult() } thenReturn
            FinalResult(Activity.RESULT_OK, Intent().putExtra("test_key", 42))
        assertNotNull(scenario.result.resultData.extras?.get("test_key") as Int?)

        verifyOnce(orchestratorMock) { handleActivityTaskResult(anyNotNull(), anyNotNull()) }
    }

    @Test
    fun orchestratorActivityWithFinishedOrchestrator_getsFinalResultAndFinishes() {
        whenever(orchestratorMock) { isFinished() } thenReturn true
        whenever(orchestratorMock) { getFinalResult() } thenReturn
            FinalResult(Activity.RESULT_OK, Intent().putExtra("test_key", 42))

        scenario = ActivityScenario.launch(createFingerprintRequestIntent(Action.IDENTIFY))

        assertNotNull(scenario.result.resultData.extras?.get("test_key") as Int?)

        verifyNever(orchestratorMock) { handleActivityTaskResult(anyNotNull(), anyNotNull()) }
        verifyOnce(orchestratorMock) { getFinalResult() }
    }

    @Test
    fun orchestratorActivity_destroyedBeneathActivity_resumesProperly() {
        val orchestratorState = OrchestratorState(FingerprintTaskFlowState(
            mock(),
            2,
            mutableMapOf("launch" to mock(), "collect" to mock())
        ))

        whenever(orchestratorMock) { getState() } thenReturn orchestratorState

        // Make sure other activities don't start appearing
        whenever(orchestratorViewModel) { start(anyNotNull()) } thenDoNothing {}

        scenario = ActivityScenario.launch(createFingerprintRequestIntent(Action.IDENTIFY))

        scenario.recreate()

        verifyOnce(orchestratorMock) { getState() }
        verifyOnce(orchestratorMock) { restoreState(eq(orchestratorState)) }
        verifyNoMoreInteractions(orchestratorMock)
    }

    @After
    fun tearDown() {
        Intents.release()
        if (::scenario.isInitialized) scenario.close()
        releaseFingerprintKoinModules()
    }

    companion object {
        private const val DEFAULT_PROJECT_ID = "some_project_id"
        private const val DEFAULT_LANGUAGE = "en"
        private const val DEFAULT_LOGO_EXISTS = true
        private const val DEFAULT_PROGRAM_NAME = "This program"
        private const val DEFAULT_ORGANISATION_NAME = "This organisation"
        private const val DEFAULT_VERIFY_GUID = "verify_guid"

        private fun launchTaskRequest(action: Action) = LaunchTaskRequest(
            DEFAULT_PROJECT_ID, action, DEFAULT_LANGUAGE, DEFAULT_LOGO_EXISTS, DEFAULT_PROGRAM_NAME,
            DEFAULT_ORGANISATION_NAME, if (action == Action.VERIFY) DEFAULT_VERIFY_GUID else null
        )
    }
}
