package com.simprints.id.data.analytics.crashreport

import com.simprints.core.analytics.CrashReportManager
import com.simprints.core.analytics.CrashReportTag
import com.simprints.core.analytics.CrashReportTrigger
import com.simprints.core.exceptions.SafeException

open class CrashReportManagerImpl: CrashReportManager {

//    internal val crashlyticsInstance by lazy {
//        FirebaseCrashlytics.getInstance()
//    }

    override fun logMessageForCrashReport(crashReportTag: CrashReportTag, crashReportTrigger: CrashReportTrigger,
                                          crashPriority: Int, message: String) {
       // crashlyticsInstance.log(getLogMessage(crashReportTrigger, message))
    }

    internal fun getLogMessage(crashReportTrigger: CrashReportTrigger, message: String) = "[${crashReportTrigger.name}] $message"

    override fun logExceptionOrSafeException(throwable: Throwable) {
        if(throwable is SafeException) {
            logSafeException(throwable)
        } else {
            logException(throwable)
        }
    }

    override fun logException(throwable: Throwable) {
        //crashlyticsInstance.recordException(throwable)
    }

    override fun logSafeException(throwable: Throwable) {
       // crashlyticsInstance.log("$throwable")
    }

    override fun logMalfunction(message: String) {
       // crashlyticsInstance.setCustomKey(MALFUNCTION_MESSAGE, message)
       // crashlyticsInstance.recordException(MalfunctionException())
    }

    override fun setProjectIdCrashlyticsKey(projectId: String) {
        //crashlyticsInstance.setCustomKey(PROJECT_ID, projectId)
    }

    override fun setUserIdCrashlyticsKey(userId: String) {
       //crashlyticsInstance.setCustomKey(USER_ID, userId)
        //crashlyticsInstance.setUserId(userId)
    }

    override fun setModuleIdsCrashlyticsKey(moduleIds: Set<String>?) {
       // crashlyticsInstance.setCustomKey(MODULE_IDS, moduleIds.toString())
    }

    override fun setDownSyncTriggersCrashlyticsKey(eventDownSyncSetting: String) {
      //  crashlyticsInstance.setCustomKey(SUBJECTS_DOWN_SYNC_TRIGGERS, eventDownSyncSetting)
    }

    override fun setSessionIdCrashlyticsKey(sessionId: String) {
       // crashlyticsInstance.setCustomKey(SESSION_ID, sessionId)
    }

    override fun setFingersSelectedCrashlyticsKey(fingersSelected: List<String>) {
      //  crashlyticsInstance.setCustomKey(FINGERS_SELECTED, fingersSelected.toString())
    }
}
