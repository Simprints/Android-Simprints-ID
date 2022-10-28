package com.simprints.core.tools.extentions

import android.text.Editable
import android.text.TextWatcher

fun textWatcherOnChange(function: (charSequence: String) -> Unit) = object : TextWatcher {

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // We do not need to listen to changes to the edit text field before the text is changed.
    }

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
        function(charSequence.toString())
    }

    override fun afterTextChanged(s: Editable) {
        // We do not need to listen to changes to the edit text field after the text is changed.
    }

}
