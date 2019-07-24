package com.simprints.fingerprint.orchestrator.task

import com.simprints.fingerprint.activities.collect.CollectFingerprintsActivity
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.launch.LaunchActivity
import com.simprints.fingerprint.activities.launch.request.LaunchTaskRequest
import com.simprints.fingerprint.activities.launch.result.LaunchTaskResult
import com.simprints.fingerprint.activities.matching.MatchingActivity
import com.simprints.fingerprint.activities.matching.request.MatchingTaskRequest
import com.simprints.fingerprint.activities.matching.result.MatchingTaskResult
import com.simprints.fingerprint.tasks.saveperson.SavePersonTask
import com.simprints.fingerprint.tasks.saveperson.SavePersonTaskRequest

sealed class FingerprintTask(
    val taskResultKey: String,
    val createTaskRequest: () -> TaskRequest
) {

    abstract class RunnableTask(
        taskResultKey: String,
        createTaskRequest: () -> TaskRequest
    ) : FingerprintTask(taskResultKey, createTaskRequest) {

        abstract fun runTask(taskRequest: TaskRequest): TaskResult
    }

    class SavePerson(savePersonResultKey: String, createSavePersonTaskRequest: () -> SavePersonTaskRequest) :
        RunnableTask(savePersonResultKey, createSavePersonTaskRequest) {

        override fun runTask(taskRequest: TaskRequest): TaskResult =
            SavePersonTask(taskRequest as SavePersonTaskRequest).savePerson()
    }

    abstract class ActivityTask(
        taskResultKey: String,
        createTaskRequest: () -> TaskRequest,
        val targetActivity: Class<*>,
        val requestCode: RequestCode,
        val requestBundleKey: String,
        val resultBundleKey: String
    ) : FingerprintTask(taskResultKey, createTaskRequest)

    class Launch(taskResultKey: String, createLaunchTaskRequest: () -> LaunchTaskRequest) :
        ActivityTask(
            taskResultKey,
            createLaunchTaskRequest,
            LaunchActivity::class.java,
            RequestCode.LAUNCH,
            LaunchTaskRequest.BUNDLE_KEY,
            LaunchTaskResult.BUNDLE_KEY
        )

    class CollectFingerprints(taskResultKey: String, createCollectFingerprintsTaskRequest: () -> CollectFingerprintsTaskRequest) :
        ActivityTask(
            taskResultKey,
            createCollectFingerprintsTaskRequest,
            CollectFingerprintsActivity::class.java,
            RequestCode.COLLECT,
            CollectFingerprintsTaskRequest.BUNDLE_KEY,
            CollectFingerprintsTaskResult.BUNDLE_KEY
        )

    class Matching(taskResultKey: String, val subAction: SubAction, createMatchingTaskRequest: () -> MatchingTaskRequest) :
        ActivityTask(
            taskResultKey,
            createMatchingTaskRequest,
            MatchingActivity::class.java,
            RequestCode.MATCHING,
            MatchingTaskRequest.BUNDLE_KEY,
            MatchingTaskResult.BUNDLE_KEY
        ) {
        enum class SubAction {
            IDENTIFY, VERIFY
        }
    }
}
