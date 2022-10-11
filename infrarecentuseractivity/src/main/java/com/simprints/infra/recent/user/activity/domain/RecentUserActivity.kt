package com.simprints.infra.recent.user.activity.domain

data class RecentUserActivity(
    var lastScannerVersion: String,
    var lastScannerUsed: String,
    var lastUserUsed: String,
    var enrolmentsToday: Int,
    var identificationsToday: Int,
    var verificationsToday: Int,
    var lastActivityTime: Long,
)
