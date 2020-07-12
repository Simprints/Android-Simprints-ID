package com.simprints.id.data.db.subject

import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.Event.EventLabel.*
import com.simprints.id.data.db.event.domain.events.EventLabel.*
import com.simprints.id.data.db.event.domain.events.Events
import com.simprints.id.data.db.event.domain.events.subject.*
import com.simprints.id.data.db.subject.domain.FaceSample
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.db.subject.remote.EventRemoteDataSource
import com.simprints.id.data.db.subjects_sync.up.SubjectsUpSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.up.domain.SubjectsUpSyncOperation
import com.simprints.id.data.db.subjects_sync.up.domain.SubjectsUpSyncOperationResult
import com.simprints.id.data.db.subjects_sync.up.domain.SubjectsUpSyncOperationResult.UpSyncState
import com.simprints.id.data.db.subjects_sync.up.domain.SubjectsUpSyncOperationResult.UpSyncState.RUNNING
import com.simprints.id.data.db.subjects_sync.up.domain.SubjectsUpSyncProgress
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.toMode
import com.simprints.id.tools.extensions.bufferedChunks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.util.*

class SubjectRepositoryUpSyncHelperImpl(
    private val loginInfoManager: LoginInfoManager,
    private val subjectLocalDataSource: SubjectLocalDataSource,
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val subjectsUpSyncScopeRepository: SubjectsUpSyncScopeRepository,
    private val modalities: List<Modality>
) : SubjectRepositoryUpSyncHelper {

    internal val batchSize by lazy { UPSYNC_BATCH_SIZE }

    @ExperimentalCoroutinesApi
    override suspend fun executeUploadWithProgress(scope: CoroutineScope) = scope.produce {
        val projectId = getProjectIdForSignedInUserOrThrow()
        try {
            subjectLocalDataSource.load(SubjectLocalDataSource.Query(toSync = true))
                .bufferedChunks(batchSize)
                .collect {
                    upSyncBatch(it, projectId)
                    this.send(SubjectsUpSyncProgress(it.size))
                }

        } catch (t: Throwable) {
            Timber.d("PersonRepository : failed uploading people")
            Timber.d(t)
            updateState(UpSyncState.FAILED, projectId)
            throw t
        }

        updateState(UpSyncState.COMPLETE, projectId)
    }

    private suspend fun upSyncBatch(subjects: List<Subject>, projectId: String) {
        uploadPeople(subjects, projectId)
        markPeopleAsSynced(subjects)
        updateState(RUNNING, projectId)
    }

    private suspend fun uploadPeople(subjects: List<Subject>, projectId: String) {
        if (subjects.isNotEmpty()) {
            Timber.d("PersonRepository : uploading ${subjects.size} people")
            eventRemoteDataSource.post(projectId, createEvents(subjects))
            Timber.d("Uploaded a batch of ${subjects.size} people")
        }
    }

    internal fun createEvents(subjects: List<Subject>) =
        Events(subjects.map { createEventFromPerson(it) })

    private fun createEventFromPerson(subject: Subject): Event =
        with(subject) {
            Event(
                getRandomUuid(),
                listOf(
                    ProjectId(projectId),
                    SubjectId(subjectId),
                    AttendantId(attendantId),
                    ModuleId(listOf(moduleId)),
                    Mode(modalities.map { it.toMode() })
                ),
                createPayload(subject)
            )
        }

    internal fun getRandomUuid() = UUID.randomUUID().toString()

    private fun createPayload(subject: Subject) =
        EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
            subjectId = subject.subjectId,
            projectId = subject.projectId,
            moduleId = subject.moduleId,
            attendantId = subject.attendantId,
            biometricReferences = buildBiometricReferences(subject.fingerprintSamples, subject.faceSamples)
        )

    private fun buildBiometricReferences(fingerprintSamples: List<FingerprintSample>,
                                         faceSamples: List<FaceSample>): List<BiometricReference> {
        val biometricReferences = mutableListOf<BiometricReference>()

        buildFingerprintReference(fingerprintSamples)?.let {
            biometricReferences.add(it)
        }

        buildFaceReference(faceSamples)?.let {
            biometricReferences.add(it)
        }

        return biometricReferences
    }

    private fun buildFingerprintReference(fingerprintSamples: List<FingerprintSample>) =
        if (fingerprintSamples.isNotEmpty()) {
            FingerprintReference(
                fingerprintSamples.map {
                    FingerprintTemplate(it.templateQualityScore,
                        EncodingUtils.byteArrayToBase64(it.template),
                        it.fingerIdentifier.fromSubjectToEvent())
                }
            )
        } else {
            null
        }

    private fun buildFaceReference(faceSamples: List<FaceSample>) =
        if (faceSamples.isNotEmpty()) {
            FaceReference(
                faceSamples.map {
                    FaceTemplate(
                        EncodingUtils.byteArrayToBase64(it.template)
                    )
                }
            )
        } else {
            null
        }

    private suspend fun markPeopleAsSynced(subjects: List<Subject>) {
        val updatedPeople = subjects.map { it.copy(toSync = false) }
        subjectLocalDataSource.insertOrUpdate(updatedPeople)
        Timber.d("Marked a batch of ${subjects.size} people as synced")
    }

    private suspend fun updateLastUpSyncTime(subjectsUpSyncOperation: SubjectsUpSyncOperation) {
        subjectsUpSyncScopeRepository.insertOrUpdate(subjectsUpSyncOperation)
    }

    private suspend fun updateState(state: UpSyncState, projectId: String) {
        Timber.d("Updating sync state: $state")
        updateLastUpSyncTime(SubjectsUpSyncOperation(
            projectId,
            SubjectsUpSyncOperationResult(
                state,
                Date().time
            )
        ))
    }

    private fun getProjectIdForSignedInUserOrThrow(): String {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
        if (projectId.isEmpty()) {
            throw IllegalStateException("People can only be uploaded when signed in")
        }
        return projectId
    }

    companion object {
        private const val UPSYNC_BATCH_SIZE = 80
    }
}
