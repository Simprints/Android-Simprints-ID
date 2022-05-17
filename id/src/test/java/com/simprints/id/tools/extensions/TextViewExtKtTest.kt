package com.simprints.id.tools.extensions

import android.widget.TextView
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TextViewExtKtTest {

    @Test(expected = IllegalAccessException::class)
    fun getTextColor() {
        //Given
        val textView = mockk<TextView>()
        // When
        val color = textView.textColor

        // Then Should throw if tried to get text color
    }

    @Test
    fun setTextColor() {
        //Given
        val textView = mockk<TextView>()
        // When
        textView.textColor = 0xff0000

        // Then
        verify { textView.setTextColor(0xff0000) }

    }
}
