package com.simprints.id

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.internal.LinkedTreeMap
import com.simprints.remoteadminclient.ApiException
import com.simprints.remoteadminclient.Configuration
import com.simprints.remoteadminclient.api.DefaultApi
import com.simprints.remoteadminclient.auth.ApiKeyAuth
import com.squareup.okhttp.OkHttpClient

import java.util.concurrent.TimeUnit

object RemoteAdminUtils {

    private val FIREBASE_PROJECTS_NODE = "projects"
    private val FIREBASE_PATIENTS_NODE = "patients"

    // TODO: Hide that better
    private val REMOTE_ADMIN_API_KEY = "AIzaSyD4j8zfttMqRdAbfxnQ-py-19QqWM--gss"

    val configuredApiInstance: DefaultApi
        get() {
            val apiInstance = DefaultApi()
            val okhttpClient = Configuration.getDefaultApiClient().httpClient
            okhttpClient.setConnectTimeout(2, TimeUnit.MINUTES)
            okhttpClient.setReadTimeout(2, TimeUnit.MINUTES)
            okhttpClient.setWriteTimeout(2, TimeUnit.MINUTES)
            okhttpClient.retryOnConnectionFailure = true
            val apiKeyAuth = Configuration.getDefaultApiClient().getAuthentication("ApiKeyAuth") as ApiKeyAuth
            apiKeyAuth.apiKey = REMOTE_ADMIN_API_KEY
            return apiInstance
        }

    @JvmStatic
    @Throws(ApiException::class)
    fun getPatientsNode(apiInstance: DefaultApi, projectApiKey: String): JsonObject {
        val patientsFirebaseNode = apiInstance.getAny("/$FIREBASE_PROJECTS_NODE/$projectApiKey/$FIREBASE_PATIENTS_NODE") as LinkedTreeMap<*, *>
        return Gson().toJsonTree(patientsFirebaseNode).asJsonObject
    }

    @JvmStatic
    @Throws(ApiException::class)
    fun clearProjectNode(apiInstance: DefaultApi, projectApiKey: String) {
        apiInstance.deleteAny("/$FIREBASE_PROJECTS_NODE/$projectApiKey")
    }
}
