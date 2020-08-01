package com.simprints.id.data.db.event.remote.models.session

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.session.Location
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent.SessionCapturePayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType
import java.util.*


@Keep
open class ApiSessionCapturePayload(override val relativeStartTime: Long,
                                    override val version: Int,
                                    var appVersionName: String,
                                    var libVersionName: String,
                                    var language: String,
                                    var device: ApiDevice,
                                    val id: String = UUID.randomUUID().toString(),
                                    var endedAt: Long = 0,
                                    var uploadedAt: Long = 0,
                                    var databaseInfo: ApiDatabaseInfo,
                                    var location: ApiLocation? = null,
                                    var analyticsId: String? = null) : ApiEventPayload(ApiEventPayloadType.SessionCapture, version, relativeStartTime) {

    constructor(domainPayload: SessionCapturePayload) : this(
        domainPayload.createdAt,
        domainPayload.eventVersion,
        domainPayload.appVersionName,
        domainPayload.libVersionName,
        domainPayload.language,
        domainPayload.device.fromDomainToApi(),
        domainPayload.id,
        domainPayload.endedAt ?: 0,
        domainPayload.uploadTime,
        domainPayload.databaseInfo.fromDomainToApi(),
        domainPayload.location.fromDomainToApi(),
        domainPayload.analyticsId
    )
}

fun Location?.fromDomainToApi() =
    this?.let {
        ApiLocation(latitude, longitude)
    }
