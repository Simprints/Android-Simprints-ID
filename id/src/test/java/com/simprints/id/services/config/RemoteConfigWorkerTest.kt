package com.simprints.id.services.config

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class RemoteConfigWorkerTest {
    private val app = ApplicationProvider.getApplicationContext<TestApplication>()

    private lateinit var remoteConfigWorker: RemoteConfigWorker

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcherProvider = object : DispatcherProvider {
        override fun main(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun default(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun io(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun unconfined(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher
    }

    private val loginInfoManagerMock: LoginInfoManager = mockk {
        every { signedInProjectId } returns UUID.randomUUID().toString()
    }

    private val projectRepositoryMock: ProjectRepository = mockk()

    @Before
    fun setUp() {
        remoteConfigWorker = TestListenableWorkerBuilder<RemoteConfigWorker>(app).build().apply {
            loginInfoManager = loginInfoManagerMock
            projectRepository = projectRepositoryMock
            remoteConfigWrapper = mockk(relaxed = true)
            dispatcherProvider = testDispatcherProvider
        }
        app.component = mockk(relaxed = true)
    }

    @Test
    fun `when download settings correctly - should return success`() = testCoroutineRule.runBlockingTest {
        coEvery { projectRepositoryMock.loadProjectRemoteConfigSettingsJsonString(any()) } returns
            JsonHelper.fromJson("{}")

        val result = remoteConfigWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `when not signed in - should return success`() = testCoroutineRule.runBlockingTest {
        every { loginInfoManagerMock.signedInProjectId } returns ""

        val result = remoteConfigWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 0) { projectRepositoryMock.loadProjectRemoteConfigSettingsJsonString(any()) }
    }

}
