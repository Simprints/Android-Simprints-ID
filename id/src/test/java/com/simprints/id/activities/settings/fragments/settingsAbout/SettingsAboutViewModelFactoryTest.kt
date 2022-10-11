package com.simprints.id.activities.settings.fragments.settingsAbout

import com.google.common.truth.Truth.assertThat
import com.simprints.id.activities.login.viewmodel.LoginViewModel
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test

class SettingsAboutViewModelFactoryTest {

    @Test
    fun `calling create should return a SettingsAboutViewModel`() {
        val factory = spyk(
            SettingsAboutViewModelFactory(
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
            )
        )

        val viewModel = factory.create(SettingsAboutViewModel::class.java)
        assertThat(viewModel).isInstanceOf(SettingsAboutViewModel::class.java)
    }

    @Test
    fun `calling create on different class should throw exception`() {
        val factory = spyk(
            SettingsAboutViewModelFactory(
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
            )
        )

        var exception: Exception? = null
        try {
            factory.create(LoginViewModel::class.java)
        } catch (ex: Exception) {
            exception = ex
        }
        assert(exception is IllegalArgumentException)
    }

}
