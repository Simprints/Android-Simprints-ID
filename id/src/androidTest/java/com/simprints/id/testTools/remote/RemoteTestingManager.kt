package com.simprints.id.testTools.remote

import com.simprints.id.testTools.DEFAULT_USER_ID
import com.simprints.id.testTools.models.*

interface RemoteTestingManager {

    companion object {
        fun create() = RemoteTestingManagerImpl()
    }

    fun createTestProject(): TestProject
    fun createTestProject(testProjectCreationParameters: TestProjectCreationParameters): TestProject
    fun deleteTestProject(projectId: String)

    fun getFirebaseToken(projectId: String, userId: String = DEFAULT_USER_ID): TestFirebaseToken
    fun getFirebaseToken(testFirebaseTokenParameters: TestFirebaseTokenParameters): TestFirebaseToken

    fun getSessionSignatures(projectId: String): List<TestSessionSignature>
    fun getSessionCount(projectId: String): TestSessionCount
}
