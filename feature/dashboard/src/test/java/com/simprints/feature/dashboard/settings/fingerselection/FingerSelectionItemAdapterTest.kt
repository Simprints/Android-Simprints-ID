package com.simprints.feature.dashboard.settings.fingerselection

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.TemplateIdentifier
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertThrows
import org.junit.Test

class FingerSelectionItemAdapterTest {
    private val context = mockk<Context>(relaxed = true)

    @Test
    fun `unknown finger name lookup throws IllegalArgumentException`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            TemplateIdentifier.NONE.toString(context)
        }

        assertThat(exception.message).isEqualTo("Incorrect sample identifier")
        verify(exactly = 0) { context.getString(any()) }
    }
}
