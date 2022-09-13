package com.simprints.id.testtools.testingapi

import android.content.Context
import android.util.Base64
import com.simprints.id.testtools.testingapi.models.TestProject
import com.simprints.id.testtools.testingapi.models.TestProjectCreationParameters
import com.simprints.id.testtools.testingapi.remote.RemoteTestingManager
import com.simprints.infra.security.keyprovider.LocalDbKey
import kotlinx.coroutines.runBlocking
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Creates a new [TestProject] on the backend.
 * Usage as a [TestRule]:
 * @build:Rule val testProjectRule = TestProjectRule()
 */
class TestProjectRule(
    private val ctx: Context,
    private val testProjectCreationParameters: TestProjectCreationParameters = TestProjectCreationParameters()
) : TestWatcher() {

    companion object {
        private const val DEFAULT_REALM_KEY_STRING =
            "Jk1P0NPgwjViIhnvrIZTN3eIpjWRrok5zBZUw1CiQGGWhTFgnANiS87J6asyTksjCHe4SHJo0dHeawAPz3JtgQ=="
        private val DEFAULT_REALM_KEY: ByteArray =
            Base64.decode(DEFAULT_REALM_KEY_STRING, Base64.NO_WRAP)
    }

    lateinit var testProject: TestProject
    lateinit var localDbKey: LocalDbKey

    override fun starting(description: Description?) = runBlocking {

        testProject =
            RemoteTestingManager.create(ctx).createTestProject(testProjectCreationParameters)

        localDbKey = LocalDbKey(
            testProject.id,
            DEFAULT_REALM_KEY
        )
    }
}
