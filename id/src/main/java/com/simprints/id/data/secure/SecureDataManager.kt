package com.simprints.id.data.secure

import com.simprints.id.data.DataManager

interface SecureDataManager {

    var apiKey: String

    fun getApiKeyOrDefault(dataManager: DataManager): String

}
