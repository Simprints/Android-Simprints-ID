package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import io.reactivex.Completable


interface ClientApiSessionEventsManager {

    suspend fun createSession(integration: IntegrationInfo): String

    suspend fun addCompletionCheckEvent(complete: Boolean)

    fun addInvalidIntentEvent(action: String, extras: Map<String, Any?>): Completable

    fun addAlertScreenEvent(clientApiAlertType: ClientApiAlert): Completable

    fun addSuspiciousIntentEvent(unexpectedExtras: Map<String, Any?>): Completable

    suspend fun getCurrentSessionId(): String?
}
