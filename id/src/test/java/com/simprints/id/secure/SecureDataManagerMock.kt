package com.simprints.id.secure

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.secure.SecureDataManager

class SecureDataManagerMock : SecureDataManager {

    override var encryptedProjectSecret: String
        get() = "encrypted_project_secret"
        set(value) {}

    override var projectId: String
        get() = "project_id"
        set(value) {}

    override var prefs: ImprovedSharedPreferences
        get() = throw RuntimeException("Not mocked!!!")
        set(value) {}

    override fun getEncryptedProjectSecretOrEmpty(): String {
        throw RuntimeException("Not mocked!!!")
    }

    override fun getProjectIdOrEmpty(): String {
        throw RuntimeException("Not mocked!!!")
    }

    override fun areProjectCredentialsMissing(): Boolean {
        throw RuntimeException("Not mocked!!!")
    }

    override var apiKey: String
        get() = throw RuntimeException("Not mocked!!!")
        set(value) {}

    override fun getApiKeyOr(default: String): String {
        throw RuntimeException("Not mocked!!!")
    }
}
