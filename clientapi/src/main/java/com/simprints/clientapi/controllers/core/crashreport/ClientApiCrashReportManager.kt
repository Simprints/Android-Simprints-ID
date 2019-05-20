package com.simprints.clientapi.controllers.core.crashreport

interface ClientApiCrashReportManager {

    fun logExceptionOrSafeException(throwable: Throwable)

    fun setSessionIdCrashlyticsKey(sessionId: String)
}
