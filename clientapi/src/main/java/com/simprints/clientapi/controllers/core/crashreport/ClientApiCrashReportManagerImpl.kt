package com.simprints.clientapi.controllers.core.crashreport

import com.simprints.id.data.analytics.crashreport.CrashReportManager

class ClientApiCrashReportManagerImpl(private val coreCrashReportManager: CrashReportManager): ClientApiCrashReportManager {

    override fun logExceptionOrThrowable(throwable: Throwable) {
        coreCrashReportManager.logExceptionOrThrowable(throwable)
    }

    override fun setSessionIdCrashlyticsKey(sessionId: String) {
        coreCrashReportManager.setSessionIdCrashlyticsKey(sessionId)
    }
}
