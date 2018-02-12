package com.simprints.id.data.secure

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.exceptions.safe.ProjectCredentialsMissingException

class SecureDataManagerImpl(override var prefs: ImprovedSharedPreferences) : SecureDataManager {

    companion object {
        private const val ENCRYPTED_PROJECT_SECRET: String = "ENCRYPTED_PROJECT_SECRET"
        private const val PROJECT_ID: String = "PROJECT_ID"
        private const val PROJECT_SECRET_AND_ID_DEFAULT: String = ""
    }

    override var encryptedProjectSecret: String = ""
        get() {
            val value = prefs.getPrimitive(ENCRYPTED_PROJECT_SECRET, PROJECT_SECRET_AND_ID_DEFAULT)
            if (value.isBlank()) {
                throw ProjectCredentialsMissingException()
            }
            return value
        }
        set(value) {
            field = value
            prefs.edit().putPrimitive(ENCRYPTED_PROJECT_SECRET, field).commit()
        }

    override var signedInProjectId: String = ""
        get() {
            val value = prefs.getPrimitive(PROJECT_ID, PROJECT_SECRET_AND_ID_DEFAULT)
            if (value.isBlank()) {
                throw ProjectCredentialsMissingException()
            }
            return value
        }
        set(value) {
            field = value
            prefs.edit().putPrimitive(PROJECT_ID, field).commit()
        }

    override fun getEncryptedProjectSecretOrEmpty(): String =
        try {
            encryptedProjectSecret
        } catch (e: ProjectCredentialsMissingException) {
            ""
        }

    override fun getSignedInProjectIdOrEmpty(): String =
        try {
            signedInProjectId
        } catch (e: ProjectCredentialsMissingException) {
            ""
        }

    override fun isProjectIdSignedIn(potentialProjectId: String): Boolean =
        !getSignedInProjectIdOrEmpty().isEmpty() && getSignedInProjectIdOrEmpty() == potentialProjectId && !getEncryptedProjectSecretOrEmpty().isEmpty()

    override fun cleanCredentials() {
        encryptedProjectSecret = ""
        signedInProjectId = ""
        prefs.edit().putMap("projectId_and_apiKey", "").commit()
    }

    override fun storeProjectIdWithLegacyApiKeyPair(projectId: String, possibleLegacyApiKey: String?) {
        if (possibleLegacyApiKey != null) {
            prefs.edit().putMap("projectId_and_apiKey", mapOf(possibleLegacyApiKey to projectId)).commit()
        }
    }

    override fun projectIdForLegacyApiKeyOrEmpty(legacyApiKey: String): String {
        val mapProjectIdAndApiKey = prefs.getMap("projectId_and_apiKey") as Map<String, String>
        return mapProjectIdAndApiKey[legacyApiKey] ?: ""
    }
}
