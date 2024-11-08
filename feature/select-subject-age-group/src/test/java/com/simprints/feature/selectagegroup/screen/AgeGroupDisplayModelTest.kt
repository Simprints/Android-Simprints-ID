package com.simprints.feature.selectagegroup.screen

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import com.simprints.infra.config.store.models.AgeGroup
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.collections.forEach
import kotlin.to

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class AgeGroupDisplayModelTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `Correctly resolve display name`() {
        mapOf(
            AgeGroup(0, 6) to "0 months to 6 months",
            AgeGroup(6, 60) to "6 months to 5 years",
            AgeGroup(60, 120) to "5 years to 10 years",
            AgeGroup(60, 63) to "5 years to 5 years, 3 months",
            AgeGroup(63, 125) to "5 years, 3 months to 10 years, 5 months",
            AgeGroup(120, null) to "10 years and above",
            AgeGroup(125, null) to "10 years, 5 months and above",
        ).forEach { (age, expected) ->
            Truth.assertThat(age.displayName(context)).isEqualTo(expected)
        }
    }

}
