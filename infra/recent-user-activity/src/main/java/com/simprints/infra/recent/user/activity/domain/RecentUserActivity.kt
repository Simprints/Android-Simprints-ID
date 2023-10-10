package com.simprints.infra.recent.user.activity.domain

import com.simprints.core.domain.tokenization.TokenizableString

data class RecentUserActivity(
    var lastScannerVersion: String,
    var lastScannerUsed: String,
    var lastUserUsed: TokenizableString,
    var enrolmentsToday: Int,
    var identificationsToday: Int,
    var verificationsToday: Int,
    var lastActivityTime: Long,
)
