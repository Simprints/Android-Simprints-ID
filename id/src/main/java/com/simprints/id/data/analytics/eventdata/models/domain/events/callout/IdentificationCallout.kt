package com.simprints.id.data.analytics.eventdata.models.domain.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.callout.ApiIdentificationCallout

@Keep
class IdentificationCallout(val projectId: String,
                            val userId: String,
                            val moduleId: String,
                            val metadata: String?): Callout(CalloutType.IDENTIFICATION)

fun IdentificationCallout.toApiIdentificationCallout() =
    ApiIdentificationCallout(projectId, userId, moduleId, metadata)
