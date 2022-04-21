package com.simprints.id.activities.fetchguid

import com.google.common.truth.Truth
import com.simprints.id.activities.login.viewmodel.LoginViewModel
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test

class FetchGuidViewModelFactoryTest {

    @Test
    fun `calling create should return a FetchGuidViewModel`() {
        val factory = spyk(
            FetchGuidViewModelFactory(
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
            )
        )

        val viewModel = factory.create(FetchGuidViewModel::class.java)
        Truth.assertThat(viewModel is FetchGuidViewModel).isTrue()
    }

    @Test
    fun `calling create on different class should throw exception`() {
        val factory = spyk(
            FetchGuidViewModelFactory(
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true)
            )
        )

        var exception: Exception? = null
        try {
            factory.create(LoginViewModel::class.java)
        } catch (ex: Exception) {
            exception = ex
        }
        assert(exception is IllegalArgumentException)
        assert(exception?.message.contentEquals("ViewModel Not Found"))
    }
}
