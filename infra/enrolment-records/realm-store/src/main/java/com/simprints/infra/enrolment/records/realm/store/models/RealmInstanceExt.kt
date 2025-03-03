package com.simprints.infra.enrolment.records.realm.store.models

import io.realm.kotlin.types.RealmInstant
import java.util.Date

/**
 * Converting epoch milliseconds of java.util.Date to pair of (epoch seconds, nanosecond offset).
 */
fun Date.toRealmInstant() = RealmInstant.from(
    time / MILLIS_IN_SECOND,
    (time % MILLIS_IN_SECOND * NANOS_IN_MILLI).toInt(),
)

/**
 * Converting (epoch seconds, nanosecond offset) pair to epoch milliseconds for java.util.Date.
 */
fun RealmInstant.toDate() = Date(
    (epochSeconds * MILLIS_IN_SECOND) + (nanosecondsOfSecond / NANOS_IN_MILLI),
)

private const val MILLIS_IN_SECOND = 1000L
private const val NANOS_IN_MILLI = 1_000_000L
