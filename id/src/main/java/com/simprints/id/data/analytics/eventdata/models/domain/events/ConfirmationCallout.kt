package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiConfirmationCallout

@Keep
class ConfirmationCallout(val selectedGuid: String,
                          val sessionId: String): Callout(CalloutType.CONFIRMATION)

fun ConfirmationCallout.toApiConfirmationCallout() =
    ApiConfirmationCallout(selectedGuid, sessionId)
