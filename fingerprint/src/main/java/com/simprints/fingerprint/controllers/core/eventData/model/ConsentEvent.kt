package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent as ConsentEventCore

@Keep
class ConsentEvent(override val starTime: Long,
                   override val endTime: Long,
                   val consentType: Type,
                   var result: Result) : Event(EventType.CONSENT) {

    @Keep
    enum class Type {
        INDIVIDUAL, PARENTAL
    }

    @Keep
    enum class Result {
        ACCEPTED, DECLINED, NO_RESPONSE
    }
}

fun ConsentEvent.fromDomainToCore() =
    ConsentEventCore(starTime, endTime, consentType.fromDomainToCore(), result.fromDomainToCore())

fun ConsentEvent.Type.fromDomainToCore() =
    when(this) {
        ConsentEvent.Type.INDIVIDUAL -> ConsentEventCore.Type.INDIVIDUAL
        ConsentEvent.Type.PARENTAL -> ConsentEventCore.Type.PARENTAL
    }

fun ConsentEvent.Result.fromDomainToCore() =
    when(this) {
        ConsentEvent.Result.ACCEPTED -> ConsentEventCore.Result.ACCEPTED
        ConsentEvent.Result.DECLINED -> ConsentEventCore.Result.DECLINED
        ConsentEvent.Result.NO_RESPONSE -> ConsentEventCore.Result.NO_RESPONSE
    }
