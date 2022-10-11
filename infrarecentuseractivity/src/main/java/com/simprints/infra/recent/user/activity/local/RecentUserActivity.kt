package com.simprints.infra.recent.user.activity.local

import com.simprints.infra.recent.user.activity.ProtoRecentUserActivity
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity

internal fun RecentUserActivity.toProto(): ProtoRecentUserActivity =
    ProtoRecentUserActivity.newBuilder()
        .setLastScannerVersion(lastScannerVersion)
        .setLastScannerUsed(lastScannerUsed)
        .setLastUserUsed(lastUserUsed)
        .setEnrolmentsToday(enrolmentsToday)
        .setIdentificationsToday(identificationsToday)
        .setVerificationsToday(verificationsToday)
        .setLastActivityTime(lastActivityTime)
        .build()

internal fun ProtoRecentUserActivity.toDomain(): RecentUserActivity =
    RecentUserActivity(
        lastScannerVersion,
        lastScannerUsed,
        lastUserUsed,
        enrolmentsToday,
        identificationsToday,
        verificationsToday,
        lastActivityTime,
    )
