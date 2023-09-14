package com.simprints.infra.recent.user.activity.local

import com.simprints.core.domain.tokenization.asTokenizedEncrypted
import com.simprints.core.domain.tokenization.asTokenizedRaw
import com.simprints.core.domain.tokenization.isTokenized
import com.simprints.infra.recent.user.activity.ProtoRecentUserActivity
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity

internal fun RecentUserActivity.toProto(): ProtoRecentUserActivity {
    val isTokenized = lastUserUsed.isTokenized()
    return ProtoRecentUserActivity.newBuilder()
        .setLastScannerVersion(lastScannerVersion)
        .setLastScannerUsed(lastScannerUsed)
        .setLastUserUsed(lastUserUsed.value)
        .setIsUserIdTokenized(isTokenized)
        .setEnrolmentsToday(enrolmentsToday)
        .setIdentificationsToday(identificationsToday)
        .setVerificationsToday(verificationsToday)
        .setLastActivityTime(lastActivityTime)
        .build()
}

internal fun ProtoRecentUserActivity.toDomain(): RecentUserActivity {
    val lastUserUsed =
        if (isUserIdTokenized) lastUserUsed.asTokenizedEncrypted() else lastUserUsed.asTokenizedRaw()
    return RecentUserActivity(
        lastScannerVersion = lastScannerVersion,
        lastScannerUsed = lastScannerUsed,
        lastUserUsed = lastUserUsed,
        enrolmentsToday = enrolmentsToday,
        identificationsToday = identificationsToday,
        verificationsToday = verificationsToday,
        lastActivityTime = lastActivityTime,
    )
}
