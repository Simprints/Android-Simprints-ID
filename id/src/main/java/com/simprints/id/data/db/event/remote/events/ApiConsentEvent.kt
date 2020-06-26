package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.ConsentEvent
import com.simprints.id.data.db.event.domain.events.ConsentEvent.ConsentPayload

@Keep
class ApiConsentEvent(val relativeStartTime: Long,
                      var relativeEndTime: Long,
                      val consentType: ApiType,
                      var result: ApiResult) : ApiEvent(ApiEventType.CONSENT) {
    @Keep
    enum class ApiType {
        INDIVIDUAL, PARENTAL
    }

    @Keep
    enum class ApiResult {
        ACCEPTED, DECLINED, NO_RESPONSE
    }

    constructor(consentEvent: ConsentEvent) :
        this((consentEvent.payload as ConsentPayload).creationTime,
            consentEvent.payload.endTime,
            ApiType.valueOf(consentEvent.payload.consentType.toString()),
            ApiResult.valueOf(consentEvent.payload.result.toString()))
}
