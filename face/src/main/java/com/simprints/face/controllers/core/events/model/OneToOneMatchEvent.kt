package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.face.exceptions.FaceUnexpectedException
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import java.io.Serializable
import com.simprints.id.data.db.session.domain.models.events.OneToOneMatchEvent as CoreOneToOneMatchEvent

@Keep
class OneToOneMatchEvent(startTime: Long,
                         endTime: Long,
                         val query: Serializable,
                         val result: MatchEntry?) : Event(EventType.ONE_TO_ONE_MATCH, startTime, endTime) {
    fun fromDomainToCore() = CoreOneToOneMatchEvent(
        startTime,
        endTime,
        (query as PersonLocalDataSource.Query).extractVerifyId(),
        result?.fromDomainToCore()
    )
}


fun PersonLocalDataSource.Query.extractVerifyId() =
    personId
        ?: throw FaceUnexpectedException("null personId in candidate query when saving OneToOneMatchEvent")


