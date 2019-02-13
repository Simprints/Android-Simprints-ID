package com.simprints.id.testSnippets

import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.testframework.common.syntax.anyNotNull
import com.simprints.testframework.common.syntax.whenever

fun setupLoginInfoToBeSignedIn(loginInfoManagerSpy: LoginInfoManager,
                               projectId: String, userId: String) {

    whenever(loginInfoManagerSpy) { getSignedInProjectIdOrEmpty() } thenReturn projectId
    whenever(loginInfoManagerSpy) { getSignedInUserIdOrEmpty() } thenReturn userId
    whenever(loginInfoManagerSpy) { getEncryptedProjectSecretOrEmpty() } thenReturn projectId
    whenever(loginInfoManagerSpy) { isProjectIdSignedIn(anyNotNull()) } thenReturn true
}
