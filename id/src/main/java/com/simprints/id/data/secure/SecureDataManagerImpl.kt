package com.simprints.id.data.secure

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.exceptions.safe.ProjectCredentialsMissingException

class SecureDataManagerImpl(override var prefs: ImprovedSharedPreferences) : SecureDataManager {

    companion object {
         const val ENCRYPTED_PROJECT_SECRET: String = "ENCRYPTED_PROJECT_SECRET"
         const val PROJECT_ID: String = "PROJECT_ID"
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

    override fun isProjectIdSignedIn(possibleProjectId: String): Boolean =
        !getSignedInProjectIdOrEmpty().isEmpty() && getSignedInProjectIdOrEmpty() == possibleProjectId && !getEncryptedProjectSecretOrEmpty().isEmpty()

    override fun cleanCredentials() {

        //TODO: SecureDataManager doesn't support multiple projects signed in.
        val possibleLegacyApiKey = prefs.getPrimitive(signedInProjectId, "")
        prefs.edit().putPrimitive(signedInProjectId, "").commit()
        prefs.edit().putPrimitive(possibleLegacyApiKey, "").commit()

        encryptedProjectSecret = ""
        signedInProjectId = ""
    }

    override fun storeProjectIdWithLegacyApiKeyPair(projectId: String, legacyApiKey: String?) {
        if (legacyApiKey != null && legacyApiKey.isNotEmpty()) {

            //TODO: to be refactored when SecureDataManager will support multiple projects
            prefs.edit().putPrimitive(legacyApiKey, projectId).commit()
            prefs.edit().putPrimitive(projectId, legacyApiKey).commit()
        }
    }

    override fun projectIdForLegacyApiKeyOrEmpty(legacyApiKey: String): String {
        return prefs.getString(legacyApiKey, "")
    }
}
