package com.simprints.fingerprint.orchestrator.task

import com.simprints.fingerprint.activities.collect.CollectFingerprintsActivity
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.connect.ConnectScannerActivity
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.activities.connect.result.ConnectScannerTaskResult
import com.simprints.fingerprint.activities.matching.MatchingActivity
import com.simprints.fingerprint.activities.matching.request.MatchingTaskRequest
import com.simprints.fingerprint.activities.matching.result.MatchingTaskResult
import com.simprints.fingerprint.controllers.fingerprint.config.ConfigurationTaskRequest
import com.simprints.fingerprint.orchestrator.domain.RequestCode

/**
 * This class represents a fingerprint task to be executed within a flow of tasks to complete a
 * fingerprint request.
 *
 * @property taskResultKey  the string value representing the unique name of the task being run
 * @property createTaskRequest  the closure that generates the task request
 */
sealed class FingerprintTask(
    val taskResultKey: String,
    val createTaskRequest: () -> TaskRequest
) {

    /**
     * This class represents a runnable task that can be executed without the use of the UI, hence
     * no activity or fragments used.
     */
    abstract class RunnableTask(
        taskResultKey: String,
        createTaskRequest: () -> TaskRequest
    ) : FingerprintTask(taskResultKey, createTaskRequest)

    class Configuration(taskResultKey: String, createConfigurationTaskRequest: () -> ConfigurationTaskRequest) :
        RunnableTask(taskResultKey, createConfigurationTaskRequest)

    /**
     * This class represents tasks that require user input, hence the need for activities or
     * fragments to execute these ActivityTasks.
     *
     * @property targetActivity  the java class of the activity to be launched for this task
     * @property requestCode  the request-code used in launching the activity
     * @property requestBundleKey  the bundle-key used in retrieving the task's request value
     * @property resultBundleKey  the bundle-key used in retrieving the task's result value
     */
    abstract class ActivityTask(
        taskResultKey: String,
        createTaskRequest: () -> TaskRequest,
        val targetActivity: Class<*>,
        val requestCode: RequestCode,
        val requestBundleKey: String,
        val resultBundleKey: String
    ) : FingerprintTask(taskResultKey, createTaskRequest)

    class ConnectScanner(taskResultKey: String, createConnectScannerTaskRequest: () -> ConnectScannerTaskRequest) :
        ActivityTask(
            taskResultKey,
            createConnectScannerTaskRequest,
            ConnectScannerActivity::class.java,
            RequestCode.CONNECT,
            ConnectScannerTaskRequest.BUNDLE_KEY,
            ConnectScannerTaskResult.BUNDLE_KEY
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

    class Matching(taskResultKey: String, createMatchingTaskRequest: () -> MatchingTaskRequest) :
        ActivityTask(
            taskResultKey,
            createMatchingTaskRequest,
            MatchingActivity::class.java,
            RequestCode.MATCHING,
            MatchingTaskRequest.BUNDLE_KEY,
            MatchingTaskResult.BUNDLE_KEY
        )
}
