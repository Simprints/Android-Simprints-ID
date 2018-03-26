package com.simprints.id.data.db.dbRecovery

import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.simprints.id.BuildConfig
import com.simprints.id.data.db.DATA_ERROR
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.remote.FirebaseManager
import com.simprints.id.domain.Constants
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

    override fun recoverDb(): Completable {

        return Completable.create {
            resultEmitter = it
            Timber.d("LocalDbRecovererImpl.recoverDb()")

            initialiseRealmAndWriteToOutputStream()
            initialiseFirebaseStorageFileReferenceAndReadFromInputStream()
        }
    }

    private fun initialiseRealmAndWriteToOutputStream() {
        connectTheStreams()
        writeAllPeopleToOutputStream()
    }

    private fun getPeopleToSaveIntoRecover(): ArrayList<rl_Person> {
        return when (group) {
            Constants.GROUP.GLOBAL -> localDbManager.loadPersonsFromLocal()
            Constants.GROUP.USER -> localDbManager.loadPersonsFromLocal(userId = userId)
            Constants.GROUP.MODULE -> localDbManager.loadPersonsFromLocal(moduleId = moduleId)
        }
    }

    private fun connectTheStreams() {
        try {
            realmDbOutputStream.connect(realmDbInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun writeAllPeopleToOutputStream() {
        try {
            val people = getPeopleToSaveIntoRecover()
            val json = JsonHelper.gson.toJson(hashMapOf("patients" to people))
            realmDbOutputStream.write(json)
            closeRealmAndOutputStream()
        } catch (e: JSONException) {
            e.printStackTrace()
            resultEmitter.onError(Throwable(DATA_ERROR.JSON_ERROR.details()))
        } catch (e: IOException) {
            e.printStackTrace()
            resultEmitter.onError(Throwable(DATA_ERROR.IO_BUFFER_WRITE_ERROR.details()))
        }
    }

    private fun PipedOutputStream.write(string: String) =
        write(string.toByteArray())

    private fun closeRealmAndOutputStream() {
        realmDbOutputStream.close()
    }

    private fun initialiseFirebaseStorageFileReferenceAndReadFromInputStream() {
        val fileReference = generateFirebaseStorageFileReference()
        val metadata = generateMetaData()
        val uploadTask = fileReference.putStream(realmDbInputStream, metadata)
        uploadTask.addOnProgressListener { handleUploadTaskProgress(it) }
            .addOnSuccessListener { handleUploadTaskSuccess() }
            .addOnFailureListener { handleUploadTaskFailure(it) }
    }

    private fun generateFirebaseStorageFileReference(): StorageReference {
        // Get a reference to [bucket]/recovered-realm-dbs/[projectId]/[userId]/[db-name].json
        val storage = firebaseManager.getFirebaseStorageInstance()
        val rootRef = storage.getReferenceFromUrl(storageBucketUrl)
        val recoveredRealmDbsRef = rootRef.child("recovered-realm-dbs")
        val projectPathRef = recoveredRealmDbsRef.child(projectId)
        val userPathRef = projectPathRef.child(userId)
        return userPathRef.child("recovered-realm-db")
    }

    private fun generateMetaData(): StorageMetadata =
        StorageMetadata.Builder()
            .setCustomMetadata("projectId", projectId)
            .setCustomMetadata("userId", userId)
            .setCustomMetadata("androidId", androidId)
            .build()

    private fun handleUploadTaskProgress(taskSnapshot: UploadTask.TaskSnapshot) {
        Timber.d("Realm DB upload progress: ${taskSnapshot.bytesTransferred}")
    }

    private fun handleUploadTaskSuccess() {
        closeInputStream()
        resultEmitter.onComplete()
    }

    private fun handleUploadTaskFailure(e: Exception) {
        e.printStackTrace()
        closeInputStream()
        resultEmitter.onError(Throwable(DATA_ERROR.FAILED_TO_UPLOAD.details()))
    }

    private fun closeInputStream() {
        try {
            realmDbInputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val storageBucketUrl = "gs://${BuildConfig.GCP_PROJECT}-firebase-storage/"
    }
}
