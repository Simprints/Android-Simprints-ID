package com.simprints.id.services

import android.app.IntentService
import android.content.Intent

import com.simprints.id.Application
import com.simprints.id.data.DataManager
import com.simprints.id.exceptions.unsafe.InvalidCalloutParameterError
import com.simprints.libsimprints.Constants.*


class GuidSelectionService : IntentService("GuidSelectionService") {

    private lateinit var dataManager: DataManager

    override fun onCreate() {
        super.onCreate()
        val app = application as Application
        dataManager = app.dataManager
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            onHandleNonNullIntent(intent)
        } else {
            dataManager.logGuidSelectionService("","", "", false)
        }
    }

    private fun onHandleNonNullIntent(intent: Intent) {
        val apiKey = intent.parseApiKey()
        val sessionId = intent.parseSessionId()
        val selectedGuid = intent.parseSelectedGuid()
        val callbackSent = try {
            checkCalloutParameters(apiKey, sessionId, selectedGuid)
            dataManager.updateIdentification(apiKey, selectedGuid)
            true
        } catch (error: InvalidCalloutParameterError) {
            dataManager.logError(error)
            false
        }
        dataManager.logGuidSelectionService(apiKey, sessionId, selectedGuid, callbackSent)
    }

    private fun Intent.parseApiKey(): String =
        this.getStringExtra(SIMPRINTS_API_KEY) ?: ""

    private fun Intent.parseSessionId(): String =
        this.getStringExtra(SIMPRINTS_SESSION_ID) ?: ""

    private fun Intent.parseSelectedGuid(): String =
        this.getStringExtra(SIMPRINTS_SELECTED_GUID) ?: "null"

    private fun checkCalloutParameters(apiKey: String, sessionId: String, selectedGuid: String) {
        checkApiKey(apiKey)
        checkSessionId(sessionId)
        checkSelectedGuid(selectedGuid)
    }

    private fun checkSessionId(sessionId: String) {
        if (sessionId.isEmpty() || sessionId != dataManager.sessionId) {
            throw InvalidCalloutParameterError.forParameter(SIMPRINTS_SESSION_ID)
        }
    }

    private fun checkApiKey(apiKey: String) {
        if (apiKey.isEmpty() || apiKey != dataManager.getApiKeyOr(apiKey)) {
            throw InvalidCalloutParameterError.forParameter(SIMPRINTS_API_KEY)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun checkSelectedGuid(selectedGuid: String) {
        // For now, any selected guid is valid
    }

}
