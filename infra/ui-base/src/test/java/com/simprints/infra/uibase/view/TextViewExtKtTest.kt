package com.simprints.infra.uibase.view

import android.content.Context
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextViewExtKtTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val sampleText = context.getString(sampleTextResource)

    private lateinit var textTextView: TextView

    @Before
    fun setUp() {
        textTextView = TextView(context)
    }

    @Test
    fun `sets empty text if no params provided`() {
        textTextView.setTextWithFallbacks(null, null)
        assertThat(textTextView.text).isEqualTo("")
    }

    @Test
    fun `sets raw text if provided`() {
        textTextView.setTextWithFallbacks(rawText, null)
        assertThat(textTextView.text).isEqualTo(rawText)
    }

    @Test
    fun `sets resource text if provided`() {
        textTextView.setTextWithFallbacks(null, sampleTextResource)
        assertThat(textTextView.text).isEqualTo(sampleText)
    }

    @Test
    fun `sets fallback text if provided`() {
        textTextView.setTextWithFallbacks(null, null, sampleTextResource)
        assertThat(textTextView.text).isEqualTo(sampleText)
    }

    companion object {
        private const val rawText = "text"
        private const val sampleTextResource = android.R.string.ok
    }

}
