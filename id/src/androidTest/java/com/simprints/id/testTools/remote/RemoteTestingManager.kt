package com.simprints.id.testTools.remote

import com.simprints.id.testTools.models.*

interface RemoteTestingManager {

    companion object {
        fun create() = RemoteTestingManagerImpl()
    }

    fun createTestProject(projectId: String, userId: String = "the_lone_user"): TestProject
    fun createTestProject(testProjectCreationParameters: TestProjectCreationParameters): TestProject
    fun deleteTestProject(projectId: String)

    fun getFirebaseToken(projectId: String, userId: String = "the_lone_user"): TestFirebaseToken
    fun getFirebaseToken(testFirebaseTokenParameters: TestFirebaseTokenParameters): TestFirebaseToken

    fun getSessionSignatures(projectId: String): List<TestSessionSignature>
    fun getSessionCount(projectId: String): TestSessionCount
}
