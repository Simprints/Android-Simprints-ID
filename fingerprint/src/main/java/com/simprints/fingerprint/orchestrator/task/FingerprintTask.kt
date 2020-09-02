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

sealed class FingerprintTask(
    val taskResultKey: String,
    val createTaskRequest: () -> TaskRequest
) {

    abstract class RunnableTask(
        taskResultKey: String,
        createTaskRequest: () -> TaskRequest
    ) : FingerprintTask(taskResultKey, createTaskRequest)

    class Configuration(taskResultKey: String, createConfigurationTaskRequest: () -> ConfigurationTaskRequest) :
        RunnableTask(taskResultKey, createConfigurationTaskRequest)

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
