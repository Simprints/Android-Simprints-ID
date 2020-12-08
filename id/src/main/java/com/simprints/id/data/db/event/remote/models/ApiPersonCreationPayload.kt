package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.PersonCreationEvent.PersonCreationPayload


@Keep
data class ApiPersonCreationPayload(override val startTime: Long,
                                    override val version: Int,
                                    val fingerprintCaptureIds: List<String>?,
                                    val fingerprintReferenceId: String?,
                                    val faceCaptureIds: List<String>?,
                                    val faceReferenceId: String?) : ApiEventPayload(ApiEventPayloadType.PersonCreation, version, startTime) {

    constructor(domainPayload: PersonCreationPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.fingerprintCaptureIds,
            domainPayload.fingerprintReferenceId,
            domainPayload.faceCaptureIds,
            domainPayload.faceReferenceId)
}
