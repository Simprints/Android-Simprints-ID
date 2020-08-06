package com.simprints.id.data.analytics.crashreport

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.FINGERS_SELECTED
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.MALFUNCTION_MESSAGE
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.MODULE_IDS
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.SUBJECTS_DOWN_SYNC_TRIGGERS
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.PROJECT_ID
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.SESSION_ID
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.USER_ID
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.exceptions.safe.MalfunctionException
import com.simprints.id.exceptions.safe.SafeException
import com.simprints.id.services.sync.events.master.models.SubjectsDownSyncSetting

open class CrashReportManagerImpl: CrashReportManager {

    internal val crashlyticsInstance by lazy {
        FirebaseCrashlytics.getInstance()
    }

    override fun logMessageForCrashReport(crashReportTag: CrashReportTag, crashReportTrigger: CrashReportTrigger,
                                          crashPriority: Int, message: String) {
        crashlyticsInstance.log(getLogMessage(crashReportTrigger, message))
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
        crashlyticsInstance.recordException(throwable)
    }

    override fun logSafeException(throwable: Throwable) {
        crashlyticsInstance.log("$throwable")
    }

    override fun logMalfunction(message: String) {
        crashlyticsInstance.setCustomKey(MALFUNCTION_MESSAGE, message)
        crashlyticsInstance.recordException(MalfunctionException())
    }

    override fun setProjectIdCrashlyticsKey(projectId: String) {
        crashlyticsInstance.setCustomKey(PROJECT_ID, projectId)
    }

    override fun setUserIdCrashlyticsKey(userId: String) {
        crashlyticsInstance.setCustomKey(USER_ID, userId)
        crashlyticsInstance.setUserId(userId)
    }

    override fun setModuleIdsCrashlyticsKey(moduleIds: Set<String>?) {
        crashlyticsInstance.setCustomKey(MODULE_IDS, moduleIds.toString())
    }

    override fun setDownSyncTriggersCrashlyticsKey(subjectsDownSyncSetting: SubjectsDownSyncSetting) {
        crashlyticsInstance.setCustomKey(SUBJECTS_DOWN_SYNC_TRIGGERS, subjectsDownSyncSetting.toString())
    }

    override fun setSessionIdCrashlyticsKey(sessionId: String) {
        crashlyticsInstance.setCustomKey(SESSION_ID, sessionId)
    }

    override fun setFingersSelectedCrashlyticsKey(fingersSelected: Map<FingerIdentifier, Boolean>) {
        crashlyticsInstance.setCustomKey(FINGERS_SELECTED, fingersSelected.toString())
    }
}
