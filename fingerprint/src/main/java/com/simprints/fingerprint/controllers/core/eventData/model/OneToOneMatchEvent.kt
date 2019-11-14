package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import java.io.Serializable
import com.simprints.id.data.analytics.eventdata.models.domain.events.OneToOneMatchEvent as CoreOneToOneMatchEvent

@Keep
class OneToOneMatchEvent(starTime: Long,
                         endTime: Long,
                         val query: Serializable,
                         val result: MatchEntry?) : Event(EventType.ONE_TO_ONE_MATCH, starTime, endTime)

fun OneToOneMatchEvent.fromDomainToCore() =
    CoreOneToOneMatchEvent(
        starTime,
        endTime,
        (query as PersonLocalDataSource.Query).extractVerifyId(),
        result?.fromDomainToCore()
    )

fun PersonLocalDataSource.Query.extractVerifyId() =
    personId
        ?: throw FingerprintUnexpectedException("null personId in candidate query when saving OneToOneMatchEvent")
