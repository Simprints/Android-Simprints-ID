package com.simprints.infra.config.store.models

import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.UpSynchronizationKind.ALL
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.UpSynchronizationKind.NONE
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS
import com.simprints.infra.config.store.testtools.faceConfiguration
import com.simprints.infra.config.store.testtools.faceSdkConfiguration
import com.simprints.infra.config.store.testtools.fingerprintConfiguration
import com.simprints.infra.config.store.testtools.fingerprintSdkConfiguration
import com.simprints.infra.config.store.testtools.projectConfiguration
import com.simprints.infra.config.store.testtools.simprintsDownSyncConfigurationConfiguration
import com.simprints.infra.config.store.testtools.simprintsUpSyncConfigurationConfiguration
import com.simprints.infra.config.store.testtools.synchronizationConfiguration
import com.simprints.infra.config.store.testtools.vero2Configuration
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
                            frequency = Frequency.PERIODICALLY,
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
    fun `isSimprintsEventDownSyncAllowed should return the correct value for enabled Simprints config`() {
        val values = mapOf(
            Frequency.ONLY_PERIODICALLY_UP_SYNC to false,
            Frequency.PERIODICALLY to true,
            Frequency.PERIODICALLY_AND_ON_SESSION_START to true,
        )

        values.forEach {
            val config = projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = simprintsDownSyncConfigurationConfiguration.copy(
                            frequency = it.key,
                        ),
                    ),
                ),
            )

            assertThat(config.isSimprintsEventDownSyncAllowed()).isEqualTo(it.value)
        }
    }

    @Test
    fun `isSimprintsEventDownSyncAllowed should return false for disabled Simprints config`() {
        val config = projectConfiguration.copy(
            synchronization = synchronizationConfiguration.copy(
                down = synchronizationConfiguration.down.copy(
                    simprints = null,
                ),
            ),
        )

        assertThat(config.isSimprintsEventDownSyncAllowed()).isEqualTo(false)
    }

    @Test
    fun `isCommCareEventDownSyncAllowed should return true for enabled CommCare config`() {
        val config = projectConfiguration.copy(
            synchronization = synchronizationConfiguration.copy(
                down = synchronizationConfiguration.down.copy(
                    commCare = DownSynchronizationConfiguration.CommCareDownSynchronizationConfiguration,
                ),
            ),
        )

        assertThat(config.isCommCareEventDownSyncAllowed()).isEqualTo(true)
    }

    @Test
    fun `isCommCareEventDownSyncAllowed should return false for disabled CommCare config`() {
        val config = projectConfiguration.copy(
            synchronization = synchronizationConfiguration.copy(
                down = synchronizationConfiguration.down.copy(
                    commCare = null,
                ),
            ),
        )

        assertThat(config.isCommCareEventDownSyncAllowed()).isEqualTo(false)
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
    fun `isSampleUploadEnabledInProject should return correct based on available saving strategy`() {
        data class TestData(
            val rankOneStrategy: FaceConfiguration.ImageSavingStrategy? = null,
            val simFaceStrategy: FaceConfiguration.ImageSavingStrategy? = null,
            val secugenStrategy: Vero2Configuration.ImageSavingStrategy? = null,
            val necStragery: Vero2Configuration.ImageSavingStrategy? = null,
            val result: Boolean,
        )
        listOf(
            TestData(
                rankOneStrategy = FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN,
                simFaceStrategy = FaceConfiguration.ImageSavingStrategy.NEVER,
                result = true,
            ),
            TestData(
                necStragery = Vero2Configuration.ImageSavingStrategy.NEVER,
                secugenStrategy = Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN,
                result = true,
            ),
            TestData(
                simFaceStrategy = FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN,
                secugenStrategy = Vero2Configuration.ImageSavingStrategy.NEVER,
                result = true,
            ),
            TestData(
                simFaceStrategy = FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN,
                secugenStrategy = Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN,
                result = true,
            ),
            TestData(
                necStragery = Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN,
                result = true,
            ),
            TestData(
                rankOneStrategy = FaceConfiguration.ImageSavingStrategy.NEVER,
                simFaceStrategy = FaceConfiguration.ImageSavingStrategy.NEVER,
                necStragery = Vero2Configuration.ImageSavingStrategy.NEVER,
                secugenStrategy = Vero2Configuration.ImageSavingStrategy.NEVER,
                result = false,
            ),
            TestData(
                rankOneStrategy = FaceConfiguration.ImageSavingStrategy.NEVER,
                necStragery = Vero2Configuration.ImageSavingStrategy.NEVER,
                result = false,
            ),
            TestData(result = false),
        ).forEach { (rankOne, simFace, secugen, nec, result) ->
            assertThat(
                projectConfiguration
                    .copy(
                        face = faceConfiguration.copy(
                            rankOne = rankOne?.let { faceSdkConfiguration.copy(imageSavingStrategy = it) },
                            simFace = simFace?.let { faceSdkConfiguration.copy(imageSavingStrategy = it) },
                        ),
                        fingerprint = fingerprintConfiguration.copy(
                            secugenSimMatcher = secugen?.let {
                                fingerprintSdkConfiguration.copy(vero2 = vero2Configuration.copy(imageSavingStrategy = it))
                            },
                            nec = nec?.let {
                                fingerprintSdkConfiguration.copy(vero2 = vero2Configuration.copy(imageSavingStrategy = it))
                            },
                        ),
                    ).isSampleUploadEnabledInProject(),
            ).isEqualTo(result)
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
            face = faceConfiguration.copy(rankOne = faceSdkConfiguration.copy(allowedAgeRange = AgeGroup(0, null))),
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
            face = faceConfiguration.copy(rankOne = faceSdkConfiguration.copy(allowedAgeRange = emptyAgeRange)),
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
            face = faceConfiguration.copy(rankOne = faceSdkConfiguration.copy(allowedAgeRange = faceAgeRange)),
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
            face = faceConfiguration.copy(rankOne = faceSdkConfiguration.copy(allowedAgeRange = faceAgeRange)),
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
            face = faceConfiguration.copy(rankOne = faceSdkConfiguration.copy(allowedAgeRange = faceAgeRange)),
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
            face = faceConfiguration.copy(rankOne = faceSdkConfiguration.copy(allowedAgeRange = faceAgeRange)),
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
            face = faceConfiguration.copy(rankOne = faceSdkConfiguration.copy(allowedAgeRange = faceAgeRange)),
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
            face = faceConfiguration.copy(rankOne = faceSdkConfiguration.copy(allowedAgeRange = faceAgeRange)),
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

    @Test
    fun `isProjectWithModuleSync should return true when partition type is MODULE`() {
        val config = projectConfiguration.copy(
            synchronization = synchronizationConfiguration.copy(
                down = synchronizationConfiguration.down.copy(
                    simprints = simprintsDownSyncConfigurationConfiguration.copy(
                        partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                    ),
                ),
            ),
        )
        assertThat(config.isProjectWithModuleSync()).isTrue()
    }

    @Test
    fun `isProjectWithModuleSync should return false when partition type is not MODULE`() {
        val config = projectConfiguration.copy(
            synchronization = synchronizationConfiguration.copy(
                down = synchronizationConfiguration.down.copy(
                    simprints = simprintsDownSyncConfigurationConfiguration.copy(
                        partitionType = DownSynchronizationConfiguration.PartitionType.PROJECT,
                    ),
                ),
            ),
        )
        assertThat(config.isProjectWithModuleSync()).isFalse()
    }

    @Test
    fun `isProjectWithPeriodicallyUpSync should return true when frequency is ONLY_PERIODICALLY_UP_SYNC`() {
        val config = projectConfiguration.copy(
            synchronization = synchronizationConfiguration.copy(
                up = synchronizationConfiguration.up.copy(
                    simprints = simprintsUpSyncConfigurationConfiguration.copy(
                        frequency = Frequency.ONLY_PERIODICALLY_UP_SYNC,
                    ),
                ),
            ),
        )
        assertThat(config.isProjectWithPeriodicallyUpSync()).isTrue()
    }

    @Test
    fun `isProjectWithPeriodicallyUpSync should return false when frequency is not ONLY_PERIODICALLY_UP_SYNC`() {
        val config = projectConfiguration.copy(
            synchronization = synchronizationConfiguration.copy(
                up = synchronizationConfiguration.up.copy(
                    simprints = simprintsUpSyncConfigurationConfiguration.copy(
                        frequency = Frequency.PERIODICALLY,
                    ),
                ),
            ),
        )
        assertThat(config.isProjectWithPeriodicallyUpSync()).isFalse()
    }

    @Test
    fun `isModuleSelectionAvailable should return true when project has MODULE and not ONLY_PERIODICALLY_UP_SYNC`() {
        val config = projectConfiguration.copy(
            synchronization = synchronizationConfiguration.copy(
                down = synchronizationConfiguration.down.copy(
                    simprints = simprintsDownSyncConfigurationConfiguration.copy(
                        partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                    ),
                ),
                up = synchronizationConfiguration.up.copy(
                    simprints = simprintsUpSyncConfigurationConfiguration.copy(
                        frequency = Frequency.PERIODICALLY,
                    ),
                ),
            ),
        )
        assertThat(config.isModuleSelectionAvailable()).isTrue()
    }

    @Test
    fun `isModuleSelectionAvailable should return false when partition type is not MODULE`() {
        val config = projectConfiguration.copy(
            synchronization = synchronizationConfiguration.copy(
                down = synchronizationConfiguration.down.copy(
                    simprints = simprintsDownSyncConfigurationConfiguration.copy(
                        partitionType = DownSynchronizationConfiguration.PartitionType.PROJECT,
                    ),
                ),
                up = synchronizationConfiguration.up.copy(
                    simprints = simprintsUpSyncConfigurationConfiguration.copy(
                        frequency = Frequency.ONLY_PERIODICALLY_UP_SYNC,
                    ),
                ),
            ),
        )
        assertThat(config.isModuleSelectionAvailable()).isFalse()
    }

    @Test
    fun `isModuleSelectionAvailable should return false when frequency is ONLY_PERIODICALLY_UP_SYNC`() {
        val config = projectConfiguration.copy(
            synchronization = synchronizationConfiguration.copy(
                down = synchronizationConfiguration.down.copy(
                    simprints = simprintsDownSyncConfigurationConfiguration.copy(
                        partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                    ),
                ),
                up = synchronizationConfiguration.up.copy(
                    simprints = simprintsUpSyncConfigurationConfiguration.copy(
                        frequency = Frequency.ONLY_PERIODICALLY_UP_SYNC,
                    ),
                ),
            ),
        )
        assertThat(config.isModuleSelectionAvailable()).isFalse()
    }
}
