package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class VerificationCallout(val integration: String,
                          val projectId: String,
                          val userId: String,
                          val moduleId: String,
                          val verifyGuid: String,
                          val metadata: String): Callout(CalloutType.VERIFICATION)
