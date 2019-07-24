package com.simprints.fingerprint.orchestrator

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.launch.result.LaunchTaskResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskIdentifyResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskVerifyResult
import com.simprints.fingerprint.activities.orchestrator.OrchestratorViewModel
import com.simprints.fingerprint.commontesttools.generators.PeopleGeneratorUtils
import com.simprints.fingerprint.data.domain.matching.MatchingResult
import com.simprints.fingerprint.data.domain.matching.MatchingTier
import com.simprints.fingerprint.orchestrator.task.FingerprintTask.*
import com.simprints.fingerprint.orchestrator.task.ResultCode
import com.simprints.fingerprint.tasks.saveperson.SavePersonTaskResult
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.runner.RunWith
import java.util.*
import kotlin.random.Random as Rand

@RunWith(AndroidJUnit4::class)
class OrchestratorTest {

    private lateinit var orchestrator: Orchestrator

    private val viewModelMock: OrchestratorViewModel = mock()
    private val runnableTaskDispatcherMock: RunnableTaskDispatcher = mock()

    @Before
    fun setup() {
        orchestrator = Orchestrator(viewModelMock)
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
}
