package com.simprints.id.data.db.event.remote.models.callout

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiEnrolmentLastBiometricsCallout(val projectId: String,
                                        val userId: String,
                                        val moduleId: String,
                                        val metadata: String?,
                                        val sessionId: String) : ApiCallout(ApiCalloutType.EnrolmentLastBiometrics)
