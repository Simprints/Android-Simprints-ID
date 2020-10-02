package com.simprints.id.data.db.event.domain.models

import com.simprints.id.domain.modality.Modes

data class EventLabels(
    val projectId: String? = null,
    val subjectId: String? = null,
    val attendantId: String? = null,
    val moduleIds: List<String>? = null,
    val mode: List<Modes>? = null,
    val sessionId: String? = null,
    val deviceId: String? = null)
