package com.simprints.id.data.secure

import com.simprints.core.security.LocalDbKey
import com.simprints.core.security.LocalDbKeyProvider

@Deprecated("Use SecureLocalDbKeyProvider")
interface LegacyLocalDbKeyProvider : LocalDbKeyProvider {

    override fun getLocalDbKeyOrThrow(projectId: String): LocalDbKey
    fun removeLocalDbKeyForProjectId(projectId: String)
}
