package com.simprints.id.data.db.dbRecovery

import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.simprints.id.BuildConfig
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.remote.FirebaseManager
import com.simprints.id.data.db.remote.tools.Utils
import com.simprints.id.domain.Constants
import com.simprints.id.exceptions.safe.data.db.LocalDbRecoveryFailedException
import com.simprints.id.tools.json.JsonHelper
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import org.json.JSONException
import timber.log.Timber
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream

class LocalDbRecovererImpl(private val localDbManager: LocalDbManager,
                           private val firebaseManager: FirebaseManager,
                           private val projectId: String,
                           private val userId: String,
                           private val androidId: String,
                           private val moduleId: String,
                           private val group: Constants.GROUP) :
    LocalDbRecoverer {

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
            val people = getListOfPeopleToRecover()
            val jsonString = convertListOfPeopleToJsonString(people)
            realmDbOutputStream.write(jsonString)
            realmDbOutputStream.close()
        } catch (e: JSONException) {
            resultEmitter.onError(LocalDbRecoveryFailedException("Failed to convert people list into JSON", e))
        } catch (e: IOException) {
            resultEmitter.onError(LocalDbRecoveryFailedException("Failed to write to the output stream", e))
        }

    private fun getListOfPeopleToRecover(): ArrayList<rl_Person> =
        when (group) {
            Constants.GROUP.GLOBAL -> localDbManager.loadPeopleFromLocal()
            Constants.GROUP.USER -> localDbManager.loadPeopleFromLocal(userId = userId)
            Constants.GROUP.MODULE -> localDbManager.loadPeopleFromLocal(moduleId = moduleId)
        }

    private fun convertListOfPeopleToJsonString(people: ArrayList<rl_Person>): String =
        JsonHelper.gson.toJson(hashMapOf(peopleJsonKey to people))

    private fun PipedOutputStream.write(string: String) =
        write(string.toByteArray())

    private fun generateFirebaseStorageFileReference(): StorageReference {
        // Get a reference to [bucket]/recovered-realm-dbs/[projectId]/[userId]/[db-name].json
        val storage = firebaseManager.getFirebaseStorageInstance()
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
        private const val recoveredDbFileName = "recovered-realm-db"
        private fun getRecoveredDbFileName() = "$recoveredDbFileName-${Utils.now().time}"

        private const val metaDataProjectIdKey = "projectId"
        private const val metaDataUserIdKey = "userId"
        private const val metaDataAndroidIdKey = "androidId"

        private const val peopleJsonKey = "people"
    }
}
