package com.simprints.id.data.db.remote

import android.content.Context
import com.google.firebase.FirebaseOptions
import com.simprints.id.BuildConfig
import com.simprints.id.exceptions.unsafe.GoogleServicesJsonInvalidError
import com.simprints.id.exceptions.unsafe.GoogleServicesJsonNotFoundError
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class FirebaseOptionsHelper(private val context: Context) {

    fun getLegacyFirebaseOptions(): FirebaseOptions =
        getFirebaseOptionsFromGoogleServicesJson(legacyGoogleServicesJsonName)

    fun getFirestoreFirebaseOptions(): FirebaseOptions =
        getFirebaseOptionsFromGoogleServicesJson(firestoreGoogleServicesJsonName)

    private fun getFirebaseOptionsFromGoogleServicesJson(jsonName: String): FirebaseOptions {
        val json = getJsonFromGoogleServicesFile(jsonName)
        val clientNode = getClientNode(json)
        val builder = FirebaseOptions.Builder()
        populateFirebaseOptions(builder, json, clientNode)
        return builder.build()
    }

    private fun getJsonFromGoogleServicesFile(jsonName: String): JSONObject {
        val id = context.resources.getIdentifier(jsonName, rawResourcesName, context.packageName)
        if (isResourceIdMissing(id)) {
            throw GoogleServicesJsonNotFoundError.forFile(jsonName)
        }
        val text = context.resources.openRawResource(id).bufferedReader().use { it.readText() }
        return JSONObject(text)
    }

    private fun isResourceIdMissing(id: Int) = id == 0

    private fun getClientNode(json: JSONObject): JSONObject {
        val clientArray = json.doOrThrow {
            getJSONArray(clientNode)
        }
        return findClientNodeOrThrow(clientArray)
    }

    private fun findClientNodeOrThrow(clientArray: JSONArray): JSONObject =
        try {
            clientArray
                .iterateOnObjects()
                .single { it.hasPackageName(context.packageName) }
        } catch (e: Throwable) {
            throw GoogleServicesJsonInvalidError()
        }

    private fun JSONArray.iterateOnObjects(): List<JSONObject> =
        (0 until length())
            .map { index -> getJSONObject(index) }

    private fun JSONObject.hasPackageName(packageName: String): Boolean {
        val clientPackageName = this.doOrThrow {
            getJSONObject(clientInfoNode).getJSONObject(androidClientInfoNode).getString(packageNameNode)
        }
        return clientPackageName == packageName
    }

    private fun populateFirebaseOptions(builder: FirebaseOptions.Builder, json: JSONObject, clientNode: JSONObject) {
        populateApiKey(builder, clientNode)
        populateApplicationId(builder, clientNode)
        populateDatabaseUrl(builder, json)
        populateGcmSenderId(builder, json)
        populateProjectId(builder, json)
        populateStorageBucket(builder, json)
    }

    private fun populateApiKey(builder: FirebaseOptions.Builder, clientNode: JSONObject) {
        val apiKey = clientNode.doOrThrow {
            getJSONArray(apiKeyNode).getJSONObject(0).getString(currentApiKeyNode)
        }
        builder.setApiKey(apiKey)
    }

    private fun populateApplicationId(builder: FirebaseOptions.Builder, clientNode: JSONObject) {
        val appId = clientNode.doOrThrow {
            getJSONObject(clientInfoNode).getString(applicationIdNode)
        }
        builder.setApplicationId(appId)
    }

    private fun populateDatabaseUrl(builder: FirebaseOptions.Builder, json: JSONObject) {
        val databaseUrl = json.doOrThrow {
            getJSONObject(projectInfoNode).getString(firebaseUrlNode)
        }
        builder.setDatabaseUrl(databaseUrl)
    }

    private fun populateGcmSenderId(builder: FirebaseOptions.Builder, json: JSONObject) {
        val gcmSenderId = json.doOrThrow {
            getJSONObject(projectInfoNode).getString(projectNumberNode)
        }
        builder.setGcmSenderId(gcmSenderId)
    }

    private fun populateProjectId(builder: FirebaseOptions.Builder, json: JSONObject) {
        val projectId = json.doOrThrow {
            getJSONObject(projectInfoNode).getString(projectIdNode)
        }
        builder.setProjectId(projectId)
    }

    private fun populateStorageBucket(builder: FirebaseOptions.Builder, json: JSONObject) {
        val storageBucket = json.doOrThrow {
            getJSONObject(projectInfoNode).getString(storageBucketNode)
        }
        builder.setStorageBucket(storageBucket)
    }

    private fun <T : Any> JSONObject.doOrThrow(operation: JSONObject.() -> T): T =
        try {
            this.operation()
        } catch (e: JSONException) {
            throw GoogleServicesJsonInvalidError()
        }

    companion object {

        private val legacyGoogleServicesJsonName =
            "${BuildConfig.GCP_PROJECT}-google-services".replace("-", "_")

        private val firestoreGoogleServicesJsonName =
            "${BuildConfig.GCP_PROJECT}-fs-google-services".replace("-", "_")

        private const val rawResourcesName = "raw"

        private const val projectInfoNode = "project_info"
        private const val firebaseUrlNode = "firebase_url"
        private const val projectNumberNode = "project_number"
        private const val projectIdNode = "project_id"
        private const val storageBucketNode = "storage_bucket"

        private const val clientNode = "client"
        private const val clientInfoNode = "client_info"
        private const val androidClientInfoNode = "android_client_info"
        private const val packageNameNode = "package_name"
        private const val applicationIdNode = "mobilesdk_app_id"
        private const val apiKeyNode = "api_key"
        private const val currentApiKeyNode = "current_key"

    }
}
