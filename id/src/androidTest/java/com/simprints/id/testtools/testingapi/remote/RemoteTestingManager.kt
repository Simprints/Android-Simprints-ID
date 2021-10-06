package com.simprints.id.testtools.testingapi.remote

import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.id.testtools.testingapi.models.*

interface RemoteTestingManager {

    companion object {
        fun create(dispatcher: DispatcherProvider) = RemoteTestingManagerImpl(dispatcher)
    }

    fun createTestProject(testProjectCreationParameters: TestProjectCreationParameters = TestProjectCreationParameters()): TestProject
    fun deleteTestProject(projectId: String)

    fun generateFirebaseToken(projectId: String, userId: String = DEFAULT_USER_ID): TestFirebaseToken
    fun generateFirebaseToken(testFirebaseTokenParameters: TestFirebaseTokenParameters): TestFirebaseToken

    fun getEventCount(projectId: String): TestEventCount
}
