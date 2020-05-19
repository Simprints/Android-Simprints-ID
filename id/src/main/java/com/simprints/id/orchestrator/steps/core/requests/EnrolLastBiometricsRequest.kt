package com.simprints.id.orchestrator.steps.core.requests

import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EnrolLastBiometricsRequest(val projectId: String,
                                      val userId: String,
                                      val moduleId: String,
                                      val fingerprintCaptureResponse: FingerprintCaptureResponse?,
                                      val faceCaptureResponse: FaceCaptureResponse?,
                                      val sessionId: String?) : CoreRequest
