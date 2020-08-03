package com.simprints.id.tools.extensions

import android.view.View
import android.widget.AdapterView

fun AdapterView<*>.onItemSelectedWithPosition(listener: (position: Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            listener(position)
        }
    }
}
