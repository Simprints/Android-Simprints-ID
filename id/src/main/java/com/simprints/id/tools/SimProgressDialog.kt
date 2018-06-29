package com.simprints.id.tools

import android.app.Activity
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.simprints.id.R
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning

// Custom UI for standard ProgressDialog (deprecated: https://developer.android.com/reference/android/app/ProgressDialog.html)
class SimProgressDialog(private val act: Activity, private val dismissibleByUser: Boolean = false) {

    private lateinit var rootLayout: RelativeLayout
    private lateinit var background: RelativeLayout

    init {
        act.runOnUiThreadIfStillRunning {
            val actLayout = act.findViewById(android.R.id.content) as ViewGroup

            rootLayout = actLayout.getChildAt(0) as? RelativeLayout
                ?: throw Exception("To use SimProgressDialog, the root layout has to be a RelativeLayout")

            background = act.layoutInflater.inflate(R.layout.progress_dialog, rootLayout, false) as RelativeLayout
            background.setOnClickListener {
                if (dismissibleByUser) {
                    dismiss()
                }
            }
        }
    }

    fun show() {
        act.runOnUiThreadIfStillRunning {
            dismiss()
            rootLayout.addView(background)
        }
    }

    fun dismiss() {
        act.runOnUiThreadIfStillRunning {
            rootLayout.removeView(background)
        }
    }
}
