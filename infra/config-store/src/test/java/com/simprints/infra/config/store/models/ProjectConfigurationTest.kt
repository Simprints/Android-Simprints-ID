package com.simprints.infra.config.store.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC
import com.simprints.infra.config.store.models.SynchronizationConfiguration.Frequency.PERIODICALLY
import com.simprints.infra.config.store.models.SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.UpSynchronizationKind.ALL
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.UpSynchronizationKind.NONE
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS
import com.simprints.infra.config.store.testtools.faceConfiguration
import com.simprints.infra.config.store.testtools.fingerprintConfiguration
import com.simprints.infra.config.store.testtools.projectConfiguration
import com.simprints.infra.config.store.testtools.rankOneConfiguration
import com.simprints.infra.config.store.testtools.simprintsUpSyncConfigurationConfiguration
import com.simprints.infra.config.store.testtools.synchronizationConfiguration
import org.junit.Test

class ProjectConfigurationTest {
    @Test
    fun `canCoSyncAllData should return the correct value`() {
        val values = mapOf(
            ALL to true,
            ONLY_ANALYTICS to false,
            ONLY_BIOMETRICS to false,
            NONE to false,
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        coSync = CoSyncUpSynchronizationConfiguration(
                            kind = it.key,
                        ),
                    ),
                ),
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
            NONE to false,
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        coSync = CoSyncUpSynchronizationConfiguration(
                            kind = it.key,
                        ),
                    ),
                ),
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
            NONE to false,
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        coSync = CoSyncUpSynchronizationConfiguration(
                            kind = it.key,
                        ),
                    ),
                ),
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
            NONE to false,
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        coSync = CoSyncUpSynchronizationConfiguration(
                            kind = it.key,
                        ),
                    ),
                ),
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
            NONE to false,
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        simprints = simprintsUpSyncConfigurationConfiguration.copy(
                            kind = it.key,
                        ),
                    ),
                ),
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
            NONE to false,
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        simprints = simprintsUpSyncConfigurationConfiguration.copy(
                            kind = it.key,
                        ),
                    ),
                ),
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
            NONE to false,
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        simprints = SimprintsUpSynchronizationConfiguration(
                            kind = it.key,
                            batchSizes = UpSynchronizationConfiguration.UpSyncBatchSizes.default(),
                            imagesRequireUnmeteredConnection = false,
                        ),
                    ),
                ),
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
            NONE to false,
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        simprints = simprintsUpSyncConfigurationConfiguration.copy(
                            kind = it.key,
                        ),
                    ),
                ),
            )
            assertThat(config.canSyncDataToSimprints()).isEqualTo(it.value)
        }
    }

    @Test
    fun `isEventDownSyncAllowed should return the correct value`() {
        val values = mapOf(
            ONLY_PERIODICALLY_UP_SYNC to false,
            PERIODICALLY to true,
            PERIODICALLY_AND_ON_SESSION_START to true,
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    frequency = it.key,
                ),
            )
            assertThat(config.isEventDownSyncAllowed()).isEqualTo(it.value)
        }
    }

    @Test
    fun `imagesRequireUnmeteredConnection should return the correct value`() {
        val values = listOf(true, false)

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        simprints = simprintsUpSyncConfigurationConfiguration.copy(
                            imagesRequireUnmeteredConnection = it,
                        ),
                    ),
                ),
            )
            assertThat(config.imagesUploadRequiresUnmeteredConnection()).isEqualTo(it)
        }
    }

    @Test
    fun `allowedAgeRanges returns all non-null age ranges`() {
        val faceAgeRange = AgeGroup(10, 20)
        val secugenSimMatcherAgeRange = AgeGroup(20, 30)

        val projectConfiguration = projectConfiguration.copy(
            face = faceConfiguration.copy(
                rankOne = faceConfiguration.rankOne?.copy(
                    allowedAgeRange = faceAgeRange,
                ),
            ),
            fingerprint = fingerprintConfiguration.copy(
                secugenSimMatcher = fingerprintConfiguration.secugenSimMatcher?.copy(
                    allowedAgeRange = secugenSimMatcherAgeRange,
                ),
                nec = null,
            ),
        )

        // Act
        val result = projectConfiguration.allowedAgeRanges()

        // Assert
        assertThat(result).containsExactly(faceAgeRange, secugenSimMatcherAgeRange)
    }

    @Test
    fun `isAgeRestricted should return false when all are empty`() {
        // Arrange
        val projectConfiguration = projectConfiguration.copy(
            face = faceConfiguration.copy(rankOne = rankOneConfiguration.copy(allowedAgeRange = AgeGroup(0, null))),
            fingerprint = fingerprintConfiguration.copy(
                secugenSimMatcher = fingerprintConfiguration.secugenSimMatcher?.copy(allowedAgeRange = AgeGroup(0, null)),
                nec = null,
            ),
        )

        // Act
        val result = projectConfiguration.isAgeRestricted()

        // Assert
        assertThat(result).isFalse()
    }

    @Test
    fun `isAgeRestricted should return false when all age ranges are empty`() {
        // Arrange
        val emptyAgeRange = AgeGroup(0, 0)

        val projectConfiguration = projectConfiguration.copy(
            face = faceConfiguration.copy(rankOne = rankOneConfiguration.copy(allowedAgeRange = emptyAgeRange)),
            fingerprint = fingerprintConfiguration.copy(
                secugenSimMatcher = fingerprintConfiguration.secugenSimMatcher?.copy(allowedAgeRange = emptyAgeRange),
                nec = null,
            ),
        )

        // Act
        val result = projectConfiguration.isAgeRestricted()

        // Assert
        assertThat(result).isFalse()
    }

    @Test
    fun `isAgeRestricted should return true when all age ranges are non-empty`() {
        // Arrange
        val faceAgeRange = AgeGroup(10, 20)
        val secugenSimMatcherAgeRange = AgeGroup(20, 30)

        val projectConfiguration = projectConfiguration.copy(
            face = faceConfiguration.copy(rankOne = rankOneConfiguration.copy(allowedAgeRange = faceAgeRange)),
            fingerprint = fingerprintConfiguration.copy(
                secugenSimMatcher = fingerprintConfiguration.secugenSimMatcher?.copy(allowedAgeRange = secugenSimMatcherAgeRange),
                nec = null,
            ),
        )

        // Act
        val result = projectConfiguration.isAgeRestricted()

        // Assert
        assertThat(result).isTrue()
    }

    @Test
    fun `sortedUniqueAgeGroups should return (0, null) when all age groups are empty`() {
        val projectConfiguration = projectConfiguration.copy(
            face = faceConfiguration.copy(rankOne = null),
            fingerprint = fingerprintConfiguration.copy(
                secugenSimMatcher = null,
                nec = null,
            ),
        )

        val result = projectConfiguration.sortedUniqueAgeGroups()
        val expected = listOf(AgeGroup(0, null))

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sortedUniqueAgeGroups should return a sorted list of unique age groups when there are no overlapping age groups`() {
        val faceAgeRange = AgeGroup(10, 20)
        val secugenSimMatcherAgeRange = AgeGroup(20, 30)

        val projectConfiguration = projectConfiguration.copy(
            face = faceConfiguration.copy(rankOne = rankOneConfiguration.copy(allowedAgeRange = faceAgeRange)),
            fingerprint = fingerprintConfiguration.copy(
                secugenSimMatcher = fingerprintConfiguration.secugenSimMatcher?.copy(allowedAgeRange = secugenSimMatcherAgeRange),
                nec = null,
            ),
        )

        val result = projectConfiguration.sortedUniqueAgeGroups()
        val expected = listOf(
            AgeGroup(0, faceAgeRange.startInclusive),
            faceAgeRange,
            secugenSimMatcherAgeRange,
            AgeGroup(secugenSimMatcherAgeRange.endExclusive!!, null),
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sortedUniqueAgeGroups should handle overlapping age groups correctly`() {
        val faceAgeRange = AgeGroup(10, 20)
        val secugenSimMatcherAgeRange = AgeGroup(15, 30)

        val projectConfiguration = projectConfiguration.copy(
            face = faceConfiguration.copy(rankOne = rankOneConfiguration.copy(allowedAgeRange = faceAgeRange)),
            fingerprint = fingerprintConfiguration.copy(
                secugenSimMatcher = fingerprintConfiguration.secugenSimMatcher?.copy(allowedAgeRange = secugenSimMatcherAgeRange),
                nec = null,
            ),
        )

        val result = projectConfiguration.sortedUniqueAgeGroups()
        val expected = listOf(
            AgeGroup(0, faceAgeRange.startInclusive),
            AgeGroup(faceAgeRange.startInclusive, secugenSimMatcherAgeRange.startInclusive),
            AgeGroup(secugenSimMatcherAgeRange.startInclusive, faceAgeRange.endExclusive),
            AgeGroup(faceAgeRange.endExclusive!!, secugenSimMatcherAgeRange.endExclusive!!),
            AgeGroup(secugenSimMatcherAgeRange.endExclusive!!, null),
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sortedUniqueAgeGroups should remove duplicates and sort the age groups correctly`() {
        val faceAgeRange = AgeGroup(10, 20)
        val duplicateAgeRange = AgeGroup(10, 20)
        val secugenSimMatcherAgeRange = AgeGroup(20, 30)

        val projectConfiguration = projectConfiguration.copy(
            face = faceConfiguration.copy(rankOne = rankOneConfiguration.copy(allowedAgeRange = faceAgeRange)),
            fingerprint = fingerprintConfiguration.copy(
                secugenSimMatcher = fingerprintConfiguration.secugenSimMatcher?.copy(allowedAgeRange = secugenSimMatcherAgeRange),
                nec = fingerprintConfiguration.nec?.copy(allowedAgeRange = duplicateAgeRange),
            ),
        )

        val result = projectConfiguration.sortedUniqueAgeGroups()
        val expected = listOf(
            AgeGroup(0, faceAgeRange.startInclusive),
            faceAgeRange,
            secugenSimMatcherAgeRange,
            AgeGroup(secugenSimMatcherAgeRange.endExclusive!!, null),
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sortedUniqueAgeGroups should handle correctly a mix of restricted and unrestricted SDKs`() {
        val faceAgeRange = AgeGroup(0, null)
        val secugenSimMatcherAgeRange = AgeGroup(20, 30)

        val projectConfiguration = projectConfiguration.copy(
            face = faceConfiguration.copy(rankOne = rankOneConfiguration.copy(allowedAgeRange = faceAgeRange)),
            fingerprint = fingerprintConfiguration.copy(
                secugenSimMatcher = fingerprintConfiguration.secugenSimMatcher?.copy(allowedAgeRange = secugenSimMatcherAgeRange),
                nec = null,
            ),
        )

        val result = projectConfiguration.sortedUniqueAgeGroups()
        val expected = listOf(
            AgeGroup(0, secugenSimMatcherAgeRange.startInclusive),
            secugenSimMatcherAgeRange,
            AgeGroup(secugenSimMatcherAgeRange.endExclusive!!, null),
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sortedUniqueAgeGroups should handle correctly overlapping null end ranges`() {
        val faceAgeRange = AgeGroup(0, null)
        val secugenSimMatcherAgeRange = AgeGroup(20, null)

        val projectConfiguration = projectConfiguration.copy(
            face = faceConfiguration.copy(rankOne = rankOneConfiguration.copy(allowedAgeRange = faceAgeRange)),
            fingerprint = fingerprintConfiguration.copy(
                secugenSimMatcher = fingerprintConfiguration.secugenSimMatcher?.copy(allowedAgeRange = secugenSimMatcherAgeRange),
                nec = null,
            ),
        )

        val result = projectConfiguration.sortedUniqueAgeGroups()
        val expected = listOf(
            AgeGroup(0, secugenSimMatcherAgeRange.startInclusive),
            secugenSimMatcherAgeRange,
        )

        assertThat(result).isEqualTo(expected)
    }
}
