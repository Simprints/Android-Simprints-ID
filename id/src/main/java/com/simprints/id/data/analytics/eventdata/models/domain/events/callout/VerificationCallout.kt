package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiVerificationCallout

@Keep
class VerificationCallout(val integration: String,
                          val projectId: String,
                          val userId: String,
                          val moduleId: String,
                          val verifyGuid: String,
                          val metadata: String): Callout(CalloutType.VERIFICATION)

fun VerificationCallout.toApiVerificationCallout() =
    ApiVerificationCallout(integration, projectId, userId, moduleId, verifyGuid, metadata)
