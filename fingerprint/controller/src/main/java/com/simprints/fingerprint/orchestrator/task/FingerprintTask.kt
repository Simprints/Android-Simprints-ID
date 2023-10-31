package com.simprints.fingerprint.orchestrator.task

import com.simprints.fingerprint.activities.collect.CollectFingerprintsActivity
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.connect.ConnectScannerActivity
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.activities.connect.result.ConnectScannerTaskResult
import com.simprints.fingerprint.orchestrator.domain.RequestCode

/**
 * This class represents tasks that require user input, hence the need for activities or
 * fragments to execute these ActivityTasks.
 *
 * @property taskResultKey  the string value representing the unique name of the task being run
 * @property createTaskRequest  the closure that generates the task request
 * @property targetActivity  the java class of the activity to be launched for this task
 * @property requestCode  the request-code used in launching the activity
 * @property requestBundleKey  the bundle-key used in retrieving the task's request value
 * @property resultBundleKey  the bundle-key used in retrieving the task's result value
 */
sealed class FingerprintTask(
    val taskResultKey: String,
    val createTaskRequest: () -> TaskRequest,
    val targetActivity: Class<*>,
    val requestCode: RequestCode,
    val requestBundleKey: String,
    val resultBundleKey: String
) {

    class ConnectScanner(taskResultKey: String, createConnectScannerTaskRequest: () -> ConnectScannerTaskRequest) :
        FingerprintTask(
            taskResultKey,
            createConnectScannerTaskRequest,
            ConnectScannerActivity::class.java,
            RequestCode.CONNECT,
            ConnectScannerTaskRequest.BUNDLE_KEY,
            ConnectScannerTaskResult.BUNDLE_KEY
        )

    class CollectFingerprints(taskResultKey: String, createCollectFingerprintsTaskRequest: () -> CollectFingerprintsTaskRequest) :
        FingerprintTask(
            taskResultKey,
            createCollectFingerprintsTaskRequest,
            CollectFingerprintsActivity::class.java,
            RequestCode.COLLECT,
            CollectFingerprintsTaskRequest.BUNDLE_KEY,
            CollectFingerprintsTaskResult.BUNDLE_KEY
        )

}
