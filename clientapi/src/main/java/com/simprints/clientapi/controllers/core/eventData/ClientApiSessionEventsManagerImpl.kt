package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.controllers.core.eventData.model.fromDomainToCore
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.libsimprints.BuildConfig.VERSION_NAME
import io.reactivex.Completable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent.AlertScreenEventType as CoreAlertScreenEventType


class ClientApiSessionEventsManagerImpl(private val coreSessionEventsManager: SessionEventsManager,
                                        private val timeHelper: ClientApiTimeHelper) :
    ClientApiSessionEventsManager {

    override suspend fun createSession(integration: IntegrationInfo): String =
        suspendCancellableCoroutine { cont ->
            CoroutineScope(Dispatchers.IO).launch {
                coreSessionEventsManager.createSession(VERSION_NAME).flatMap {
                    addIntentParsingEvent(integration).toSingleDefault(it.id)
                }.subscribeBy(
                    onSuccess = { cont.resume(it) },
                    onError = { cont.resumeWithException(it) }
                )
            }
        }

    private fun addIntentParsingEvent(integration: IntegrationInfo): Completable = addEvent(
        IntentParsingEvent(timeHelper.now(), integration.fromDomainToCore())
    )

    override fun addAlertScreenEvent(clientApiAlertType: ClientApiAlert): Completable = addEvent(
        AlertScreenEvent(timeHelper.now(), clientApiAlertType.fromAlertToAlertTypeEvent())
    )

    override suspend fun addSkipCheckEvent(skipValue: Boolean) = suspendCancellableCoroutine<Unit> { cont ->
        try {
            addEvent(SkipCheckEvent(timeHelper.now(), skipValue)).blockingAwait()
            cont.resume(Unit)
        } catch (t: Throwable) {
            cont.resumeWithException(t)
        }
    }

    override fun addSuspiciousIntentEvent(unexpectedExtras: Map<String, Any?>): Completable =
        addEvent(SuspiciousIntentEvent(timeHelper.now(), unexpectedExtras))

    override fun addInvalidIntentEvent(action: String, extras: Map<String, Any?>): Completable =
        addEvent(InvalidIntentEvent(timeHelper.now(), action, extras))

    private fun addEvent(event: Event): Completable =
        coreSessionEventsManager.addEvent(event)

}

fun ClientApiAlert.fromAlertToAlertTypeEvent(): CoreAlertScreenEventType =
    when (this) {
        ClientApiAlert.INVALID_CLIENT_REQUEST -> CoreAlertScreenEventType.INVALID_INTENT_ACTION
        ClientApiAlert.INVALID_METADATA -> CoreAlertScreenEventType.INVALID_METADATA
        ClientApiAlert.INVALID_MODULE_ID -> CoreAlertScreenEventType.INVALID_MODULE_ID
        ClientApiAlert.INVALID_PROJECT_ID -> CoreAlertScreenEventType.INVALID_PROJECT_ID
        ClientApiAlert.INVALID_SELECTED_ID -> CoreAlertScreenEventType.INVALID_SELECTED_ID
        ClientApiAlert.INVALID_SESSION_ID -> CoreAlertScreenEventType.INVALID_SESSION_ID
        ClientApiAlert.INVALID_USER_ID -> CoreAlertScreenEventType.INVALID_USER_ID
        ClientApiAlert.INVALID_VERIFY_ID -> CoreAlertScreenEventType.INVALID_VERIFY_ID
    }
