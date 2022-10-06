package com.simprints.infra.config.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.models.*
import com.simprints.infra.config.domain.models.SynchronizationConfiguration.Frequency.*
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.UpSynchronizationKind.*
import com.simprints.infra.config.testtools.projectConfiguration
import com.simprints.infra.config.testtools.synchronizationConfiguration
import org.junit.Test

class ProjectConfigurationTest {

    @Test
    fun `canCoSyncAllData should return the correct value`() {
        val values = mapOf(
            ALL to true,
            ONLY_ANALYTICS to false,
            ONLY_BIOMETRICS to false,
            NONE to false
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        coSync = CoSyncUpSynchronizationConfiguration(
                            kind = it.key
                        )
                    )
                )
            )
            assertThat(config.canCoSyncAllData()).isEqualTo(it.value)
        }
    }

    @Test
    fun `canCoSyncBiometricData should return the correct value`() {
        val values = mapOf(
            ALL to false,
            ONLY_ANALYTICS to false,
            ONLY_BIOMETRICS to true,
            NONE to false
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        coSync = CoSyncUpSynchronizationConfiguration(
                            kind = it.key
                        )
                    )
                )
            )
            assertThat(config.canCoSyncBiometricData()).isEqualTo(it.value)
        }
    }

    @Test
    fun `canCoSyncAnalyticsData should return the correct value`() {
        val values = mapOf(
            ALL to false,
            ONLY_ANALYTICS to true,
            ONLY_BIOMETRICS to false,
            NONE to false
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        coSync = CoSyncUpSynchronizationConfiguration(
                            kind = it.key
                        )
                    )
                )
            )
            assertThat(config.canCoSyncAnalyticsData()).isEqualTo(it.value)
        }
    }

    @Test
    fun `canCoSyncData should return the correct value`() {
        val values = mapOf(
            ALL to true,
            ONLY_ANALYTICS to true,
            ONLY_BIOMETRICS to true,
            NONE to false
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        coSync = CoSyncUpSynchronizationConfiguration(
                            kind = it.key
                        )
                    )
                )
            )
            assertThat(config.canCoSyncData()).isEqualTo(it.value)
        }
    }

    @Test
    fun `canSyncAllDataToSimprints should return the correct value`() {
        val values = mapOf(
            ALL to true,
            ONLY_ANALYTICS to false,
            ONLY_BIOMETRICS to false,
            NONE to false
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        simprints = SimprintsUpSynchronizationConfiguration(
                            kind = it.key
                        )
                    )
                )
            )
            assertThat(config.canSyncAllDataToSimprints()).isEqualTo(it.value)
        }
    }

    @Test
    fun `canSyncBiometricDataToSimprints should return the correct value`() {
        val values = mapOf(
            ALL to false,
            ONLY_ANALYTICS to false,
            ONLY_BIOMETRICS to true,
            NONE to false
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        simprints = SimprintsUpSynchronizationConfiguration(
                            kind = it.key
                        )
                    )
                )
            )
            assertThat(config.canSyncBiometricDataToSimprints()).isEqualTo(it.value)
        }
    }

    @Test
    fun `canSyncAnalyticsDataToSimprints should return the correct value`() {
        val values = mapOf(
            ALL to false,
            ONLY_ANALYTICS to true,
            ONLY_BIOMETRICS to false,
            NONE to false
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        simprints = SimprintsUpSynchronizationConfiguration(
                            kind = it.key
                        )
                    )
                )
            )
            assertThat(config.canSyncAnalyticsDataToSimprints()).isEqualTo(it.value)
        }
    }

    @Test
    fun `canSyncDataToSimprints should return the correct value`() {
        val values = mapOf(
            ALL to true,
            ONLY_ANALYTICS to true,
            ONLY_BIOMETRICS to true,
            NONE to false
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        simprints = SimprintsUpSynchronizationConfiguration(
                            kind = it.key
                        )
                    )
                )
            )
            assertThat(config.canSyncDataToSimprints()).isEqualTo(it.value)
        }
    }

    @Test
    fun `isEventDownSyncAllowed should return the correct value`() {
        val values = mapOf(
            ONLY_PERIODICALLY_UP_SYNC to false,
            PERIODICALLY to true,
            PERIODICALLY_AND_ON_SESSION_START to true
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    frequency = it.key
                )
            )
            assertThat(config.isEventDownSyncAllowed()).isEqualTo(it.value)
        }
    }
}
