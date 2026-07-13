package com.simprints.infra.config.store.local.migrations

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.local.models.ProtoFaceConfiguration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProjectConfigFaceAutoCaptureConfigMigrationTest {
    private val migration = ProjectConfigFaceAutoCaptureConfigMigration()

    @Test
    fun `should not migrate if face is missing`() = runTest {
        val currentData = ProtoProjectConfiguration.newBuilder().build()

        assertThat(migration.shouldMigrate(currentData)).isFalse()
    }

    @Test
    fun `should not migrate if face already has auto capture`() = runTest {
        val currentData = projectConfig(
            customJson = """{"faceAutoCaptureEnabled":true}""",
            hasAutoCapture = true,
            isAutoCapture = true,
        )

        assertThat(migration.shouldMigrate(currentData)).isFalse()
    }

    @Test
    fun `should not migrate if face already has disabled auto capture`() = runTest {
        val currentData = projectConfig(
            customJson = """{"faceAutoCaptureEnabled":false}""",
            hasAutoCapture = true,
            isAutoCapture = false,
        )

        assertThat(migration.shouldMigrate(currentData)).isFalse()
    }

    @Test
    fun `should not migrate if custom json is missing`() = runTest {
        val currentData = projectConfig()

        assertThat(migration.shouldMigrate(currentData)).isFalse()
    }

    @Test
    fun `should not migrate if custom json does not contain auto capture flag`() = runTest {
        val currentData = projectConfig(customJson = """{"somethingElse":true}""")

        assertThat(migration.shouldMigrate(currentData)).isFalse()
    }

    @Test
    fun `should migrate if custom json contains auto capture flag`() = runTest {
        val currentData = projectConfig(customJson = """{"faceAutoCaptureEnabled":true}""")

        assertThat(migration.shouldMigrate(currentData)).isTrue()
    }

    @Test
    fun `migrate should set auto capture to true when custom json flag is true`() = runTest {
        val currentData = projectConfig(customJson = """{"faceAutoCaptureEnabled":true}""")
        val migratedData = migration.migrate(currentData)

        assertThat(migratedData.face.isAutoCapture).isTrue()
    }

    @Test
    fun `migrate should set auto capture to false when custom json flag is false`() = runTest {
        val currentData = projectConfig(customJson = """{"faceAutoCaptureEnabled":false}""")

        val migratedData = migration.migrate(currentData)

        assertThat(migratedData.face.isAutoCapture).isFalse()
    }

    @Test
    fun `migrate should default auto capture to false when custom json is malformed`() = runTest {
        val currentData = projectConfig(customJson = """{"faceAutoCaptureEnabled":true""")

        val migratedData = migration.migrate(currentData)

        assertThat(migratedData.face.isAutoCapture).isFalse()
    }

    private fun projectConfig(
        customJson: String? = null,
        hasAutoCapture: Boolean = false,
        isAutoCapture: Boolean = false,
    ): ProtoProjectConfiguration {
        val faceBuilder = ProtoFaceConfiguration
            .newBuilder()
            .setRankOne(
                ProtoFaceConfiguration.ProtoFaceSdkConfiguration
                    .newBuilder()
                    .setVersion("1.0")
                    .build(),
            )

        if (hasAutoCapture) {
            faceBuilder.setIsAutoCapture(isAutoCapture)
        }

        val projectBuilder = ProtoProjectConfiguration
            .newBuilder()
            .setFace(faceBuilder.build())

        if (customJson != null) {
            projectBuilder.setCustomJson(customJson)
        }

        return projectBuilder.build()
    }
}
