package com.simprints.feature.selectagegroup.screen

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.allowedAgeRanges
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class BuildAgeGroupsDescriptionUseCaseTest {

    private lateinit var buildAgeGroups: BuildAgeGroupsUseCase

    @MockK
    private lateinit var configurationRepo: ConfigRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic("com.simprints.infra.config.store.models.ProjectConfigurationKt")
        buildAgeGroups = BuildAgeGroupsUseCase(configurationRepo)
    }

    @Test
    fun testAgeGroupDescriptions() = runTest {
        coEvery { configurationRepo.getProjectConfiguration().allowedAgeRanges() } returns listOf(
            AgeGroup(6, 60), AgeGroup(120, null), AgeGroup(60, 120)
        )
        val result = buildAgeGroups()
        val expected = listOf(AgeGroup(0, 6), AgeGroup(6, 60), AgeGroup(60, 120), AgeGroup(120, null))
        assertThat(result).containsExactlyElementsIn(expected)
    }

    @Test
    fun testAgeGroupsOverlapping() = runTest {
        coEvery { configurationRepo.getProjectConfiguration().allowedAgeRanges() } returns listOf(
            AgeGroup(6, 60), AgeGroup(36, null), AgeGroup(60, null)
        )
        val result = buildAgeGroups()
        val expected = listOf(AgeGroup(0, 6), AgeGroup(6, 36), AgeGroup(36, 60), AgeGroup(60, null))
        assertThat(result).containsExactlyElementsIn(expected)
    }

    @Test
    fun testAgeGroupWithInitialAndFinalMissing() = runTest {
        coEvery { configurationRepo.getProjectConfiguration().allowedAgeRanges() } returns listOf(
            AgeGroup(6, 60)
        )
        val result = buildAgeGroups()
        val expected = listOf(AgeGroup(0, 6), AgeGroup(6, 60), AgeGroup(60, null))
        assertThat(result).containsExactlyElementsIn(expected)
    }

    @Test
    fun testEmptyAgeGroup() = runTest {
        coEvery { configurationRepo.getProjectConfiguration().allowedAgeRanges() } returns listOf()
        val result = buildAgeGroups()
        val expected = listOf(AgeGroup(0, null))
        assertThat(result).containsExactlyElementsIn(expected)
    }
}
