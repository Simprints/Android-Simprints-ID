package com.simprints.core.analytics

import android.util.Log

interface CrashReportManager: CoreCrashReportManager {
    fun logExceptionOrSafeException(throwable: Throwable)
}

interface CoreCrashReportManager {
    fun logMessageForCrashReport(crashReportTag: CrashReportTag,
                                 crashReportTrigger: CrashReportTrigger,
                                 crashPriority: Int = Log.INFO,
                                 message: String)

    fun logException(throwable: Throwable)
    fun logSafeException(throwable: Throwable)

    fun logMalfunction(message: String)

    fun setProjectIdCrashlyticsKey(projectId: String)
    fun setUserIdCrashlyticsKey(userId: String)
    fun setModuleIdsCrashlyticsKey(moduleIds: Set<String>?)
    fun setDownSyncTriggersCrashlyticsKey(eventDownSyncSetting: String)
    fun setSessionIdCrashlyticsKey(sessionId: String)
    fun setFingersSelectedCrashlyticsKey(fingersSelected: List<String>)

    companion object {
//        fun build(app: Application): CoreCrashReportManager =
//            app.component.getCrashReportManager()
    }
}
