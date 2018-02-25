package com.simprints.id.tools

import android.app.Activity
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.simprints.id.R
import com.simprints.id.tools.extensions.ifStillRunning

// Custom UI for standard ProgressDialog (deprecated: https://developer.android.com/reference/android/app/ProgressDialog.html)
class SimProgressDialog(private val act: Activity, private val dismissibleByUser: Boolean = false) {

    private val progressBar: ProgressBar by lazy {
        ProgressBar(act, null, android.R.attr.progressBarStyleLarge).apply {
            val pixels = (80 * context.resources.displayMetrics.density).toInt()
            val params = RelativeLayout.LayoutParams(pixels, pixels)
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            layoutParams = params

            val drawableBackground = ContextCompat.getDrawable(act, R.drawable.bt_progress_dialog)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                background = drawableBackground
            } else {
                setBackgroundDrawable(drawableBackground)
            }
            visibility = View.GONE
        }
    }

    init {
        act.ifStillRunning {
            val actLayout = act.findViewById(android.R.id.content) as ViewGroup
            val rootLayout = actLayout.getChildAt(0) as? RelativeLayout
                ?: throw Exception("To use SimProgressDialog, the root layout has to be a RelativeLayout")

            rootLayout.addView(progressBar)
        }
    }

    fun show() {
        act.ifStillRunning {
            progressBar.visibility = View.VISIBLE
            if (dismissibleByUser) {
                progressBar.setOnClickListener {
                    this.dismiss()
                }
            } else {
                act.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }
    }

    fun dismiss() {
        act.ifStillRunning {
            progressBar.visibility = View.GONE

            if (!dismissibleByUser) {
                act.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }
    }
}
