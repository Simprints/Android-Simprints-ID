package com.simprints.infra.events.remote.models.callout

import androidx.annotation.Keep

@Keep
data class ApiEnrolmentCallout(val projectId: String,
                          val userId: String,
                          val moduleId: String,
                          val metadata: String?): ApiCallout(ApiCalloutType.Enrolment)
