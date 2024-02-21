package com.simprints.infra.eventsync.status.up.domain

data class EventUpSyncResult(
    val requestId: String,
    val status: Int,
)
