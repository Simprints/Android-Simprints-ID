package com.simprints.id.orchestrator.steps.core.requests

import com.simprints.id.orchestrator.steps.Step
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EnrolLastBiometricsRequest(val projectId: String,
                                      val userId: String,
                                      val moduleId: String,
                                      val previousSteps: List<Step>,
                                      val sessionId: String?) : CoreRequest
