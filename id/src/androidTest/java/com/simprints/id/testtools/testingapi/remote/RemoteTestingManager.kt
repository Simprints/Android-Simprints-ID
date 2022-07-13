package com.simprints.id.testtools.testingapi.remote

import android.content.Context
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.id.testtools.testingapi.models.*

interface RemoteTestingManager {

    companion object {
        fun create(ctx: Context) = RemoteTestingManagerImpl(ctx)
    }

    suspend fun createTestProject(testProjectCreationParameters: TestProjectCreationParameters = TestProjectCreationParameters()): TestProject
    suspend fun deleteTestProject(projectId: String)

    suspend fun generateFirebaseToken(
        projectId: String,
        userId: String = DEFAULT_USER_ID
    ): TestFirebaseToken

    suspend fun generateFirebaseToken(testFirebaseTokenParameters: TestFirebaseTokenParameters): TestFirebaseToken

    suspend fun getEventCount(projectId: String): TestEventCount
}
