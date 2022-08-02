package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.eventsystem.event.domain.models.Event
import kotlinx.coroutines.flow.Flow


interface ClientApiSessionEventsManager {

    suspend fun createSession(integration: IntegrationInfo): String

    suspend fun addCompletionCheckEvent(complete: Boolean)

    suspend fun addInvalidIntentEvent(action: String, extras: Map<String, Any?>)

    suspend fun addAlertScreenEvent(clientApiAlertType: ClientApiAlert)

    suspend fun addSuspiciousIntentEvent(unexpectedExtras: Map<String, Any?>)

    suspend fun getCurrentSessionId(): String

    suspend fun isCurrentSessionAnIdentificationOrEnrolment(): Boolean

    suspend fun isSessionHasIdentificationCallback(sessionId: String): Boolean

    suspend fun getAllEventsForSession(sessionId: String): Flow<Event>

    suspend fun deleteSessionEvents(sessionId: String)

    suspend fun closeCurrentSessionNormally()
}
