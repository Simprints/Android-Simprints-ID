package com.simprints.infra.config.local.migrations

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.local.models.ProtoDeviceConfiguration
import com.simprints.infra.config.testtools.protoDeviceConfiguration
import com.simprints.infra.login.LoginManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeviceConfigSharedPrefsMigrationTest {

    private val ctx = mockk<Context>()
    private val preferences = mockk<SharedPreferences>()
    private val loginManager = mockk<LoginManager>()
    private lateinit var deviceConfigSharedPrefsMigration: DeviceConfigSharedPrefsMigration

    @Before
    fun setup() {
        every { ctx.getSharedPreferences(any(), any()) } returns preferences
        deviceConfigSharedPrefsMigration = DeviceConfigSharedPrefsMigration(ctx, loginManager)
    }

    @Test
    fun `shouldMigrate should return true only if the project is signed in and the current data empty`() =
        runTest {
            every { loginManager.signedInProjectId } returns "project_id"

            val shouldMigrate =
                deviceConfigSharedPrefsMigration.shouldMigrate(ProtoDeviceConfiguration.getDefaultInstance())
            assertThat(shouldMigrate).isTrue()
        }

    @Test
    fun `shouldMigrate should return false if the project is not signed in`() =
        runTest {
            every { loginManager.signedInProjectId } returns ""

            val shouldMigrate =
                deviceConfigSharedPrefsMigration.shouldMigrate(ProtoDeviceConfiguration.getDefaultInstance())
            assertThat(shouldMigrate).isFalse()
        }

    @Test
    fun `shouldMigrate should return false if the current data is not empty`() =
        runTest {
            every { loginManager.signedInProjectId } returns "project_id"

            val shouldMigrate = deviceConfigSharedPrefsMigration.shouldMigrate(
                protoDeviceConfiguration
            )
            assertThat(shouldMigrate).isFalse()
        }

    @Test
    fun `should migrate the fields that exists`() = runTest {

    }
}
