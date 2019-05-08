package com.simprints.id.data.analytics.eventdata.models.domain.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.callout.ApiEnrolmentCallout

@Keep
class EnrolmentCallout(val projectId: String,
                       val userId: String,
                       val moduleId: String,
                       val metadata: String?): Callout(CalloutType.ENROLMENT)

fun EnrolmentCallout.toApiEnrolmentCallout() =
    ApiEnrolmentCallout(projectId, userId, moduleId, metadata)
