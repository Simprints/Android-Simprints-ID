package com.simprints.id.data.analytics.crashreport

import android.util.Log
import com.simprints.id.Application
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.services.scheduledSync.people.master.models.PeopleDownSyncSetting

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
    fun setDownSyncTriggersCrashlyticsKey(peopleDownSyncSetting: PeopleDownSyncSetting)
    fun setSessionIdCrashlyticsKey(sessionId: String)
    fun setFingersSelectedCrashlyticsKey(fingersSelected: Map<FingerIdentifier, Boolean>)

    companion object {
        fun build(app: Application): CoreCrashReportManager =
            app.component.getCrashReportManager()
    }
}
