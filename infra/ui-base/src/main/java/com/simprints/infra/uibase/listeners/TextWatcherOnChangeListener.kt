package com.simprints.infra.uibase.listeners

import android.text.Editable
import android.text.TextWatcher

class TextWatcherOnChangeListener(
    private val function: (charSequence: String) -> Unit,
) : TextWatcher {
    override fun beforeTextChanged(
        s: CharSequence,
        start: Int,
        count: Int,
        after: Int,
    ) {
        // We do not need to listen to changes to the edit text field before the text is changed.
    }

    override fun onTextChanged(
        charSequence: CharSequence,
        start: Int,
        before: Int,
        count: Int,
    ) {
        function(charSequence.toString())
    }

    override fun afterTextChanged(s: Editable) {
        // We do not need to listen to changes to the edit text field after the text is changed.
    }
}
