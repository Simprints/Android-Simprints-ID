package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import java.io.Serializable
import com.simprints.eventsystem.event.domain.models.OneToOneMatchEvent as CoreOneToOneMatchEvent

@Keep
class OneToOneMatchEvent(
    startTime: Long,
    endTime: Long,
    val query: Serializable,
    val matcher: Matcher,
    val result: MatchEntry?,
    val fingerComparisonStrategy: FingerComparisonStrategy,
) : Event(EventType.ONE_TO_ONE_MATCH, startTime, endTime)

fun OneToOneMatchEvent.fromDomainToCore() =
    CoreOneToOneMatchEvent(
        startTime,
        endTime,
        (query as SubjectQuery).extractVerifyId(),
        matcher.fromDomainToCore(),
        result?.fromDomainToCore(),
        fingerComparisonStrategy.fromDomainToCore()
    )

fun SubjectQuery.extractVerifyId() =
    subjectId
        ?: throw FingerprintUnexpectedException("null personId in candidate query when saving OneToOneMatchEvent")
