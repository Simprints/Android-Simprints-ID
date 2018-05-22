package com.simprints.id.secure

import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
import com.simprints.id.shared.whenever
import org.mockito.Mockito

inline fun mockLoginInfoManager(encryptedProjectSecret: String = "encryptedProjectSecret",
                                projectId: String = "project_id",
                                signedInUserId: String = "signedInUserId"): LoginInfoManager {

    val mockLoginInfoManager = Mockito.spy(LoginInfoManager::class.java)
    whenever(mockLoginInfoManager.encryptedProjectSecret).thenReturn(encryptedProjectSecret)
    whenever(mockLoginInfoManager.signedInProjectId).thenReturn(projectId)
    whenever(mockLoginInfoManager.signedInUserId).thenReturn(signedInUserId)
    return mockLoginInfoManager
}
