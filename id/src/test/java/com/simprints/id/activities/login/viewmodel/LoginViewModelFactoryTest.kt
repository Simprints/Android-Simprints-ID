package com.simprints.id.activities.login.viewmodel

import com.simprints.id.activities.coreexitform.CoreExitFormViewModel
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

        factory.create(LoginViewModel::class.java)
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
            factory.create(CoreExitFormViewModel::class.java)
        } catch (ex: Exception) {
            exception = ex
        }
        assert(exception is IllegalArgumentException)
    }
}
