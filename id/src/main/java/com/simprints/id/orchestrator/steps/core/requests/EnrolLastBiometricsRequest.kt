package com.simprints.id.orchestrator.steps.core.requests

import kotlinx.android.parcel.Parcelize

@Parcelize
data class EnrolLastBiometricsRequest(val projectId: String,
                                      val userId: String,
                                      val moduleId: String,
                                      val metadata: String,
                                      val sessionId: String?) : CoreRequest
