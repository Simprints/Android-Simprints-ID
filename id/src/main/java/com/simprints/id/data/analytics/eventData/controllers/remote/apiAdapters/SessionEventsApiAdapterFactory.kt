package com.simprints.id.data.analytics.eventData.controllers.remote.apiAdapters

import com.google.gson.GsonBuilder
import com.simprints.id.data.analytics.eventData.models.domain.events.*
import com.simprints.id.data.analytics.eventData.models.domain.session.DatabaseInfo
import com.simprints.id.data.analytics.eventData.models.domain.session.Device
import com.simprints.id.data.analytics.eventData.models.domain.session.Location
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents


class SessionEventsApiAdapterFactory {

    val gson by lazy {
        val builder = GsonBuilder()
        with(builder) {

            //Event Adapters
            registerAdapterForEvent<AlertScreenEvent>()
            registerAdapterForEvent<ArtificialTerminationEvent>()
            registerAdapterForEvent<AuthenticationEvent>()
            registerAdapterForEvent<AuthorizationEvent>()
            registerAdapterForEvent<CallbackEvent>()
            registerAdapterForEvent<CalloutEvent>()
            registerAdapterForEvent<CandidateReadEvent>()
            registerAdapterForEvent<ConnectivitySnapshotEvent>()
            registerAdapterForEvent<ConsentEvent>()
            registerAdapterForEvent<EnrollmentEvent>()
            //nothing for FingerprintCaptureEvent - we want the event.id
            registerAdapterForEvent<GuidSelectionEvent>()

            registerAdapterForEvent<OneToManyMatchEvent>()
            registerAdapterForEvent<OneToOneMatchEvent>()
            registerAdapterForEvent<PersonCreationEvent>()
            registerAdapterForEvent<RefusalEvent>()
            registerAdapterForEvent<ScannerConnectionEvent>()

            //Session Info Adapters
            registerAdapterForSessionInfo<DatabaseInfo>()
            registerAdapterForSessionInfo<Device>()
            registerAdapterForSessionInfo<Location>()
        }

        //Finally, Session Adapter
        builder.registerAdapterForSession()
        builder.create()
    }

    private inline fun <reified T> GsonBuilder.registerAdapterForEvent() {
        registerTypeAdapter(T::class.java, EventApiAdapter<T>())
    }

    private inline fun <reified T> GsonBuilder.registerAdapterForSessionInfo() {
        registerTypeAdapter(T::class.java, SessionInfoApiAdapter<T>())
    }

    private fun GsonBuilder.registerAdapterForSession() {
        registerTypeAdapter(SessionEvents::class.java, SessionApiAdapter(this.create()))
    }
}
