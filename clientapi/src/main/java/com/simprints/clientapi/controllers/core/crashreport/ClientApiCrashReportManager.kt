package com.simprints.clientapi.controllers.core.crashreport

interface ClientApiCrashReportManager {

    fun logExceptionOrThrowable(throwable: Throwable)

    fun setSessionIdCrashlyticsKey(sessionId: String)
}
