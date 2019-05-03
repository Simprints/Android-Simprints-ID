package com.simprints.clientapi.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConfirmationCallout as CoreConfirmationCallout

@Keep
class ConfirmationCallout(val selectedGuid: String, val sessionId: String): Callout(CalloutType.CONFIRMATION)

fun ConfirmationCallout.fromDomainToCore(): CoreConfirmationCallout = CoreConfirmationCallout(selectedGuid, sessionId)
