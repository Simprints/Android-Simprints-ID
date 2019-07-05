package com.simprints.fingerprint.orchestrator

import com.simprints.fingerprint.activities.ActRequest
import com.simprints.fingerprint.activities.collect.CollectFingerprintsActivity
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsActRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsActResult
import com.simprints.fingerprint.activities.launch.LaunchActivity
import com.simprints.fingerprint.activities.launch.request.LaunchActRequest
import com.simprints.fingerprint.activities.launch.result.LaunchActResult
import com.simprints.fingerprint.activities.matching.MatchingActivity
import com.simprints.fingerprint.activities.matching.request.MatchingActRequest
import com.simprints.fingerprint.activities.matching.result.MatchingActResult

sealed class ActivityTask(
    val createActRequest: () -> ActRequest,
    val actResultKey: String,
    val targetClass: Class<*>,
    val requestCode: Int,
    val requestBundleKey: String,
    val resultBundleKey: String
)

class Launch(createLaunchActRequest: () -> LaunchActRequest, actResultKey: String) :
    ActivityTask(
        createLaunchActRequest,
        actResultKey,
        LaunchActivity::class.java,
        0, // TODO request codes
        LaunchActRequest.BUNDLE_KEY,
        LaunchActResult.BUNDLE_KEY
    )

class CollectFingerprints(createCollectFingerprintsActRequest: () -> CollectFingerprintsActRequest,
                          actResultKey: String) :
    ActivityTask(
        createCollectFingerprintsActRequest,
        actResultKey,
        CollectFingerprintsActivity::class.java,
        1,
        CollectFingerprintsActRequest.BUNDLE_KEY,
        CollectFingerprintsActResult.BUNDLE_KEY
    )

class Matching(createMatchingActRequest: () -> MatchingActRequest, actResultKey: String) :
    ActivityTask(
        createMatchingActRequest,
        actResultKey,
        MatchingActivity::class.java,
        2,
        MatchingActRequest.BUNDLE_KEY,
        MatchingActResult.BUNDLE_KEY
    )
