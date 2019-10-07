package com.simprints.id.domain.moduleapi.fingerprint

import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintRequest

interface FingerprintRequestFactory {

    fun buildFingerprintCaptureRequest(projectId: String,
                                       userId: String,
                                       moduleId: String,
                                       metadata: String,
                                       prefs: PreferencesManager): FingerprintCaptureRequest

    fun buildFingerprintMatchRequest(projectId: String,
                                      userId: String,
                                      moduleId: String,
                                      metadata: String,
                                      verifyGuid: String,
                                      prefs: PreferencesManager): FingerprintMatchingRequest

    fun buildFingerprintIdentifyRequest(projectId: String,
                                        userId: String,
                                        moduleId: String,
                                        metadata: String,
                                        prefs: PreferencesManager,
                                        returnIdCount: Int = 10): FingerprintIdentifyRequest
}

fun FingerprintRequestFactory.buildFingerprintRequestFromAppRequest(appRequest: AppRequest, prefs: PreferencesManager): FingerprintRequest =
    when (appRequest) {
        is AppEnrolRequest -> with(appRequest) {
            buildFingerprintCaptureRequest(projectId, userId, moduleId, metadata, prefs)
        }
        is AppVerifyRequest -> with(appRequest) {
            buildFingerprintVerifyRequest(projectId, userId, moduleId, metadata, verifyGuid, prefs)
        }
        is AppIdentifyRequest -> with(appRequest) {
            buildFingerprintIdentifyRequest(projectId, userId, moduleId, metadata, prefs)
        }
        else -> throw IllegalStateException("Invalid fingerprint request")
    }

