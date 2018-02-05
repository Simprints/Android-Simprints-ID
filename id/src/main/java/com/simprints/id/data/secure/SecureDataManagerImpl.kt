package com.simprints.id.data.secure

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.exceptions.unsafe.ApiKeyNotFoundError
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

    override var projectId: String = ""
        get() {
            val value = prefs.getPrimitive(PROJECT_ID, PROJECT_SECRET_AND_ID_DEFAULT)
            if (value.isBlank()) {
                throw ProjectCredentialsMissingException()
            }
            return value
        }
        set(value) {
            field = value
            prefs.edit().putPrimitive(ENCRYPTED_PROJECT_SECRET, field).commit()
        }

    override fun getEncryptedProjectSecretOrEmpty(): String =
        try {
            encryptedProjectSecret
        } catch (e: ProjectCredentialsMissingException) {
            ""
        }

    override fun getProjectIdOrEmpty(): String =
        try {
            projectId
        } catch (e: ProjectCredentialsMissingException) {
            ""
        }

    override fun areProjectCredentialsMissing(): Boolean = getProjectIdOrEmpty() == "" || getEncryptedProjectSecretOrEmpty() == ""

    /*TODO: Legacy stuff to refactor */
    private var apiKeyBackingField: String = ""


    override var apiKey: String
        get() {
            if (apiKeyBackingField.isBlank()) {
                throw ApiKeyNotFoundError()
            }
            return apiKeyBackingField
        }
        set(value) {
            apiKeyBackingField = value
        }

    override fun getApiKeyOr(default: String): String =
        if (apiKeyBackingField.isBlank()) {
            default
        } else {
            apiKeyBackingField
        }
}
