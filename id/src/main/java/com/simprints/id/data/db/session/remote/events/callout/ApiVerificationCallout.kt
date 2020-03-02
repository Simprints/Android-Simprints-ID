package com.simprints.id.data.db.session.remote.events.callout

import androidx.annotation.Keep

@Keep
class ApiVerificationCallout(val projectId: String,
                             val userId: String,
                             val moduleId: String,
                             val metadata: String,
                             val verifyGuid: String): ApiCallout(ApiCalloutType.VERIFICATION)
