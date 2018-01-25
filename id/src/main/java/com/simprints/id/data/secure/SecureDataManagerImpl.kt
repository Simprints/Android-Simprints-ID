package com.simprints.id.data.secure

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.exceptions.unsafe.ApiKeyNotFoundError
import com.simprints.id.exceptions.unsafe.ProjectKeyNonValid
import java.util.*


class SecureDataManagerImpl(override var prefs: ImprovedSharedPreferences) : SecureDataManager {

    companion object {
        private const val PROJECT_KEY: String = "PROJECT_KEY"
        private const val PROJECT_KEY_DEFAULT: String = ""
    }

    override var projectKey: String = ""
        get() {
            val key = prefs.getPrimitive(PROJECT_KEY, PROJECT_KEY_DEFAULT)
            if (key.isBlank()) {
                throw ProjectKeyNonValid()
            }
            return key
        }
        set(value) {
            try {
                UUID.fromString(value)
                field = value
                prefs.edit().putPrimitive(PROJECT_KEY, field).commit()
            } catch (e: Exception) {
                throw ProjectKeyNonValid()
            }
        }

    override fun getProjectKeyOrEmpty(): String {
        var key = ""
        try {
            key = projectKey
        } finally {
            return key
        }
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
