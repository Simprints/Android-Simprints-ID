package com.simprints.id.data.db

import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.simprints.id.BuildConfig
import com.simprints.id.data.db.remote.FirebaseManager
import com.simprints.libdata.DATA_ERROR
import com.simprints.libdata.DataCallback
import com.simprints.libdata.models.realm.RealmConfig
import com.simprints.libdata.models.realm.rl_Person
import com.simprints.libdata.tools.Constants
import com.simprints.libdata.tools.Utils
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import org.json.JSONException
import timber.log.Timber
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream

class LocalDbRecovererImpl(private val firebaseManager: FirebaseManager,
                           private val projectId: String,
                           private val userId: String,
                           private val androidId: String,
                           private val moduleId: String,
                           private val group: Constants.GROUP,
                           callback: DataCallback) :
    LocalDbRecoverer {

    private val wrappedCallback = Utils.wrapCallback("FirebaseManager.recoverRealmDb", callback)

    private lateinit var realm: Realm
    private lateinit var request: RealmResults<rl_Person>

    private val realmDbInputStream = PipedInputStream()
    private val realmDbOutputStream = PipedOutputStream()

    override fun recoverDb() {
        Timber.d("LocalDbRecovererImpl.recoverDb()")

        initialiseRealmAndWriteToOutputStream()
        initialiseFirebaseStorageFileReferenceAndReadFromInputStream()
    }

    private fun initialiseRealmAndWriteToOutputStream() {
        getRealmInstance()
        request = getRealmRequest()
        connectTheStreams()
        request.addChangeListener(realmChangeListener)
    }

    private fun getRealmInstance() {
        // Create a new Realm instance - needed since this should rn on a background thread
        realm = Realm.getInstance(RealmConfig.get(projectId))
    }

    private fun getRealmRequest(): RealmResults<rl_Person> {
        return when (group) {
            Constants.GROUP.GLOBAL -> realm.where(rl_Person::class.java).findAllAsync()
            Constants.GROUP.USER -> realm.where(rl_Person::class.java).equalTo("userId", userId).findAllAsync()
            Constants.GROUP.MODULE -> realm.where(rl_Person::class.java).equalTo("moduleId", moduleId).findAllAsync()
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
        writeStartOfOutputStream()
        results
            .mapIndexed { count, person -> convertPersonToJsonString(person, count) }
            .forEach { writePersonToOutputStream(it) }
        writeEndOfOutputStream()
        closeRealmAndOutputStream()
    }

    private fun convertPersonToJsonString(person: rl_Person, count: Int): String {
        val personJson = person.jsonPerson
        var commaString = ","
        // We need commas before all entries except the first
        if (count == 0) {
            commaString = ""
        }
        val nodeString = "\"" + personJson.get("patientId").toString() + "\"" + ":"
        return commaString + nodeString + person.jsonPerson.toString()
    }

    private fun writeStartOfOutputStream() {
        realmDbOutputStream.write("{".toByteArray())
    }

    private fun writeEndOfOutputStream() {
        realmDbOutputStream.write("}".toByteArray())
    }

    private fun writePersonToOutputStream(personJsonString: String) {
        realmDbOutputStream.write(personJsonString.toByteArray())
    }

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
        try {
            realmDbInputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        wrappedCallback.onSuccess()
    }

    private fun handleUploadTaskFailure(e: Exception) {
        e.printStackTrace()
        try {
            realmDbInputStream.close()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        wrappedCallback.onFailure(DATA_ERROR.FAILED_TO_UPLOAD)
    }

    companion object {
        private const val storageBucketUrl = "gs://${BuildConfig.GCP_PROJECT}-firebase-storage/"
    }
}
