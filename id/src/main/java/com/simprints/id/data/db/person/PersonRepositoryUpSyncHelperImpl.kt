package com.simprints.id.data.db.person

import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperation
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperationResult
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperationResult.UpSyncState
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperationResult.UpSyncState.RUNNING
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncProgress
import com.simprints.id.data.db.person.domain.FaceSample
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.domain.personevents.*
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.EventRemoteDataSource
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

class PersonRepositoryUpSyncHelperImpl(
    private val loginInfoManager: LoginInfoManager,
    private val personLocalDataSource: PersonLocalDataSource,
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository,
    private val modalities: List<Modality>
) : PersonRepositoryUpSyncHelper {

    internal val batchSize by lazy { UPSYNC_BATCH_SIZE }

    @ExperimentalCoroutinesApi
    override suspend fun executeUploadWithProgress(scope: CoroutineScope) = scope.produce {
        val projectId = getProjectIdForSignedInUserOrThrow()
        try {
            personLocalDataSource.load(PersonLocalDataSource.Query(toSync = true))
                .bufferedChunks(batchSize)
                .collect {
                    upSyncBatch(it, projectId)
                    this.send(PeopleUpSyncProgress(it.size))
                }

        } catch (t: Throwable) {
            Timber.d("PersonRepository : failed uploading people")
            Timber.d(t)
            updateState(UpSyncState.FAILED, projectId)
            throw t
        }

        updateState(UpSyncState.COMPLETE, projectId)
    }

    private suspend fun upSyncBatch(people: List<Person>, projectId: String) {
        uploadPeople(people, projectId)
        markPeopleAsSynced(people)
        updateState(RUNNING, projectId)
    }

    private suspend fun uploadPeople(people: List<Person>, projectId: String) {
        if (people.isNotEmpty()) {
            Timber.d("PersonRepository : uploading ${people.size} people")
            eventRemoteDataSource.post(projectId, createEvents(people))
            Timber.d("Uploaded a batch of ${people.size} people")
        }
    }

    internal fun createEvents(people: List<Person>) =
        Events(people.map { createEventFromPerson(it) })

    private fun createEventFromPerson(person: Person): Event =
        with(person) {
            Event(
                getRandomUuid(),
                listOf(projectId),
                listOf(patientId),
                listOf(userId),
                listOf(moduleId),
                modalities.map { it.toMode() },
                createPayload(person)
            )
        }

    internal fun getRandomUuid() = UUID.randomUUID().toString()

    private fun createPayload(person: Person) =
        EnrolmentRecordCreationPayload(
            subjectId = person.patientId,
            projectId = person.projectId,
            moduleId = person.moduleId,
            attendantId = person.userId,
            biometricReferences = buildBiometricReferences(person.fingerprintSamples, person.faceSamples)
        )

    private fun buildBiometricReferences(fingerprintSamples: List<FingerprintSample>, faceSamples: List<FaceSample>) =
        listOf(
            FingerprintReference(
                fingerprintSamples.map {
                    FingerprintTemplate(it.templateQualityScore,
                        EncodingUtils.byteArrayToBase64(it.template),
                        it.fingerIdentifier.fromPersonToEvent())
                }),
            FaceReference(
                faceSamples.map {
                    FaceTemplate(
                        EncodingUtils.byteArrayToBase64(it.template)
                    )
                })
        )

    private suspend fun markPeopleAsSynced(people: List<Person>) {
        val updatedPeople = people.map { it.copy(toSync = false) }
        personLocalDataSource.insertOrUpdate(updatedPeople)
        Timber.d("Marked a batch of ${people.size} people as synced")
    }

    private suspend fun updateLastUpSyncTime(peopleUpSyncOperation: PeopleUpSyncOperation) {
        peopleUpSyncScopeRepository.insertOrUpdate(peopleUpSyncOperation)
    }

    private suspend fun updateState(state: UpSyncState, projectId: String) {
        Timber.d("Updating sync state: $state")
        updateLastUpSyncTime(PeopleUpSyncOperation(
            projectId,
            PeopleUpSyncOperationResult(
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
