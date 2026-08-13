package com.simprints.infra.camera

import android.content.Context
import android.util.AttributeSet
import android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.camera.view.PreviewView
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("UI class")
class CameraPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    val previewView: PreviewView = PreviewView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    val injectionOverlay: ImageView = ImageView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        scaleType = ImageView.ScaleType.FIT_CENTER
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        visibility = GONE
    }

    init {
        addView(previewView)
        addView(injectionOverlay)
    }
}
