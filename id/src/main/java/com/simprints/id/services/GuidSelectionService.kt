package com.simprints.id.services

import android.app.IntentService
import android.content.Intent
import com.simprints.id.Application
import com.simprints.id.data.DataManager
import com.simprints.id.exceptions.safe.NotSignedInException
import com.simprints.id.exceptions.unsafe.InvalidCalloutParameterError
import com.simprints.id.secure.cryptography.Hasher
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
            dataManager.logGuidSelectionService("", "", "", false)
        }
    }

    private fun onHandleNonNullIntent(intent: Intent) {
        val apiKey = intent.parseApiKey()
        val projectId = intent.parseProjectId()
        val sessionId = intent.parseSessionId()
        val selectedGuid = intent.parseSelectedGuid()
        val callbackSent = try {
            checkCalloutParameters(projectId, apiKey, sessionId, selectedGuid)
            dataManager.updateIdentification(dataManager.getSignedInProjectIdOrEmpty(), selectedGuid)
            true
        } catch (error: InvalidCalloutParameterError) {
            dataManager.logError(error)
            false
        } catch (e: NotSignedInException) {
            false
        }
        dataManager.logGuidSelectionService(dataManager.getSignedInProjectIdOrEmpty(),
            sessionId ?: "", selectedGuid, callbackSent)
    }

    private fun Intent.parseApiKey(): String? =
        this.getStringExtra(SIMPRINTS_API_KEY)

    private fun Intent.parseProjectId(): String? =
        this.getStringExtra(SIMPRINTS_PROJECT_ID)

    private fun Intent.parseSessionId(): String? =
        this.getStringExtra(SIMPRINTS_SESSION_ID)

    private fun Intent.parseSelectedGuid(): String =
        this.getStringExtra(SIMPRINTS_SELECTED_GUID) ?: "null"

    private fun checkCalloutParameters(projectId: String?, apiKey: String?, sessionId: String?, selectedGuid: String) {
        checkProjectIdOrApiKey(projectId, apiKey)
        checkSessionId(sessionId)
        checkSelectedGuid(selectedGuid)
    }

    private fun checkSessionId(sessionId: String?) {
        if (sessionId == null || sessionId != dataManager.sessionId) {
            throw InvalidCalloutParameterError.forParameter(SIMPRINTS_SESSION_ID)
        }
    }

    private fun checkProjectIdOrApiKey(projectId: String?, apiKey: String?) =
        when {
            projectId != null -> checkProjectId(projectId)
            apiKey != null -> checkApiKey(apiKey)
            else -> throw InvalidCalloutParameterError.forParameter(SIMPRINTS_PROJECT_ID)
        }

    private fun checkProjectId(projectId: String) {
        if (!dataManager.isProjectIdSignedIn(projectId)) throw NotSignedInException()
    }

    private fun checkApiKey(apiKey: String) {
        val potentialProjectId = dataManager.getProjectIdForHashedLegacyProjectIdOrEmpty(Hasher().hash(apiKey))
        if (!dataManager.isProjectIdSignedIn(potentialProjectId)) throw NotSignedInException()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun checkSelectedGuid(selectedGuid: String) {
        // For now, any selected guid is valid
    }
}
