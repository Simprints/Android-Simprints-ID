package com.simprints.id.secure

import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.testUtils.whenever
import org.mockito.Mockito

inline fun mockSecureDataManager(): SecureDataManager {
    val mockSecureDataManager = Mockito.spy(SecureDataManager::class.java)
    whenever(mockSecureDataManager.encryptedProjectSecret).thenReturn("encrypted_project_secret")
    whenever(mockSecureDataManager.signedInProjectId).thenReturn("project_id")
    whenever(mockSecureDataManager.signedInUserId).thenReturn("user_id")
    return mockSecureDataManager
}
