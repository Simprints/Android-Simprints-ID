package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class ConfirmationCallout(val selectedGuid: String,
                          val sessionId: String): Callout(CalloutType.CONFIRMATION)
