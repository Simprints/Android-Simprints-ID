package com.simprints.id.data.secure

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.exceptions.safe.CredentialMissingException
import com.simprints.id.secure.cryptography.Hasher

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

    override fun getSignedInHashedLegacyApiKeyOrEmpty(): String = getHashedLegacyProjectIdForProjectIdOrEmpty(getSignedInProjectIdOrEmpty())

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
        val possibleMd5LegacyApiKey = prefs.getPrimitive(projectId, "")
        prefs.edit().putPrimitive(projectId, "").commit()
        prefs.edit().putPrimitive(possibleMd5LegacyApiKey, "").commit()

        encryptedProjectSecret = ""
        signedInProjectId = ""
        signedInUserId = ""
    }

    override fun storeProjectIdWithLegacyProjectIdPair(projectId: String, legacyProjectId: String?) {
        if (legacyProjectId != null && legacyProjectId.isNotEmpty()) {
            val hashedLegacyApiKey = Hasher.hash(legacyProjectId)
            prefs.edit().putPrimitive(hashedLegacyApiKey, projectId).commit()
            prefs.edit().putPrimitive(projectId, hashedLegacyApiKey).commit()
        }
    }

    override fun getHashedLegacyProjectIdForProjectIdOrEmpty(projectId: String): String = prefs.getString(projectId, "")
    override fun getProjectIdForHashedLegacyProjectIdOrEmpty(hashedLegacyApiKey: String): String = prefs.getString(hashedLegacyApiKey, "")
}
