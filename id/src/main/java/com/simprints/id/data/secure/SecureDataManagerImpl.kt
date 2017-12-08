package com.simprints.id.data.secure

class SecureDataManagerImpl : SecureDataManager {

    override var apiKey: String = ""
        get() {
            if (field.isBlank()) {
                throw NullPointerException()
            }
            return field
        }


    override fun getApiKeyOrDefault(): String {
        return try {
            apiKey
        } catch (nullPointerException: NullPointerException) {
            ""
        }
    }

}
