package com.simprints.id.data.secure

import com.simprints.id.exceptions.ApiKeyNotFoundException

class SecureDataManagerImpl : SecureDataManager {

    override var apiKey: String = ""
        get() {
            if (field.isBlank()) {
                throw ApiKeyNotFoundException()
            }
            return field
        }

}

