package com.simprints.id.data.db.event.remote.session

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.AuthorizationEvent
import com.simprints.id.data.db.event.domain.events.session.Location
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent.SessionCapturePayload
import com.simprints.id.data.db.event.remote.events.ApiEvent
import com.simprints.id.data.db.event.remote.events.ApiEventPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType
import com.simprints.id.data.db.event.remote.events.fromDomainToApi
import java.util.*

@Keep
class ApiSessionCapture(domainEvent: SessionCaptureEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    open class ApiSessionCapturePayload(createdAt: Long,
                                        version: Int,
                                        var appVersionName: String,
                                        var libVersionName: String,
                                        var language: String,
                                        var device: ApiDevice,
                                        val id: String = UUID.randomUUID().toString(),
                                        var endedAt: Long = 0,
                                        var uploadedAt: Long = 0,
                                        var databaseInfo: ApiDatabaseInfo,
                                        var location: ApiLocation? = null,
                                        var analyticsId: String? = null) : ApiEventPayload(ApiEventPayloadType.SESSION_CAPTURE, version, createdAt) {

        constructor(domainPayload: SessionCapturePayload) : this(
            domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.appVersionName,
            domainPayload.libVersionName,
            domainPayload.language,
            domainPayload.device.fromDomainToApi(),
            domainPayload.id,
            domainPayload.endTime,
            domainPayload.uploadTime,
            domainPayload.databaseInfo.fromDomainToApi(),
            domainPayload.location.fromDomainToApi(),
            domainPayload.analyticsId
        )
    }
}

fun Location?.fromDomainToApi() =
    this?.let {
        ApiLocation(latitude, longitude)
    }
