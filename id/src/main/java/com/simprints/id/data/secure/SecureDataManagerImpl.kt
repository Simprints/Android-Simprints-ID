package com.simprints.id.data.secure

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.exceptions.unsafe.ApiKeyNotFoundError
import com.simprints.id.exceptions.unsafe.ProjectCredentialsNonValidError
import java.util.*


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
            try {
                // Probably we won't need this check in the future. We will validate it with server
                UUID.fromString(value)
                field = value
                prefs.edit().putPrimitive(PROJECT_SECRET, field).commit()
            } catch (e: Exception) {
                throw ProjectCredentialsNonValidError()
            }
        }

    override fun getProjectSecretOrEmpty(): String {
        var key = ""
        try {
            key = projectSecret
        } finally {
            return key
        }
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
            try {
                // Probably we won't need this check in the future. We will validate it with server
                UUID.fromString(value)
                field = value
                prefs.edit().putPrimitive(PROJECT_SECRET, field).commit()
            } catch (e: Exception) {
                throw ProjectCredentialsNonValidError()
            }
        }

    override fun getProjectIdOrEmpty(): String {
        var key = ""
        try {
            key = projectId
        } finally {
            return key
        }
    }

    override fun areProjectCredentialsStore(): Boolean {
        return getProjectIdOrEmpty() != "" && getProjectSecretOrEmpty() != ""
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
