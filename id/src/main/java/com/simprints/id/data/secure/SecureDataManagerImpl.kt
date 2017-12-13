package com.simprints.id.data.secure

import com.simprints.id.exceptions.unsafe.ApiKeyNotFoundError


class SecureDataManagerImpl : SecureDataManager {

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
