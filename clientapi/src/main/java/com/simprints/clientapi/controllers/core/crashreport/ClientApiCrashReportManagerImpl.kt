package com.simprints.clientapi.controllers.core.crashreport

import com.simprints.clientapi.exceptions.ClientApiSafeException
import com.simprints.core.analytics.CoreCrashReportManager
import com.simprints.logging.Simber

class ClientApiCrashReportManagerImpl(private val coreCrashReportManager: CoreCrashReportManager)
    : ClientApiCrashReportManager {

    override fun logExceptionOrSafeException(throwable: Throwable) =
        if (throwable is ClientApiSafeException) {
            Simber.i(throwable)
        } else {
            Simber.e(throwable)
        }

    override fun setSessionIdCrashlyticsKey(sessionId: String) {
        coreCrashReportManager.setSessionIdCrashlyticsKey(sessionId)
    }
}
