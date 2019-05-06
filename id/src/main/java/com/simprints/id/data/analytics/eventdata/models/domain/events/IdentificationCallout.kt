package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiIdentificationCallout

@Keep
class IdentificationCallout(val integration: String,
                            val projectId: String,
                            val userId: String,
                            val moduleId: String,
                            val metadata: String?): Callout(CalloutType.IDENTIFICATION)

fun IdentificationCallout.toApiIdentificationCallout() =
    ApiIdentificationCallout(integration, projectId, userId, moduleId, metadata)
