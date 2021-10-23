package com.simprints.id.activities.settings.syncinformation

import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.activities.login.viewmodel.LoginViewModel
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test

class SyncInformationViewModelFactoryTest {

    @Test
    fun `calling create should return a SyncInformationViewModel`() {
        val factory = spyk(
            SyncInformationViewModelFactory(
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                DEFAULT_PROJECT_ID,
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true)
            )
        )

        val viewModel = factory.create(SyncInformationViewModel::class.java)
        assert(viewModel is SyncInformationViewModel)
    }

    @Test
    fun `calling create on different class should throw exception`() {
        val factory = spyk(
            SyncInformationViewModelFactory(
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                DEFAULT_PROJECT_ID,
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true)
            )
        )

        var exception: Exception? = null
        try {
            factory.create(LoginViewModel::class.java)
        } catch (ex: Exception){
            exception = ex
        }
        assert(exception is IllegalArgumentException)
        assert(exception?.message.contentEquals("ViewModel Not Found"))
    }

}
