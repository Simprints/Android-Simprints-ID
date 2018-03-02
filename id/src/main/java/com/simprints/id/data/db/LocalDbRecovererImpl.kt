package com.simprints.id.data.db

import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.simprints.id.BuildConfig
import com.simprints.id.data.db.local.RealmDbManager
import com.simprints.id.data.db.remote.FirebaseManager
import com.simprints.id.libdata.DATA_ERROR
import com.simprints.id.libdata.DataCallback
import com.simprints.id.libdata.models.realm.rl_Person
import com.simprints.id.libdata.tools.Constants
import com.simprints.id.libdata.tools.Utils
import io.realm.RealmChangeListener
import io.realm.RealmResults
import org.json.JSONException
import timber.log.Timber
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream

class LocalDbRecovererImpl(realmManager: RealmDbManager,
                           private val firebaseManager: FirebaseManager,
                           private val projectId: String,
                           private val userId: String,
                           private val androidId: String,
                           private val moduleId: String,
                           private val group: com.simprints.id.libdata.tools.Constants.GROUP,
                           callback: DataCallback) :
    LocalDbRecoverer {

    private val wrappedCallback = com.simprints.id.libdata.tools.Utils.wrapCallback("FirebaseManager.recoverRealmDb", callback)

    private val realm = realmManager.getRealmInstance()
    private lateinit var request: RealmResults<rl_Person>

    private val realmDbInputStream = PipedInputStream()
    private val realmDbOutputStream = PipedOutputStream()

    override fun recoverDb() {
        Timber.d("LocalDbRecovererImpl.recoverDb()")

        initialiseRealmAndWriteToOutputStream()
        initialiseFirebaseStorageFileReferenceAndReadFromInputStream()
    }

    private fun initialiseRealmAndWriteToOutputStream() {
        request = getRealmRequest()
        connectTheStreams()
        request.addChangeListener(realmChangeListener)
    }

    private fun getRealmRequest(): RealmResults<rl_Person> {
        return when (group) {
            com.simprints.id.libdata.tools.Constants.GROUP.GLOBAL -> realm.where(rl_Person::class.java).findAllAsync()
            com.simprints.id.libdata.tools.Constants.GROUP.USER -> realm.where(rl_Person::class.java).equalTo("userId", userId).findAllAsync()
            com.simprints.id.libdata.tools.Constants.GROUP.MODULE -> realm.where(rl_Person::class.java).equalTo("moduleId", moduleId).findAllAsync()
        }
    }

    private fun connectTheStreams() {
        try {
            realmDbOutputStream.connect(realmDbInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private val realmChangeListener = object : RealmChangeListener<RealmResults<rl_Person>> {
        override fun onChange(results: RealmResults<rl_Person>) {
            try {
                request.removeChangeListener(this)
                writeAllPeopleToOutputStream(results)
            } catch (e: JSONException) {
                e.printStackTrace()
                wrappedCallback.onFailure(DATA_ERROR.JSON_ERROR)
            } catch (e: IOException) {
                e.printStackTrace()
                wrappedCallback.onFailure(DATA_ERROR.IO_BUFFER_WRITE_ERROR)
            }
        }
    }

    private fun writeAllPeopleToOutputStream(results: RealmResults<rl_Person>) {
        realmDbOutputStream.write("{")
        results.mapIndexed { count, person -> convertPersonToJsonString(person, count) }
            .forEach { realmDbOutputStream.write(it) }
        realmDbOutputStream.write("}")
        closeRealmAndOutputStream()
    }

    private fun convertPersonToJsonString(person: rl_Person, count: Int): String {
        // We need commas before all entries except the first
        val commaString = if (count == 0) "" else ","
        val patientId = person.jsonPerson.get("patientId").toString()
        val patientValue = person.jsonPerson.toString()
        return "$commaString\"$patientId\":$patientValue"
    }

    private fun PipedOutputStream.write(string: String) =
        write(string.toByteArray())

    private fun closeRealmAndOutputStream() {
        realmDbOutputStream.close()
        realm.close()
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
        wrappedCallback.onSuccess()
    }

    private fun handleUploadTaskFailure(e: Exception) {
        e.printStackTrace()
        closeInputStream()
        wrappedCallback.onFailure(DATA_ERROR.FAILED_TO_UPLOAD)
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
