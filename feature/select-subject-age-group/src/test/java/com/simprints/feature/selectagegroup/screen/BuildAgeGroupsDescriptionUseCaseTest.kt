package com.simprints.feature.selectagegroup.screen

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class BuildAgeGroupsDescriptionUseCaseTest {
    private lateinit var buildAgeGroupsDescription: BuildAgeGroupsDescriptionUseCase

    private val context = InstrumentationRegistry.getInstrumentation().context

    @MockK
    private lateinit var configurationRepo: ConfigRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic("com.simprints.infra.config.store.models.ProjectConfigurationKt")
        buildAgeGroupsDescription = BuildAgeGroupsDescriptionUseCase(configurationRepo, context)
    }

    @Test
    fun testAgeGroupDescriptions() = runTest {
        coEvery { configurationRepo.getProjectConfiguration().allowedAgeRanges() } returns listOf(
            AgeGroup(6, 60), AgeGroup(120, null), AgeGroup(60, 120)
        )
        val result = buildAgeGroupsDescription()
        val expected = arrayOf(
            "0 months to 6 months",
            "6 months to 5 years",
            "5 years to 10 years",
            "10 years and above"
        )
        assertAgeGroupDescriptionsMatchExpected(result, expected)
    }

    @Test
    fun testAgeGroupsOverlapping() = runTest {
        coEvery { configurationRepo.getProjectConfiguration().allowedAgeRanges() } returns listOf(
            AgeGroup(6, 60), AgeGroup(36, null), AgeGroup(60, null)
        )

        val result = buildAgeGroupsDescription()
        val expected = arrayOf(
            "0 months to 6 months", "6 months to 3 years", "3 years to 5 years", "5 years and above"
        )

        assertAgeGroupDescriptionsMatchExpected(result, expected)
    }

    @Test
    fun testAgeGroupWithInitialAndFinalMissing() = runTest {
        coEvery { configurationRepo.getProjectConfiguration().allowedAgeRanges() } returns listOf(
            AgeGroup(6, 60)
        )

        val result = buildAgeGroupsDescription()
        val expected = arrayOf("0 months to 6 months", "6 months to 5 years", "5 years and above")

        assertAgeGroupDescriptionsMatchExpected(result, expected)
    }

    @Test
    fun testEmptyAgeGroup() = runTest {
        coEvery { configurationRepo.getProjectConfiguration().allowedAgeRanges() } returns listOf()
        val result = buildAgeGroupsDescription()

        val expected = arrayOf("0 months and above")

        assertAgeGroupDescriptionsMatchExpected(result, expected)
    }

    @Test
    fun testAgeGroupsWithMonthsFraction() = runTest {
        coEvery { configurationRepo.getProjectConfiguration().allowedAgeRanges() } returns listOf(
            AgeGroup(6, 63), AgeGroup(125, null), AgeGroup(63, 125)
        )
        val result = buildAgeGroupsDescription()
        val expected = arrayOf(
            "0 months to 6 months",
            "6 months to 5 years, 3 months",
            "5 years, 3 months to 10 years, 5 months",
            "10 years, 5 months and above"
        )

        assertAgeGroupDescriptionsMatchExpected(result, expected)
    }

    private fun assertAgeGroupDescriptionsMatchExpected(
        result: List<AgeGroupDisplayModel>, expected: Array<String>
    ) = assertThat(result.map { it.displayString }).containsExactlyElementsIn(expected)


}

