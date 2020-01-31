package com.simprints.id.data.analytics.crashreport

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.FINGERS_SELECTED
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.MALFUNCTION_MESSAGE
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.MODULE_IDS
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.PEOPLE_DOWN_SYNC_TRIGGERS
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.PROJECT_ID
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.SESSION_ID
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.USER_ID
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.exceptions.safe.MalfunctionException
import com.simprints.id.exceptions.safe.SafeException
import com.simprints.id.services.scheduledSync.people.master.models.PeopleDownSyncTrigger

open class CrashReportManagerImpl: CrashReportManager {

    internal val crashlyticsInstance by lazy {
        Crashlytics.getInstance().core
    }

    override fun logMessageForCrashReport(crashReportTag: CrashReportTag, crashReportTrigger: CrashReportTrigger,
                                          crashPriority: Int, message: String) {
        crashlyticsInstance.log(crashPriority, crashReportTag.name, getLogMessage(crashReportTrigger, message))
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
        crashlyticsInstance.logException(throwable)
    }

    override fun logSafeException(throwable: Throwable) {
        crashlyticsInstance.log(Log.ERROR, CrashReportTag.SAFE_EXCEPTION.name, "$throwable")
    }

    override fun logMalfunction(message: String) {
        crashlyticsInstance.setString(MALFUNCTION_MESSAGE, message)
        crashlyticsInstance.logException(MalfunctionException())
    }

    override fun setProjectIdCrashlyticsKey(projectId: String) {
        crashlyticsInstance.setString(PROJECT_ID, projectId)
    }

    override fun setUserIdCrashlyticsKey(userId: String) {
        crashlyticsInstance.setString(USER_ID, userId)
        crashlyticsInstance.setUserIdentifier(userId)
    }

    override fun setModuleIdsCrashlyticsKey(moduleIds: Set<String>?) {
        crashlyticsInstance.setString(MODULE_IDS, moduleIds.toString())
    }

    override fun setDownSyncTriggersCrashlyticsKey(peopleDownSyncTriggers: Map<PeopleDownSyncTrigger, Boolean>) {
        crashlyticsInstance.setString(PEOPLE_DOWN_SYNC_TRIGGERS, peopleDownSyncTriggers.toString())
    }

    override fun setSessionIdCrashlyticsKey(sessionId: String) {
        crashlyticsInstance.setString(SESSION_ID, sessionId)
    }

    override fun setFingersSelectedCrashlyticsKey(fingersSelected: Map<FingerIdentifier, Boolean>) {
        crashlyticsInstance.setString(FINGERS_SELECTED, fingersSelected.toString())
    }
}
