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

    private lateinit var json: JSONObject
    private lateinit var clientNode: JSONObject
    private val builder = FirebaseOptions.Builder()

    fun getLegacyFirebaseOptions(): FirebaseOptions =
        getFirebaseOptionsFromGoogleServicesJson(legacyGoogleServicesJsonName)

    fun getFirestoreFirebaseOptions(): FirebaseOptions =
        getFirebaseOptionsFromGoogleServicesJson(firestoreGoogleServicesJsonName)

    private fun getFirebaseOptionsFromGoogleServicesJson(jsonName: String): FirebaseOptions {
        json = getJsonFromGoogleServicesFile(jsonName)
        clientNode = getClientNode(json)
        populateFirebaseOptions()
        return builder.build()
    }

    private fun getJsonFromGoogleServicesFile(jsonName: String): JSONObject {
        val id = context.resources.getIdentifier(jsonName, "raw", context.packageName)
        if (isResourceIdMissing(id)) {
            throw GoogleServicesJsonNotFoundError.forFile(jsonName)
        }
        val text = context.resources.openRawResource(id).bufferedReader().use { it.readText() }
        return JSONObject(text)
    }

    private fun isResourceIdMissing(id: Int) = id == 0

    private fun getClientNode(json: JSONObject): JSONObject {
        val clientArray = getFromJsonOrThrow {
            json.getJSONArray("client")
        } as JSONArray

        return findClientNodeOrThrow(clientArray)
    }

    private fun findClientNodeOrThrow(clientArray: JSONArray): JSONObject {
        (0 until (clientArray.length()))
                .map { clientArray.getJSONObject(it) }
                .forEach {
                    val possibleClientPackageName = getFromJsonOrThrow {
                        it.getJSONObject("client_info").getJSONObject("android_client_info").getString("package_name")
                    } as String
                    if (possibleClientPackageName == context.packageName) return it
                }
        throw GoogleServicesJsonInvalidError()
    }

    private fun populateFirebaseOptions() {
        populateApiKey()
        populateApplicationId()
        populateDatabaseUrl()
        populateGcmSenderId()
        populateProjectId()
        populateStorageBucket()
    }

    private fun populateApiKey() {
        val apiKey = getFromJsonOrThrow {
            clientNode.getJSONArray("api_key").getJSONObject(0).getString("current_key")
        } as String
        builder.setApiKey(apiKey)
    }

    private fun populateApplicationId() {
        val appId = getFromJsonOrThrow {
            clientNode.getJSONObject("client_info").getString("mobilesdk_app_id")
        } as String
        builder.setApplicationId(appId)
    }

    private fun populateDatabaseUrl() {
        val databaseUrl = getFromJsonOrThrow {
            json.getJSONObject("project_info").getString("firebase_url")
        } as String
        builder.setDatabaseUrl(databaseUrl)
    }

    private fun populateGcmSenderId() {
        val gcmSenderId = getFromJsonOrThrow {
            json.getJSONObject("project_info").getString("project_number")
        } as String
        builder.setGcmSenderId(gcmSenderId)
    }

    private fun populateProjectId() {
        val projectId = getFromJsonOrThrow {
            json.getJSONObject("project_info").getString("project_id")
        } as String
        builder.setProjectId(projectId)
    }

    private fun populateStorageBucket() {
        val storageBucket = getFromJsonOrThrow {
            json.getJSONObject("project_info").getString("storage_bucket")
        } as String
        builder.setStorageBucket(storageBucket)
    }

    private fun getFromJsonOrThrow(f: () -> Any): Any {
        return try {
            f()
        } catch (e: JSONException) {
            throw GoogleServicesJsonInvalidError()
        }
    }

    companion object {

        private val legacyGoogleServicesJsonName =
            "${BuildConfig.GCP_PROJECT}-google-services".replace("-", "_")

        private val firestoreGoogleServicesJsonName =
            "${BuildConfig.GCP_PROJECT}-fs-google-services".replace("-", "_")
    }
}
