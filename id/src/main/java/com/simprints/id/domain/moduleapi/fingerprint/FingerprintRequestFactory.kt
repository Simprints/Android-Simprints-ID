package com.simprints.id.domain.moduleapi.fingerprint

import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest

interface FingerprintRequestFactory {

    fun buildFingerprintEnrolRequest(projectId: String,
                                     userId: String,
                                     moduleId: String,
                                     metadata: String,
                                     prefs: PreferencesManager): FingerprintEnrolRequest

    fun buildFingerprintVerifyRequest(projectId: String,
                                      userId: String,
                                      moduleId: String,
                                      metadata: String,
                                      verifyGuid: String,
                                      prefs: PreferencesManager): FingerprintVerifyRequest

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
            buildFingerprintEnrolRequest(projectId, userId, moduleId, metadata, prefs)
        }
        is AppVerifyRequest -> with(appRequest) {
            buildFingerprintVerifyRequest(projectId, userId, moduleId, metadata, verifyGuid, prefs)
        }
        is AppIdentifyRequest -> with(appRequest) {
            buildFingerprintIdentifyRequest(projectId, userId, moduleId, metadata, prefs)
        }
        else -> throw IllegalStateException("Invalid fingerprint request")
    }

