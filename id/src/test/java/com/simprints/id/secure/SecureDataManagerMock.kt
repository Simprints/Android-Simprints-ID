package com.simprints.id.secure

import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
import com.simprints.id.testUtils.whenever
import org.mockito.Mockito

inline fun mockSecureDataManager(encryptedProjectSecret: String = "encryptedProjectSecret",
                                 projectId: String = "project_id",
                                 signedInUserId: String = "signedInUserId"): LoginInfoManager {

    val mockSecureDataManager = Mockito.spy(LoginInfoManager::class.java)
    whenever(mockSecureDataManager.encryptedProjectSecret).thenReturn(encryptedProjectSecret)
    whenever(mockSecureDataManager.signedInProjectId).thenReturn(projectId)
    whenever(mockSecureDataManager.signedInUserId).thenReturn(signedInUserId)
    return mockSecureDataManager
}
