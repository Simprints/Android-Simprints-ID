package com.simprints.id.tools

import android.text.Editable
import android.text.TextWatcher

fun textWatcherOnChange(function: (charSequence: String) -> Unit) = object : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
        function(charSequence.toString())
    }

    override fun afterTextChanged(s: Editable) {
    }
}
