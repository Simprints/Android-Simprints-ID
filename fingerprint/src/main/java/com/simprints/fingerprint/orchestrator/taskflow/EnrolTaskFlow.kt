package com.simprints.fingerprint.orchestrator.taskflow

import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.launch.request.LaunchTaskRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.toAction
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.tasks.saveperson.SavePersonTaskRequest

class EnrolTaskFlow(fingerprintRequest: FingerprintRequest) : FingerprintTaskFlow(fingerprintRequest) {

    init {
        with(fingerprintRequest) {
            fingerprintTasks = listOf(
                FingerprintTask.Launch(LAUNCH) { createLaunchTaskRequest() },
                FingerprintTask.CollectFingerprints(COLLECT) { createCollectFingerprintsTaskRequest() },
                FingerprintTask.SavePerson(SAVE) { createSavePersonTaskRequest() }
            )
        }
    }

    private fun FingerprintRequest.createLaunchTaskRequest() =
        LaunchTaskRequest(
            projectId, this.toAction(), language, logoExists, programName, organizationName
        )

    private fun FingerprintRequest.createCollectFingerprintsTaskRequest() =
        CollectFingerprintsTaskRequest(
            projectId, userId, moduleId, this.toAction(), language, fingerStatus
        )

    private fun createSavePersonTaskRequest() =
        with(taskResults[COLLECT] as CollectFingerprintsTaskResult) {
            SavePersonTaskRequest(
                probe
            )
        }

    override fun getFinalOkResult(finalResultBuilder: FinalResultBuilder): FinalResult =
        finalResultBuilder.createEnrolResult(taskResults[COLLECT] as CollectFingerprintsTaskResult)

    companion object {
        private const val LAUNCH = "launch"
        private const val COLLECT = "collect"
        private const val SAVE = "save"
    }
}
