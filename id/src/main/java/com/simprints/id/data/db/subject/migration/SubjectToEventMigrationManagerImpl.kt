package com.simprints.id.data.db.subject.migration

import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.subject.domain.SubjectAction
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.toMode
import com.simprints.id.exceptions.unexpected.MigrationToNewEventArchitectureException
import com.simprints.id.tools.time.TimeHelper
import kotlinx.coroutines.flow.toList
import timber.log.Timber

class SubjectToEventMigrationManagerImpl(
    val loginInfoManager: LoginInfoManager,
    val eventRepository: EventRepository,
    val timeHelper: TimeHelper,
    val crashReportManager: CrashReportManager,
    val preferencesManager: PreferencesManager,
    val localDataSource: SubjectLocalDataSource) : SubjectToEventMigrationManager {

    override suspend fun migrateSubjectToSyncToEventsDb() {
        try {
            if (loginInfoManager.getSignedInProjectIdOrEmpty().isNotEmpty()) {
                val subjectsToSync = localDataSource.load().toList()
                for (subject in subjectsToSync) {
                    eventRepository.addEvent(
                        EnrolmentRecordCreationEvent(
                            timeHelper.now(),
                            subject.subjectId,
                            subject.projectId,
                            subject.moduleId,
                            subject.attendantId,
                            preferencesManager.modalities.map { it.toMode() },
                            EnrolmentRecordCreationEvent.buildBiometricReferences(subject.fingerprintSamples, subject.faceSamples)
                        )
                    )
                }

                localDataSource.performActions(subjectsToSync.map {
                    //It overrides the original subject since the subjectId is the same
                    SubjectAction.Creation(it.copy(toSync = false))
                })
            }
        } catch (t: Throwable) {
            Timber.e(t)
            crashReportManager.logException(MigrationToNewEventArchitectureException(cause = t))
        }
    }
}
