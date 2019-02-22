package com.simprints.id.data.analytics.crashReport

import android.util.Log
import com.simprints.id.services.scheduledSync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.libsimprints.FingerIdentifier

interface CrashReportManager {

    fun logMessageForCrashReport(crashReportTag: CrashReportTags,
                                 crashTrigger: CrashTrigger,
                                 crashPriority: Int = Log.INFO,
                                 message: String)

    fun logExceptionOrThrowable(throwable: Throwable)

    fun setProjectIdCrashlyticsKey(projectId: String)
    fun setUserIdCrashlyticsKey(userId: String)
    fun setModuleIdsCrashlyticsKey(moduleIds: Set<String>?)
    fun setDownSyncTriggersCrashlyticsKey(peopleDownSyncTriggers: Map<PeopleDownSyncTrigger, Boolean>)
    fun setSessionIdCrashlyticsKey(sessionId: String)
    fun setFingersSelectedCrashlyticsKey(fingersSelected: Map<FingerIdentifier, Boolean>)
}
