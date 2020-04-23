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
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.*

class PersonRepositoryUpSyncHelperImpl(
    private val loginInfoManager: LoginInfoManager,
    private val personLocalDataSource: PersonLocalDataSource,
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository,
    private val modalities: List<Modality>
) : PersonRepositoryUpSyncHelper {

    val projectId: String
        get() {
            val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
            return if (projectId.isEmpty() /*|| userId != loginInfoManager.signedInUserId*/) {
                throw IllegalStateException("Only people enrolled by the currently signed in user can be up-synced")
            } else {
                projectId
            }
        }

    @ExperimentalCoroutinesApi
    override suspend fun executeUploadWithProgress(scope: CoroutineScope) = scope.produce {
        try {
            personLocalDataSource.load(PersonLocalDataSource.Query(toSync = true))
                .bufferedChunks(UPSYNC_BATCH_SIZE)
                .collect {
                    Timber.d("PersonRepository : uploading ${it.size} people")
                    upSyncBatch(it)
                    this.send(PeopleUpSyncProgress(it.size))
                }

        } catch (t: Throwable) {
            t.printStackTrace()
            Timber.d("PersonRepository : failed uploading people")
            updateState(UpSyncState.FAILED)
            throw t
        }

        updateState(UpSyncState.COMPLETE)
    }

    private suspend fun upSyncBatch(people: List<Person>) {
        uploadPeople(people)
        Timber.d("Uploaded a batch of ${people.size} people")
        markPeopleAsSynced(people)
        Timber.d("Marked a batch of ${people.size} people as synced")
        updateState(RUNNING)
    }

    private suspend fun uploadPeople(people: List<Person>) {
        if (people.isNotEmpty()) {
            eventRemoteDataSource.post(projectId, createEvents(people))
        }
    }

    internal fun createEvents(people: List<Person>) =
        Events(people.map { createEventFromPerson(it) })

    private fun createEventFromPerson(person: Person): Event =
        with(person) {
            Event(
                UUID.randomUUID().toString(),
                listOf(projectId),
                listOf(patientId),
                listOf(userId),
                listOf(moduleId),
                modalities.map { it.toMode() },
                createPayload(person)
            )
        }

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

    private fun markPeopleAsSynced(people: List<Person>) {
        val updatedPeople = people.map { it.copy(toSync = false) }
        runBlocking { personLocalDataSource.insertOrUpdate(updatedPeople) }
    }

    private suspend fun updateLastUpSyncTime(peopleUpSyncOperation: PeopleUpSyncOperation) {
        peopleUpSyncScopeRepository.insertOrUpdate(peopleUpSyncOperation)
    }

    private suspend fun updateState(state: UpSyncState) {
        Timber.d("Updating sync state: $state")
        updateLastUpSyncTime(PeopleUpSyncOperation(
            projectId,
            PeopleUpSyncOperationResult(
                state,
                Date().time
            )
        ))
    }

    companion object {
        private const val UPSYNC_BATCH_SIZE = 80
    }
}
