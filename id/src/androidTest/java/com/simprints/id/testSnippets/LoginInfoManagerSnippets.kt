package com.simprints.id.testSnippets

import com.nhaarman.mockito_kotlin.doReturn
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.shared.anyNotNull

fun setupLoginInfoToBeSignedIn(loginInfoManagerSpy: LoginInfoManager,
                               projectId: String, userId: String) {
    doReturn(projectId).`when`(loginInfoManagerSpy).getSignedInProjectIdOrEmpty()
    doReturn(userId).`when`(loginInfoManagerSpy).getSignedInUserIdOrEmpty()
    doReturn(projectId).`when`(loginInfoManagerSpy).getEncryptedProjectSecretOrEmpty()
    doReturn(true).`when`(loginInfoManagerSpy).isProjectIdSignedIn(anyNotNull())
}
