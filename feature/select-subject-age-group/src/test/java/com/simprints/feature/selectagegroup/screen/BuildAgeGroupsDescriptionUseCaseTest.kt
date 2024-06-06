package com.simprints.feature.selectagegroup.screen

import android.content.Context
import com.google.common.truth.Truth
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.allowedAgeRanges
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import com.simprints.infra.resources.R.string as IDR


class BuildAgeGroupsDescriptionUseCaseTest {
    private lateinit var buildAgeGroupsDescription: BuildAgeGroupsDescriptionUseCase

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var configurationRepo: ConfigRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic("com.simprints.infra.config.store.models.ProjectConfigurationKt")
        buildAgeGroupsDescription = BuildAgeGroupsDescriptionUseCase(configurationRepo, context)
        with(context) {
            every { getString(IDR.age_group_selection_months) } returns "months"
            every { getString(IDR.age_group_selection_year) } returns "year"
            every { getString(IDR.age_group_selection_years) } returns "years"
            every { getString(IDR.age_group_selection_age_range_to) } returns "to"
            every { getString(IDR.age_group_selection_age_range_and_above) } returns "and above"
        }
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

    private fun assertAgeGroupDescriptionsMatchExpected(
        result: List<AgeGroupDisplayModel>, expected: Array<String>
    ) {
        result.forEachIndexed { index, ageGroupDisplayModel ->
            Truth.assertThat(
                ageGroupDisplayModel.displayString
            ).isEqualTo(expected[index])
        }

    }

}

