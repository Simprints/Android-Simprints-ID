package com.simprints.id.data.analytics.eventdata.models.domain.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.callout.ApiConfirmationCallout

@Keep
class ConfirmationCallout(val selectedGuid: String,
                          val sessionId: String): Callout(CalloutType.CONFIRMATION)

fun ConfirmationCallout.toApiConfirmationCallout() =
    ApiConfirmationCallout(selectedGuid, sessionId)
