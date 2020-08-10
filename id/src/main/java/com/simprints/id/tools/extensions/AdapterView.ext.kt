package com.simprints.id.tools.extensions

import android.view.MotionEvent
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

// Long press on Spinners can be buggy
fun AdapterView<*>.disableLongPress() {
    setOnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_UP) v.performClick()
        true
    }
}
