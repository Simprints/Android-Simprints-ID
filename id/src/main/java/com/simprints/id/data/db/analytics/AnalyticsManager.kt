package com.simprints.id.data.db.analytics

interface AnalyticsManager {

    fun logException(throwable: Throwable?)

    fun logAlert(alertName: String,
                 apiKey: String,
                 moduleId: String,
                 userId: String,
                 deviceId: String)
}

