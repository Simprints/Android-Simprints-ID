package com.simprints.id.data.db.event.remote.models.callout

import androidx.annotation.Keep

@Keep
data class ApiEnrolmentLastBiometricsCallout(val projectId: String,
                                        val userId: String,
                                        val moduleId: String,
                                        val metadata: String?,
                                        val sessionId: String) : ApiCallout(ApiCalloutType.EnrolmentLastBiometrics)
