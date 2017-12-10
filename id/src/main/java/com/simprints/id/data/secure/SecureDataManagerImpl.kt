package com.simprints.id.data.secure

import com.simprints.id.data.DataManager

class SecureDataManagerImpl : SecureDataManager {

    override var apiKey: String = ""
        get() {
            if (field.isBlank()) {
                throw ApiKeyNotFoundException()
            }
            return field
        }

    override fun getApiKeyOrDefault(dataManager: DataManager): String {
        return try {
            apiKey
        } catch (ex: ApiKeyNotFoundException) {
            dataManager.logException(ex)
            ""
        }
    }

}

class ApiKeyNotFoundException(override var message: String = "") : RuntimeException()
