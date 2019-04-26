package com.simprints.fingerprint.integration.testtools.remote

import com.simprints.fingerprint.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.fingerprint.integration.testtools.models.*

interface RemoteTestingManager {

    companion object {
        fun create() = RemoteTestingManagerImpl()
    }

    fun createTestProject(testProjectCreationParameters: TestProjectCreationParameters = TestProjectCreationParameters()): TestProject
    fun deleteTestProject(projectId: String)

    fun generateFirebaseToken(projectId: String, userId: String = DEFAULT_USER_ID): TestFirebaseToken
    fun generateFirebaseToken(testFirebaseTokenParameters: TestFirebaseTokenParameters): TestFirebaseToken

    fun getSessionSignatures(projectId: String): List<TestSessionSignature>
    fun getSessionCount(projectId: String): TestSessionCount
}
