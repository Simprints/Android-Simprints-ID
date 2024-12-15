package com.simprints.infra.config.store.local.migrations

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.local.migrations.DeviceConfigSharedPrefsMigration.Companion.FINGERS_TO_COLLECT_KEY
import com.simprints.infra.config.store.local.migrations.DeviceConfigSharedPrefsMigration.Companion.FINGERS_TO_COLLECT_OVERRIDDEN_KEY
import com.simprints.infra.config.store.local.migrations.DeviceConfigSharedPrefsMigration.Companion.LANGUAGE_KEY
import com.simprints.infra.config.store.local.migrations.DeviceConfigSharedPrefsMigration.Companion.LANGUAGE_OVERRIDDEN_KEY
import com.simprints.infra.config.store.local.migrations.DeviceConfigSharedPrefsMigration.Companion.LAST_INSTRUCTION_ID_KEY
import com.simprints.infra.config.store.local.migrations.DeviceConfigSharedPrefsMigration.Companion.SELECTED_MODULES_KEY
import com.simprints.infra.config.store.local.models.ProtoDeviceConfiguration
import com.simprints.infra.config.store.testtools.protoDeviceConfiguration
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeviceConfigSharedPrefsMigrationTest {
    private val ctx = mockk<Context>()
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    private val preferences = mockk<SharedPreferences>(relaxed = true) {
        every { edit() } returns editor
    }
    private val authStore = mockk<AuthStore>()
    private lateinit var deviceConfigSharedPrefsMigration: DeviceConfigSharedPrefsMigration

    @Before
    fun setup() {
        every { ctx.getSharedPreferences(any(), any()) } returns preferences
        deviceConfigSharedPrefsMigration = DeviceConfigSharedPrefsMigration(ctx, authStore)
        LanguageHelper.init(mockk(relaxed = true))
    }

    @Test
    fun `shouldMigrate should return true only if the project is signed in and the language preference is not empty`() = runTest {
        every { authStore.signedInProjectId } returns "project_id"
        every { preferences.getString(LANGUAGE_KEY, "") } returns "en"

        val shouldMigrate =
            deviceConfigSharedPrefsMigration.shouldMigrate(ProtoDeviceConfiguration.getDefaultInstance())
        assertThat(shouldMigrate).isTrue()
    }

    @Test
    fun `shouldMigrate should return false if the project is not signed in`() = runTest {
        every { authStore.signedInProjectId } returns ""
        every { preferences.getString(LANGUAGE_KEY, "") } returns "en"

        val shouldMigrate =
            deviceConfigSharedPrefsMigration.shouldMigrate(ProtoDeviceConfiguration.getDefaultInstance())
        assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `shouldMigrate should return false if the preference language is empty`() = runTest {
        every { authStore.signedInProjectId } returns "project_id"
        every { preferences.getString(LANGUAGE_KEY, "") } returns ""

        val shouldMigrate = deviceConfigSharedPrefsMigration.shouldMigrate(
            protoDeviceConfiguration,
        )
        assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `should migrate the language when it exists`() = runTest {
        every {
            preferences.getString(
                LANGUAGE_KEY,
                "",
            )
        } returns LANGUAGE
        every {
            preferences.getBoolean(
                LANGUAGE_OVERRIDDEN_KEY,
                false,
            )
        } returns true

        val deviceConfiguration =
            deviceConfigSharedPrefsMigration.migrate(ProtoDeviceConfiguration.getDefaultInstance())

        val expectedDeviceConfiguration = ProtoDeviceConfiguration
            .newBuilder()
            .setLanguage(
                ProtoDeviceConfiguration.Language
                    .newBuilder()
                    .setLanguage(LANGUAGE)
                    .setIsOverwritten(true),
            ).build()
        assertThat(deviceConfiguration).isEqualTo(expectedDeviceConfiguration)
    }

    @Test
    fun `should migrate the fingersToCollect when it exists`() = runTest {
        every {
            preferences.getString(
                FINGERS_TO_COLLECT_KEY,
                "",
            )
        } returns "LEFT_THUMB,LEFT_THUMB,LEFT_INDEX_FINGER"
        every {
            preferences.getBoolean(
                FINGERS_TO_COLLECT_OVERRIDDEN_KEY,
                false,
            )
        } returns true

        val deviceConfiguration =
            deviceConfigSharedPrefsMigration.migrate(ProtoDeviceConfiguration.getDefaultInstance())

        val expectedDeviceConfiguration = ProtoDeviceConfiguration
            .newBuilder()
            .build()
        assertThat(deviceConfiguration).isEqualTo(expectedDeviceConfiguration)
    }

    @Test
    fun `should migrate the selectedModules when it exists`() = runTest {
        every {
            preferences.getString(
                SELECTED_MODULES_KEY,
                "",
            )
        } returns "module1|module2"

        val deviceConfiguration =
            deviceConfigSharedPrefsMigration.migrate(ProtoDeviceConfiguration.getDefaultInstance())

        val expectedDeviceConfiguration = ProtoDeviceConfiguration
            .newBuilder()
            .addAllModuleSelected(listOf("module1", "module2"))
            .build()
        assertThat(deviceConfiguration).isEqualTo(expectedDeviceConfiguration)
    }

    @Test
    fun `should migrate the lastInstructionId`() = runTest {
        every {
            preferences.getString(
                LAST_INSTRUCTION_ID_KEY,
                "",
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

    @Test
    fun `cleanUp should do remove all the keys`() = runTest {
        every { editor.remove(any()) } returns editor

        deviceConfigSharedPrefsMigration.cleanUp()

        verify(exactly = 1) { editor.remove(LANGUAGE_KEY) }
        verify(exactly = 1) { editor.remove(LANGUAGE_OVERRIDDEN_KEY) }
        verify(exactly = 1) { editor.remove(FINGERS_TO_COLLECT_KEY) }
        verify(exactly = 1) { editor.remove(FINGERS_TO_COLLECT_OVERRIDDEN_KEY) }
        verify(exactly = 1) { editor.remove(LAST_INSTRUCTION_ID_KEY) }
        verify(exactly = 1) { editor.remove(SELECTED_MODULES_KEY) }
        verify(exactly = 1) { editor.apply() }
    }

    companion object {
        private const val LANGUAGE = "fr"
    }
}
