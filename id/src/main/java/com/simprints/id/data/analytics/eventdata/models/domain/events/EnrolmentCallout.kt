package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiEnrolmentCallout

@Keep
class EnrolmentCallout(val integration: String,
                       val projectId: String,
                       val userId: String,
                       val moduleId: String,
                       val metadata: String?): Callout(CalloutType.ENROLMENT)

fun EnrolmentCallout.toApiEnrolmentCallout() =
    ApiEnrolmentCallout(integration, projectId, userId, moduleId, metadata)
