package com.simprints.id.data.analytics.eventdata.models.domain.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.callout.ApiVerificationCallout

@Keep
class VerificationCallout(val projectId: String,
                          val userId: String,
                          val moduleId: String,
                          val verifyGuid: String,
                          val metadata: String): Callout(CalloutType.VERIFICATION)

fun VerificationCallout.toApiVerificationCallout() =
    ApiVerificationCallout(projectId, userId, moduleId, verifyGuid, metadata)
