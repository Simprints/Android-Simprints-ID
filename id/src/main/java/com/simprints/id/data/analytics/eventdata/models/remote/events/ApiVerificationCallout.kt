package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep

@Keep
class ApiVerificationCallout(val integration: String,
                             val projectId: String,
                             val userId: String,
                             val moduleId: String,
                             val verifyGuid: String,
                             val metadata: String): ApiCallout(ApiCalloutType.VERIFICATION)
