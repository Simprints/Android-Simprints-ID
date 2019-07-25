package com.simprints.clientapi.controllers.core.crashreport

import com.simprints.clientapi.exceptions.ClientApiSafeException
import com.simprints.id.data.analytics.crashreport.CoreCrashReportManager

class ClientApiCrashReportManagerImpl(private val coreCrashReportManager: CoreCrashReportManager)
    : ClientApiCrashReportManager {

    override fun logExceptionOrSafeException(throwable: Throwable) =
        if (throwable is ClientApiSafeException) {
            coreCrashReportManager.logSafeException(throwable)
        } else {
            coreCrashReportManager.logException(throwable)
        }

    override fun setSessionIdCrashlyticsKey(sessionId: String) {
        coreCrashReportManager.setSessionIdCrashlyticsKey(sessionId)
    }
}
