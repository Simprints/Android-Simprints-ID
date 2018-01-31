package com.simprints.id.data.secure

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.exceptions.unsafe.ApiKeyNotFoundError
import com.simprints.id.exceptions.unsafe.ProjectCredentialsNonValidError


class SecureDataManagerImpl(override var prefs: ImprovedSharedPreferences) : SecureDataManager {

    companion object {
        private const val PROJECT_SECRET: String = "PROJECT_SECRET"
        private const val PROJECT_ID: String = "PROJECT_SECRET"
        private const val PROJECT_SECRET_AND_ID_DEFAULT: String = ""
    }

    override var projectSecret: String = ""
        get() {
            val value = prefs.getPrimitive(PROJECT_SECRET, PROJECT_SECRET_AND_ID_DEFAULT)
            if (value.isBlank()) {
                throw ProjectCredentialsNonValidError()
            }
            return value
        }
        set(value) {
            field = value
            prefs.edit().putPrimitive(PROJECT_SECRET, field).commit()
        }

    override fun getProjectSecretOrEmpty(): String =
        try {
            projectSecret
        } catch (e: Error) {
            ""
        }

    override var projectId: String = ""
        get() {
            val value = prefs.getPrimitive(PROJECT_ID, PROJECT_SECRET_AND_ID_DEFAULT)
            if (value.isBlank()) {
                throw ProjectCredentialsNonValidError()
            }
            return value
        }
        set(value) {
            field = value
            prefs.edit().putPrimitive(PROJECT_SECRET, field).commit()
        }

    override fun getProjectIdOrEmpty(): String =
        try {
            projectId
        } catch (e: Error) {
            ""
        }

    override fun areProjectCredentialsMissing(): Boolean {
        return getProjectIdOrEmpty() == "" || getProjectSecretOrEmpty() != ""
    }

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
