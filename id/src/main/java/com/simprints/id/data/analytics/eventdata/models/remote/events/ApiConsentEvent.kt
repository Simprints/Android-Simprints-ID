package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent

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
        this(consentEvent.relativeStartTime ?: 0,
            consentEvent.relativeEndTime ?: 0,
            ApiType.valueOf(consentEvent.consentType.toString()),
            ApiResult.valueOf(consentEvent.result.toString()))
}
