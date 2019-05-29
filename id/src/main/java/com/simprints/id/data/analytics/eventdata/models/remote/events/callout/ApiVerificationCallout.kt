package com.simprints.id.data.analytics.eventdata.models.remote.events.callout

import androidx.annotation.Keep

@Keep
class ApiVerificationCallout(val projectId: String,
                             val userId: String,
                             val moduleId: String,
                             val metadata: String,
                             val verifyGuid: String): ApiCallout(ApiCalloutType.VERIFICATION)
