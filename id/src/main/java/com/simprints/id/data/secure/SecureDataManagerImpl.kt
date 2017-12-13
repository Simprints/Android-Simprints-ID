package com.simprints.id.data.secure

import com.simprints.id.exceptions.unsafe.ApiKeyNotFoundError


class SecureDataManagerImpl : SecureDataManager {

    override var apiKey: String = ""
        get() {
            if (field.isBlank()) {
                throw ApiKeyNotFoundError()
            }
            return field
        }

}
