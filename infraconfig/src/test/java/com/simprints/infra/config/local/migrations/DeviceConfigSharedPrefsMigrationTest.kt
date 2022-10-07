package com.simprints.infra.config.local.migrations

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.local.models.ProtoDeviceConfiguration
import com.simprints.infra.config.local.models.ProtoFinger
import com.simprints.infra.config.testtools.protoDeviceConfiguration
import com.simprints.infra.login.LoginManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeviceConfigSharedPrefsMigrationTest {

    private val ctx = mockk<Context>()
    private val preferences = mockk<SharedPreferences>(relaxed = true)
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
    fun `should migrate the language when it exists`() = runTest {
        every {
            preferences.getString(
                DeviceConfigSharedPrefsMigration.LANGUAGE_KEY,
                ""
            )
        } returns LANGUAGE
        every {
            preferences.getBoolean(
                DeviceConfigSharedPrefsMigration.LANGUAGE_OVERRIDDEN_KEY,
                false
            )
        } returns true

        val deviceConfiguration =
            deviceConfigSharedPrefsMigration.migrate(ProtoDeviceConfiguration.getDefaultInstance())

        val expectedDeviceConfiguration = ProtoDeviceConfiguration
            .newBuilder()
            .setLanguage(
                ProtoDeviceConfiguration.Language.newBuilder().setLanguage(LANGUAGE)
                    .setIsOverwritten(true)
            )
            .build()
        assertThat(deviceConfiguration).isEqualTo(expectedDeviceConfiguration)
    }

    @Test
    fun `should migrate the fingersToCollect when it exists`() = runTest {
        every {
            preferences.getString(
                DeviceConfigSharedPrefsMigration.FINGERS_TO_COLLECT_KEY,
                ""
            )
        } returns "LEFT_THUMB,LEFT_THUMB,LEFT_INDEX_FINGER"
        every {
            preferences.getBoolean(
                DeviceConfigSharedPrefsMigration.FINGERS_TO_COLLECT_OVERRIDDEN_KEY,
                false
            )
        } returns true

        val deviceConfiguration =
            deviceConfigSharedPrefsMigration.migrate(ProtoDeviceConfiguration.getDefaultInstance())

        val expectedDeviceConfiguration = ProtoDeviceConfiguration
            .newBuilder()
            .setFingersToCollect(
                ProtoDeviceConfiguration.FingersToCollect.newBuilder()
                    .addAllFingersToCollect(
                        listOf(
                            ProtoFinger.LEFT_THUMB,
                            ProtoFinger.LEFT_THUMB,
                            ProtoFinger.LEFT_INDEX_FINGER
                        )
                    )
                    .setIsOverwritten(true)
            )
            .build()
        assertThat(deviceConfiguration).isEqualTo(expectedDeviceConfiguration)
    }

    @Test
    fun `should migrate the selectedModules when it exists`() = runTest {
        every {
            preferences.getString(
                DeviceConfigSharedPrefsMigration.SELECTED_MODULES_KEY,
                ""
            )
        } returns "module1|module2"

        val deviceConfiguration =
            deviceConfigSharedPrefsMigration.migrate(ProtoDeviceConfiguration.getDefaultInstance())

        val expectedDeviceConfiguration = ProtoDeviceConfiguration
            .newBuilder()
            .addAllModuleSelected(listOf("module1","module2"))
            .build()
        assertThat(deviceConfiguration).isEqualTo(expectedDeviceConfiguration)
    }

    @Test
    fun `should migrate the lastInstructionId`() = runTest {
        every {
            preferences.getString(
                DeviceConfigSharedPrefsMigration.LAST_INSTRUCTION_ID_KEY,
                ""
            )
        } returns "instruction"
        val deviceConfiguration =
            deviceConfigSharedPrefsMigration.migrate(ProtoDeviceConfiguration.getDefaultInstance())

        val expectedDeviceConfiguration = ProtoDeviceConfiguration
            .newBuilder()
            .setLastInstructionId("instruction")
            .build()
        assertThat(deviceConfiguration).isEqualTo(expectedDeviceConfiguration)
    }

    companion object {
        private const val LANGUAGE = "fr"
    }
}
