package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import java.io.Serializable
import com.simprints.id.data.db.session.domain.models.events.Matcher as CoreMatcher
import com.simprints.id.data.db.session.domain.models.events.OneToOneMatchEvent as CoreOneToOneMatchEvent

@Keep
class OneToOneMatchEvent(
    startTime: Long,
    endTime: Long,
    val query: Serializable,
    val result: MatchEntry?
) : Event(EventType.ONE_TO_ONE_MATCH, startTime, endTime)

fun OneToOneMatchEvent.fromDomainToCore() =
    CoreOneToOneMatchEvent(
        startTime,
        endTime,
        (query as SubjectLocalDataSource.Query).extractVerifyId(),
        CoreMatcher.SIM_AFIS, // TODO: implement Matcher in fingerprint module
        result?.fromDomainToCore()
    )

fun SubjectLocalDataSource.Query.extractVerifyId() =
    subjectId
        ?: throw FingerprintUnexpectedException("null personId in candidate query when saving OneToOneMatchEvent")
