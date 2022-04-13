package com.simprints.id.activities.settings.syncinformation.fetchRecords

data class OutageData(
    val isFailureBackendMaintenance: Boolean,
    val estimatedOutage: Long? = null
)
