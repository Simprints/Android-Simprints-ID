package com.simprints.id.activities.login.viewmodel

import com.google.common.truth.Truth
import com.simprints.id.activities.setup.SetupViewModel
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test


class LoginViewModelFactoryTest {

    @Test
    fun `calling create should return a LoginViewModel`() {
        val factory = spyk(
            LoginViewModelFactory(
                mockk(),
                mockk()
            )
        )

        val viewModel = factory.create(LoginViewModel::class.java)
        Truth.assertThat(viewModel is LoginViewModel).isTrue()
    }

    @Test
    fun `calling create on different class should throw exception`() {
        val factory = spyk(
            LoginViewModelFactory(
                mockk(),
                mockk(),
            )
        )

        var exception: Exception? = null
        try {
            factory.create(SetupViewModel::class.java)
        } catch (ex: Exception) {
            exception = ex
        }
        assert(exception is IllegalArgumentException)
    }
}
