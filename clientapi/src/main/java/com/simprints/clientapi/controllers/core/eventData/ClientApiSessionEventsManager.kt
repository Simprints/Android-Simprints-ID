package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo


interface ClientApiSessionEventsManager {

    suspend fun createSession(integration: IntegrationInfo): String

    suspend fun addCompletionCheckEvent(complete: Boolean)

    suspend fun addInvalidIntentEvent(action: String, extras: Map<String, Any?>)

    suspend fun addAlertScreenEvent(clientApiAlertType: ClientApiAlert)

    suspend fun addSuspiciousIntentEvent(unexpectedExtras: Map<String, Any?>)

    suspend fun getCurrentSessionId(): String?
}
