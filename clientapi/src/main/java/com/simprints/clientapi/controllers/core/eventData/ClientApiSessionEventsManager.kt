package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.eventData.model.IntentAction
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import io.reactivex.Completable
import io.reactivex.Single


interface ClientApiSessionEventsManager {

    fun createSession(integration: IntegrationInfo): Single<String>

    fun addInvalidIntentEvent(action: IntentAction, extras: Map<String, Any?>): Completable

    fun addAlertScreenEvent(clientApiAlertType: ClientApiAlert): Completable

    fun addSuspiciousIntentEvent(unexpectedExtras: Map<String, Any?>): Completable

}
