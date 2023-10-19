package com.simprints.fingerprint.orchestrator.task

import android.os.Parcelable
import androidx.core.os.bundleOf
import com.simprints.fingerprint.activities.collect.CollectFingerprintsActivity
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.connect.result.ConnectScannerTaskResult
import com.simprints.fingerprint.connect.FingerprintConnectContract
import com.simprints.fingerprint.connect.FingerprintConnectParams
import com.simprints.fingerprint.connect.screens.ConnectScannerWrapperActivity
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
    open val taskResultKey: String,
    open val resultBundleKey: String
) {

    data class ConnectScanner(
        override val taskResultKey: String,
        override val resultBundleKey: String = ConnectScannerTaskResult.BUNDLE_KEY,
    ) : FingerprintTask(taskResultKey, resultBundleKey)

    data class CollectFingerprints(
        override val taskResultKey: String,
        val createTaskRequest: () -> Parcelable,
        val targetActivity: Class<*> = CollectFingerprintsActivity::class.java,
        val requestCode: RequestCode = RequestCode.COLLECT,
        val requestBundleKey: String = CollectFingerprintsTaskRequest.BUNDLE_KEY,
        override val resultBundleKey: String = CollectFingerprintsTaskResult.BUNDLE_KEY,
    ) : FingerprintTask(taskResultKey, resultBundleKey)

}
