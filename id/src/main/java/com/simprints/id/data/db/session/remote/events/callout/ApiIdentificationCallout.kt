package com.simprints.id.data.db.session.remote.events.callout

import androidx.annotation.Keep

@Keep
class ApiIdentificationCallout(val projectId: String,
                               val userId: String,
                               val moduleId: String,
                               val metadata: String?): ApiCallout(ApiCalloutType.IDENTIFICATION)
