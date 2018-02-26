package com.simprints.id.data.secure

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.exceptions.safe.CredentialMissingException

class SecureDataManagerImpl(override var prefs: ImprovedSharedPreferences) : SecureDataManager {

    companion object {
        const val ENCRYPTED_PROJECT_SECRET: String = "ENCRYPTED_PROJECT_SECRET"
        const val PROJECT_ID: String = "PROJECT_ID"
        const val USER_ID: String = "USER_ID"
        private const val DEFAULT_VALUE: String = ""
    }

    override var encryptedProjectSecret: String = ""
        get() {
            val value = prefs.getPrimitive(ENCRYPTED_PROJECT_SECRET, DEFAULT_VALUE)
            if (value.isBlank()) {
                throw CredentialMissingException()
            }
            return value
        }
        set(value) {
            field = value
            prefs.edit().putPrimitive(ENCRYPTED_PROJECT_SECRET, field).commit()
        }

    override var signedInProjectId: String = ""
        get() {
            val value = prefs.getPrimitive(PROJECT_ID, DEFAULT_VALUE)
            if (value.isBlank()) {
                throw CredentialMissingException()
            }
            return value
        }
        set(value) {
            field = value
            prefs.edit().putPrimitive(PROJECT_ID, field).commit()
        }

    override var signedInUserId: String = ""
        get() {
            val value = prefs.getPrimitive(USER_ID, DEFAULT_VALUE)
            if (value.isBlank()) {
                throw CredentialMissingException()
            }
            return value
        }
        set(value) {
            field = value
            prefs.edit().putPrimitive(USER_ID, field).commit()
        }

    override fun getEncryptedProjectSecretOrEmpty(): String =
        try {
            encryptedProjectSecret
        } catch (e: CredentialMissingException) {
            ""
        }

    override fun getSignedInProjectIdOrEmpty(): String =
        try {
            signedInProjectId
        } catch (e: CredentialMissingException) {
            ""
        }

    override fun getSignedInUserIdOrEmpty(): String =
        try {
            signedInUserId
        } catch (e: CredentialMissingException) {
            ""
        }

    override fun isProjectIdSignedIn(possibleProjectId: String): Boolean =
        getSignedInProjectIdOrEmpty().isNotEmpty() &&
            getSignedInProjectIdOrEmpty() == possibleProjectId &&
            getEncryptedProjectSecretOrEmpty().isNotEmpty()

    override fun cleanCredentials() {

        val projectId = getSignedInProjectIdOrEmpty()
        val possibleLegacyApiKey = prefs.getPrimitive(projectId, "") // FIXME
        prefs.edit().putPrimitive(projectId, "").commit()
        prefs.edit().putPrimitive(possibleLegacyApiKey, "").commit()

        encryptedProjectSecret = ""
        signedInProjectId = ""
        signedInUserId = ""
    }

    override fun storeProjectIdWithLegacyApiKeyPair(projectId: String, legacyApiKey: String?) {
        if (legacyApiKey != null && legacyApiKey.isNotEmpty()) {

            //TODO: to be refactored when SecureDataManager will support multiple projects
            prefs.edit().putPrimitive(legacyApiKey, projectId).commit()
            prefs.edit().putPrimitive(projectId, legacyApiKey).commit()
        }
    }

    override fun legacyApiKeyForProjectIdOrEmpty(projectId: String): String = prefs.getString(projectId, "")
    override fun projectIdForLegacyApiKeyOrEmpty(legacyApiKey: String): String = prefs.getString(legacyApiKey, "")
}
