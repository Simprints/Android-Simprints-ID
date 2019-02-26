package com.simprints.id.data.db.dbRecovery

import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.gson.annotations.SerializedName
import com.simprints.id.BuildConfig
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.FirebaseManagerImpl
import com.simprints.id.data.db.remote.tools.Utils
import com.simprints.id.domain.Constants
import com.simprints.id.domain.fingerprint.Fingerprint
import com.simprints.id.domain.Person
import com.simprints.id.exceptions.safe.data.db.LocalDbRecoveryFailedException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.json.SkipSerialisationField
import com.simprints.id.domain.fingerprint.Utils.byteArrayToBase64
import com.simprints.libsimprints.FingerIdentifier
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Flowable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import timber.log.Timber
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.*

class LocalDbRecovererImpl(private val localDbManager: LocalDbManager,
                           private val firebaseManagerImpl: FirebaseManagerImpl,
                           private val projectId: String,
                           private val userId: String,
                           private val androidId: String,
                           private val moduleId: String,
                           private val group: Constants.GROUP) :
    LocalDbRecoverer {

    private class JsonPerson(
        @SerializedName("id")
        val patientId: String,
        val projectId: String,
        val userId: String,
        val moduleId: String,
        val createdAt: Date?,
        val updatedAt: Date?,
        val toSync: Boolean = false,
        val fingerprints: Map<FingerIdentifier, ArrayList<JsonFingerprint>>
    )

    private fun Person.toJsonPerson(): JsonPerson =
        JsonPerson(
            patientId = patientId,
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            toSync = toSync,
            fingerprints = fingerprints
                .map { it.toJsonFingerprint() }
                .groupBy { it.fingerId }
                .mapValues { ArrayList(it.value) }
        )

    private class JsonFingerprint(
        @SkipSerialisationField val fingerId: FingerIdentifier,
        val template: String,
        val quality: Int
    )

    private fun Fingerprint.toJsonFingerprint(): JsonFingerprint =
        JsonFingerprint(
            fingerId = FingerIdentifier.values()[fingerId],
            template = byteArrayToBase64(template!!), // TODO: get rid of double bang
            quality = qualityScore
        )

    private val realmDbInputStream = PipedInputStream()
    private val realmDbOutputStream = PipedOutputStream()

    private lateinit var resultEmitter: CompletableEmitter

    override fun recoverDb(): Completable =
        Completable.create {
            Timber.d("LocalDbRecovererImpl.recoverDb()")
            resultEmitter = it

            connectTheStreams()
            val fileReference = generateFirebaseStorageFileReference()
            val metadata = generateMetaData()
            readAllPeopleIntoInputStream(fileReference, metadata)
            writeAllPeopleToOutputStream()
        }

    private fun connectTheStreams() =
        try {
            realmDbOutputStream.connect(realmDbInputStream)
        } catch (e: IOException) {
            resultEmitter.onError(LocalDbRecoveryFailedException("Failed to connect the streams", e))
        }

    private fun writeAllPeopleToOutputStream() =
        try {
            writeBeginningOfStream()
            getListOfPeopleToRecover()
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                    onNext = { writePersonToStream(it) },
                    onComplete = { writeEndOfStreamAndClose() },
                    onError = { resultEmitter.onError(LocalDbRecoveryFailedException("Failed to load people from DB", it)) }
                )
        } catch (e: JSONException) {
            resultEmitter.onError(LocalDbRecoveryFailedException("Failed to convert people list into JSON", e))
        } catch (e: IOException) {
            resultEmitter.onError(LocalDbRecoveryFailedException("Failed to write to the output stream", e))
        }

    private fun writeBeginningOfStream() {
        realmDbOutputStream.write("{\"$peopleJsonKey\":[")
    }

    private var peopleCount = 0
    private fun writePersonToStream(person: Person) {
        val commaString = if (peopleCount == 0) "" else ","
        val personString = convertPersonToJsonString(person)
        val fullString = "$commaString$personString"
        realmDbOutputStream.write(fullString)
        peopleCount++
    }

    private fun writeEndOfStreamAndClose() {
        realmDbOutputStream.write("]}")
        realmDbOutputStream.close()
    }

    private fun getListOfPeopleToRecover(): Flowable<Person> =
        when (group) {
            Constants.GROUP.GLOBAL -> localDbManager.loadPeopleFromLocalRx()
            Constants.GROUP.USER -> localDbManager.loadPeopleFromLocalRx(userId = userId)
            Constants.GROUP.MODULE -> localDbManager.loadPeopleFromLocalRx(moduleId = moduleId)
        }

    private fun convertPersonToJsonString(person: Person): String =
        JsonHelper.gson.toJson(person.toJsonPerson())

    private fun PipedOutputStream.write(string: String) =
        write(string.toByteArray())

    private fun generateFirebaseStorageFileReference(): StorageReference {
        // Get a reference to [bucket]/recovered-realm-dbs/[projectId]/[userId]/[dbManager-name].json
        val storage = firebaseManagerImpl.getFirebaseStorageInstance()
        val rootRef = storage.getReferenceFromUrl(storageBucketUrl)
        val recoveredRealmDbsRef = rootRef.child(recoveredDbDirName)
        val projectPathRef = recoveredRealmDbsRef.child(projectId)
        val userPathRef = projectPathRef.child(userId)
        return userPathRef.child(getRecoveredDbFileName())
    }

    private fun generateMetaData(): StorageMetadata =
        StorageMetadata.Builder()
            .setCustomMetadata(metaDataProjectIdKey, projectId)
            .setCustomMetadata(metaDataUserIdKey, userId)
            .setCustomMetadata(metaDataAndroidIdKey, androidId)
            .build()

    private fun readAllPeopleIntoInputStream(fileReference: StorageReference, metadata: StorageMetadata) {
        fileReference.putStream(realmDbInputStream, metadata)
            .addOnProgressListener { handleUploadTaskProgress(it) }
            .addOnSuccessListener { handleUploadTaskSuccess() }
            .addOnFailureListener { handleUploadTaskFailure(it) }
    }

    private fun handleUploadTaskProgress(taskSnapshot: UploadTask.TaskSnapshot) {
        Timber.d("Realm DB upload progress: ${taskSnapshot.bytesTransferred}")
    }

    private fun handleUploadTaskSuccess() {
        closeInputStream()
        resultEmitter.onComplete()
    }

    private fun handleUploadTaskFailure(e: Exception) {
        closeInputStream()
        resultEmitter.onError(LocalDbRecoveryFailedException("Failed to upload file to storage", e))
    }

    private fun closeInputStream() =
        try {
            realmDbInputStream.close()
        } catch (e: IOException) {
            throw LocalDbRecoveryFailedException("Failed to close the input stream", e)
        }

    companion object {
        private const val storageBucketUrl = "gs://${BuildConfig.GCP_PROJECT}-firebase-storage/"
        private const val recoveredDbDirName = "recovered-realm-dbs"
        private const val recoveredDbFileName = "recovered-realm-dbManager"
        private fun getRecoveredDbFileName() = "$recoveredDbFileName-${Utils.now().time}"

        private const val metaDataProjectIdKey = "projectId"
        private const val metaDataUserIdKey = "userId"
        private const val metaDataAndroidIdKey = "androidId"

        private const val peopleJsonKey = "patients"
    }
}
